package cl.duoc.biblioteca.service;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.dao.*;
import cl.duoc.biblioteca.model.*;
import java.sql.*;
import java.time.*;

public class BibliotecaService {
  private final DatabaseConnection db = DatabaseConnection.getInstance();
  private final ConsultaDAO q = new ConsultaDAO();
  public final LibroDAO libros = new LibroDAO();
  public final EstudianteDAO estudiantes = new EstudianteDAO();
  public final CategoriaDAO categorias = new CategoriaDAO();

  public Usuario login(String c, String p) throws SQLException {
    db.preparar();
    return q.autenticar(c, p);
  }

  public Estudiante propio(Usuario u) throws SQLException {
    for (Estudiante e : estudiantes.listar()) if (e.getRut().equals(u.getRut())) return e;
    throw new IllegalArgumentException(
        "Esta cuenta no tiene estudiante vinculado. Contacta al bibliotecario.");
  }

  private void admin(Usuario u) {
    if (u == null || !u.esBibliotecario())
      throw new SecurityException("Acceso exclusivo para bibliotecarios.");
  }

  private void permitido(Usuario u, int id) throws SQLException {
    if (u == null) throw new SecurityException("Inicia sesión.");
    if (!u.esBibliotecario() && propio(u).getId() != id)
      throw new SecurityException("Solo puedes operar tus propios préstamos.");
  }

  @FunctionalInterface
  private interface Operacion {
    void run() throws SQLException;
  }

  private void transaccion(Operacion op) throws SQLException {
    synchronized (db) {
      Connection c = db.getConnection();
      boolean prev = c.getAutoCommit();
      c.setAutoCommit(false);
      try {
        op.run();
        c.commit();
      } catch (SQLException | RuntimeException e) {
        try {
          c.rollback();
        } catch (SQLException x) {
          e.addSuppressed(x);
        }
        throw e;
      } finally {
        c.setAutoCommit(prev);
      }
    }
  }

  /**
   * El monitor Singleton protege la conexión y la transacción; el UPDATE condicional protege el
   * stock incluso entre procesos.
   */
  public synchronized void prestar(Usuario u, int estudiante, int libro) throws SQLException {
    permitido(u, estudiante);
    transaccion(
        () -> {
          if (estudiantes.buscar(estudiante) == null)
            throw new IllegalArgumentException("Estudiante inexistente.");
          if (q.ejecutar("UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0", libro) != 1)
            throw new IllegalArgumentException("El libro no tiene stock disponible.");
          LocalDate hoy = LocalDate.now();
          new PrestamoDAO()
              .crear(
                  new Prestamo(
                      0, estudiante, libro, hoy, hoy.plusDays(db.diasPrestamo()), false, null));
          // La fila de prestamos ES el historial persistente del estudiante, conservada tras
          // devolver.
        });
  }

  public synchronized void devolver(Usuario u, int id) throws SQLException {
    transaccion(
        () -> {
          ConsultaDAO.Tabla t =
              q.consultar(
                  "SELECT id_estudiante,id_libro FROM prestamos WHERE id=? AND devuelto=FALSE FOR"
                      + " UPDATE",
                  id);
          if (t.filas().isEmpty())
            throw new IllegalArgumentException("El préstamo ya fue devuelto o no existe.");
          int est = ((Number) t.filas().get(0)[0]).intValue(),
              lib = ((Number) t.filas().get(0)[1]).intValue();
          permitido(u, est);
          if (q.ejecutar(
                  "UPDATE prestamos SET devuelto=TRUE,fecha_devolucion_real=? WHERE id=? AND"
                      + " devuelto=FALSE",
                  LocalDate.now(),
                  id)
              != 1) throw new IllegalArgumentException("Ya fue devuelto.");
          if (q.ejecutar("UPDATE libros SET stock=stock+1 WHERE id=?", lib) != 1)
            throw new SQLException("Libro inexistente.");
        });
  }

  public void guardarLibro(Usuario u, Libro l) throws SQLException {
    admin(u);
    if (l.getStock() < 0) throw new IllegalArgumentException("Stock no puede ser negativo.");
    if (l.getId() == 0) libros.crear(l);
    else libros.actualizar(l);
  }

  public void guardarCategoria(Usuario u, Categoria c) throws SQLException {
    admin(u);
    if (c.getId() == 0) categorias.crear(c);
    else categorias.actualizar(c);
  }

  public void guardarEstudiante(Usuario u, Estudiante e, String clave) throws SQLException {
    admin(u);
    transaccion(
        () -> {
          ConsultaDAO.Tabla duplicado =
              q.consultar(
                  "SELECT id FROM usuarios WHERE correo=? AND rut<>?",
                  e.getCorreo(),
                  e.getId() == 0 ? e.getRut() : estudiantes.buscar(e.getId()).getRut());
          if (!duplicado.filas().isEmpty())
            throw new IllegalArgumentException("El correo ya está asociado a otra cuenta.");
          Estudiante viejo = e.getId() == 0 ? null : estudiantes.buscar(e.getId());
          if (e.getId() == 0) estudiantes.crear(e);
          else estudiantes.actualizar(e);
          String rut = viejo == null ? e.getRut() : viejo.getRut();
          ConsultaDAO.Tabla t = q.consultar("SELECT id,rol FROM usuarios WHERE rut=?", rut);
          if (t.filas().isEmpty()) {
            if (clave.isBlank())
              throw new IllegalArgumentException("Ingresa una contraseña para la nueva cuenta.");
            new UsuarioDAO()
                .crear(
                    new Usuario(0, e.getNombre(), e.getRut(), e.getCorreo(), clave, "estudiante"));
          } else {
            if (!"estudiante".equals(t.filas().get(0)[1]))
              throw new IllegalArgumentException("El RUT pertenece a un bibliotecario.");
            q.ejecutar(
                "UPDATE usuarios SET nombre=?,rut=?,correo=? WHERE rut=?",
                e.getNombre(),
                e.getRut(),
                e.getCorreo(),
                rut);
            if (!clave.isBlank())
              q.ejecutar("UPDATE usuarios SET `contraseña`=? WHERE rut=?", clave, e.getRut());
          }
        });
  }

  public void eliminar(Usuario u, String tipo, int id) throws SQLException {
    admin(u);
    switch (tipo) {
      case "Libros" -> libros.eliminar(id);
      case "Categorías" -> categorias.eliminar(id);
      case "Estudiantes" ->
          transaccion(
              () -> {
                Estudiante e = estudiantes.buscar(id);
                if (e == null) throw new IllegalArgumentException("No existe.");
                estudiantes.eliminar(id);
                q.ejecutar("DELETE FROM usuarios WHERE rut=? AND rol='estudiante'", e.getRut());
              });
      default -> throw new IllegalArgumentException("Módulo inválido.");
    }
  }

  public ConsultaDAO.Tabla inventario() throws SQLException {
    return q.consultar(
        "SELECT l.id AS ID,l.titulo AS Título,l.autor AS Autor,l.isbn AS ISBN,l.editorial AS"
            + " Editorial,l.stock AS Disponibles,c.nombre AS Categoría FROM libros l LEFT JOIN"
            + " categorias c ON c.id=l.id_categoria ORDER BY l.titulo");
  }

  private String listado() {
    return "SELECT p.id AS ID,e.nombre AS Estudiante,l.titulo AS Libro,p.fecha_prestamo AS"
               + " Inicio,p.fecha_devolucion AS Vencimiento,p.fecha_devolucion_real AS `Devolución"
               + " real`,CASE WHEN p.devuelto=TRUE THEN CASE WHEN"
               + " p.fecha_devolucion_real>p.fecha_devolucion THEN 'Devuelto con atraso' ELSE"
               + " 'Devuelto' END WHEN p.fecha_devolucion<CURDATE() THEN 'Atrasado' ELSE 'En"
               + " préstamo' END AS Estado FROM prestamos p JOIN estudiantes e ON"
               + " e.id=p.id_estudiante JOIN libros l ON l.id=p.id_libro";
  }

  public ConsultaDAO.Tabla prestamos(Usuario u, boolean activos, Integer estudiante)
      throws SQLException {
    if (!u.esBibliotecario()) estudiante = propio(u).getId();
    String sql =
        listado()
            + " WHERE 1=1"
            + (activos ? " AND p.devuelto=FALSE" : "")
            + (estudiante != null ? " AND p.id_estudiante=?" : "")
            + " ORDER BY p.id DESC";
    return estudiante == null ? q.consultar(sql) : q.consultar(sql, estudiante);
  }

  public ConsultaDAO.Tabla populares(Usuario u) throws SQLException {
    admin(u);
    return q.consultar(
        "SELECT l.titulo AS Libro,COUNT(p.id) AS Préstamos FROM libros l LEFT JOIN prestamos p ON"
            + " p.id_libro=l.id GROUP BY l.id,l.titulo ORDER BY Préstamos DESC,l.titulo");
  }
}
