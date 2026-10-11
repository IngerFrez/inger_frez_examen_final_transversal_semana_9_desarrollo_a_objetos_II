package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Estudiante;
import java.sql.*;

public class EstudianteDAO extends AbstractDAO<Estudiante> {
  public EstudianteDAO() {
    super("estudiantes", "nombre", "rut", "curso", "correo");
  }

  @Override
  protected Estudiante mapear(ResultSet r) throws SQLException {
    return new Estudiante(
        r.getInt("id"),
        r.getString("nombre"),
        r.getString("rut"),
        r.getString("curso"),
        r.getString("correo"));
  }
}
