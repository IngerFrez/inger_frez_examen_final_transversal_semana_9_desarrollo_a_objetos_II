package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Entidad;
import java.sql.*;
import java.util.*;

/** Los identificadores provienen exclusivamente de subclases; datos en PreparedStatement. */
public abstract class AbstractDAO<T extends Entidad> implements CrudDAO<T> {
  protected final DatabaseConnection db = DatabaseConnection.getInstance();
  private final String tabla;
  private final String[] columnas;

  protected AbstractDAO(String tabla, String... columnas) {
    this.tabla = tabla;
    this.columnas = columnas;
  }

  protected abstract T mapear(ResultSet r) throws SQLException;

  public List<T> listar() throws SQLException {
    synchronized (db) {
      List<T> out = new ArrayList<>();
      try (PreparedStatement s =
              db.getConnection().prepareStatement("SELECT * FROM " + tabla + " ORDER BY id");
          ResultSet r = s.executeQuery()) {
        while (r.next()) out.add(mapear(r));
      }
      return out;
    }
  }

  public T buscar(int id) throws SQLException {
    synchronized (db) {
      try (PreparedStatement s =
          db.getConnection().prepareStatement("SELECT * FROM " + tabla + " WHERE id=?")) {
        s.setInt(1, id);
        try (ResultSet r = s.executeQuery()) {
          return r.next() ? mapear(r) : null;
        }
      }
    }
  }

  public void crear(T e) throws SQLException {
    synchronized (db) {
      String qs = String.join(",", Collections.nCopies(columnas.length, "?"));
      try (PreparedStatement s =
          db.getConnection()
              .prepareStatement(
                  "INSERT INTO "
                      + tabla
                      + " ("
                      + String.join(",", columnas)
                      + ") VALUES ("
                      + qs
                      + ")",
                  Statement.RETURN_GENERATED_KEYS)) {
        bind(s, e.valores());
        s.executeUpdate();
        try (ResultSet r = s.getGeneratedKeys()) {
          if (r.next()) e.setId(r.getInt(1));
        }
      }
    }
  }

  public void actualizar(T e) throws SQLException {
    synchronized (db) {
      StringJoiner j = new StringJoiner(",");
      for (String col : columnas) j.add(col + "=?");
      try (PreparedStatement s =
          db.getConnection().prepareStatement("UPDATE " + tabla + " SET " + j + " WHERE id=?")) {
        bind(s, e.valores());
        s.setInt(columnas.length + 1, e.getId());
        if (s.executeUpdate() != 1) throw new SQLException("El registro ya no existe.");
      }
    }
  }

  public void eliminar(int id) throws SQLException {
    synchronized (db) {
      try (PreparedStatement s =
          db.getConnection().prepareStatement("DELETE FROM " + tabla + " WHERE id=?")) {
        s.setInt(1, id);
        if (s.executeUpdate() != 1) throw new SQLException("El registro ya no existe.");
      }
    }
  }

  private void bind(PreparedStatement s, Object[] vals) throws SQLException {
    for (int i = 0; i < vals.length; i++) s.setObject(i + 1, vals[i]);
  }
}
