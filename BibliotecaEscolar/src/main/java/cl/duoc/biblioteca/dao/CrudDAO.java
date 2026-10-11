package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Entidad;
import java.sql.SQLException;
import java.util.List;

public interface CrudDAO<T extends Entidad> {
  List<T> listar() throws SQLException;

  T buscar(int id) throws SQLException;

  void crear(T e) throws SQLException;

  void actualizar(T e) throws SQLException;

  void eliminar(int id) throws SQLException;
}
