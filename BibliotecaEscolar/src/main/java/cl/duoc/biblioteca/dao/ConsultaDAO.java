package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Usuario;
import java.sql.*;
import java.util.*;

public class ConsultaDAO {
  private final DatabaseConnection db = DatabaseConnection.getInstance();

  public record Tabla(String[] columnas, List<Object[]> filas) {}

  public Tabla consultar(String sql, Object... args) throws SQLException {
    synchronized (db) {
      try (PreparedStatement s = db.getConnection().prepareStatement(sql)) {
        for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
        try (ResultSet r = s.executeQuery()) {
          ResultSetMetaData m = r.getMetaData();
          String[] cols = new String[m.getColumnCount()];
          for (int i = 0; i < cols.length; i++) cols[i] = m.getColumnLabel(i + 1);
          List<Object[]> rows = new ArrayList<>();
          while (r.next()) {
            Object[] row = new Object[cols.length];
            for (int i = 0; i < cols.length; i++) row[i] = r.getObject(i + 1);
            rows.add(row);
          }
          return new Tabla(cols, rows);
        }
      }
    }
  }

  public int ejecutar(String sql, Object... args) throws SQLException {
    synchronized (db) {
      try (PreparedStatement s = db.getConnection().prepareStatement(sql)) {
        for (int i = 0; i < args.length; i++) s.setObject(i + 1, args[i]);
        return s.executeUpdate();
      }
    }
  }

  public Usuario autenticar(String correo, String clave) throws SQLException {
    synchronized (db) {
      try (PreparedStatement s =
          db.getConnection()
              .prepareStatement("SELECT * FROM usuarios WHERE correo=? AND `contraseña`=?")) {
        s.setString(1, correo);
        s.setString(2, clave);
        try (ResultSet r = s.executeQuery()) {
          return r.next()
              ? new Usuario(
                  r.getInt("id"),
                  r.getString("nombre"),
                  r.getString("rut"),
                  r.getString("correo"),
                  r.getString("contraseña"),
                  r.getString("rol"))
              : null;
        }
      }
    }
  }
}
