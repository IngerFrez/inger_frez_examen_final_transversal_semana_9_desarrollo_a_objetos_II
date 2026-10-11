package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Libro;
import java.sql.*;

public class LibroDAO extends AbstractDAO<Libro> {
  public LibroDAO() {
    super("libros", "titulo", "autor", "isbn", "editorial", "stock", "id_categoria");
  }

  @Override
  protected Libro mapear(ResultSet r) throws SQLException {
    return new Libro(
        r.getInt("id"),
        r.getString("titulo"),
        r.getString("autor"),
        r.getString("isbn"),
        r.getString("editorial"),
        r.getInt("stock"),
        r.getInt("id_categoria"));
  }
}
