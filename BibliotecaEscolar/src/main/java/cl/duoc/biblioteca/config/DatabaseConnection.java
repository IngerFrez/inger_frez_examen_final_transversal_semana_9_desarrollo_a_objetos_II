package cl.duoc.biblioteca.config;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Singleton: todos los DAO usan esta instancia y su mismo monitor. */
public final class DatabaseConnection {
  private static final DatabaseConnection INSTANCE = new DatabaseConnection();
  private final Properties config = new Properties();
  private Connection connection;

  private DatabaseConnection() {
    config.setProperty(
        "db.url",
        "jdbc:mysql://localhost:3307/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago&connectTimeout=5000&socketTimeout=15000");
    config.setProperty("db.user", "root");
    config.setProperty("db.password", "");
    config.setProperty("prestamo.dias", "7");
    if (Files.exists(Path.of("config.properties")))
      try (InputStream in = Files.newInputStream(Path.of("config.properties"))) {
        config.load(in);
      } catch (IOException e) {
        throw new IllegalStateException("No se pudo leer config.properties", e);
      }
  }

  public static DatabaseConnection getInstance() {
    return INSTANCE;
  }

  public synchronized Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed() || !connection.isValid(2))
      connection =
          DriverManager.getConnection(
              config.getProperty("db.url"),
              config.getProperty("db.user"),
              config.getProperty("db.password"));
    return connection;
  }

  public int diasPrestamo() {
    int d = Integer.parseInt(config.getProperty("prestamo.dias"));
    if (d < 1 || d > 365) throw new IllegalArgumentException("Plazo de préstamo inválido");
    return d;
  }

  public synchronized void preparar() throws SQLException {
    Connection c = getConnection();
    // Migración mínima: conserva fecha_devolucion como vencimiento y registra la devolución real.
    boolean found = false;
    try (ResultSet r =
        c.getMetaData().getColumns(c.getCatalog(), null, "prestamos", "fecha_devolucion_real")) {
      found = r.next();
    }
    if (!found)
      try (Statement s = c.createStatement()) {
        s.executeUpdate("ALTER TABLE prestamos ADD COLUMN fecha_devolucion_real DATE NULL");
      }
  }
}
