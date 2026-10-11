package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Prestamo;
import java.sql.*;

public class PrestamoDAO extends AbstractDAO<Prestamo> {
  public PrestamoDAO() {
    super(
        "prestamos",
        "id_estudiante",
        "id_libro",
        "fecha_prestamo",
        "fecha_devolucion",
        "devuelto",
        "fecha_devolucion_real");
  }

  @Override
  protected Prestamo mapear(ResultSet r) throws SQLException {
    return new Prestamo(
        r.getInt("id"),
        r.getInt("id_estudiante"),
        r.getInt("id_libro"),
        r.getObject("fecha_prestamo", java.time.LocalDate.class),
        r.getObject("fecha_devolucion", java.time.LocalDate.class),
        r.getBoolean("devuelto"),
        r.getObject("fecha_devolucion_real", java.time.LocalDate.class));
  }
}
