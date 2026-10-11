package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Categoria;
import java.sql.*;

public class CategoriaDAO extends AbstractDAO<Categoria> {
  public CategoriaDAO() {
    super("categorias", "nombre");
  }

  @Override
  protected Categoria mapear(ResultSet r) throws SQLException {
    return new Categoria(r.getInt("id"), r.getString("nombre"));
  }
}
