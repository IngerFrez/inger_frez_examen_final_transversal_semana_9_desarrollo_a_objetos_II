package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Usuario;
import java.sql.*;

public class UsuarioDAO extends AbstractDAO<Usuario> {
  public UsuarioDAO() {
    super("usuarios", "nombre", "rut", "correo", "`contraseña`", "rol");
  }

  @Override
  protected Usuario mapear(ResultSet r) throws SQLException {
    return new Usuario(
        r.getInt("id"),
        r.getString("nombre"),
        r.getString("rut"),
        r.getString("correo"),
        r.getString("contraseña"),
        r.getString("rol"));
  }
}
