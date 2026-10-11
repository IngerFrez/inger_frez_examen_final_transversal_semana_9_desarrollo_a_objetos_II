package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.*;
import cl.duoc.biblioteca.model.*;
import cl.duoc.biblioteca.service.*;
import cl.duoc.biblioteca.view.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/** Eventos en el EDT; JDBC siempre en SwingWorker. */
public class BibliotecaController {
  private final BibliotecaService servicio = new BibliotecaService();
  private LoginView login;
  private MainView view;
  private Usuario usuario;
  private boolean ocupado;
  private java.util.List<Libro> libros = java.util.List.of();
  private java.util.List<Estudiante> estudiantes = java.util.List.of();
  private java.util.List<Categoria> categorias = java.util.List.of();

  public void iniciar() {
    login = new LoginView();
    login.entrar.addActionListener(e -> acceder());
    login.setVisible(true);
  }

  private void acceder() {
    String correo = login.correo.getText().trim();
    String clave = new String(login.clave.getPassword());
    login.entrar.setEnabled(false);
    login.estado.setText("Conectando…");
    new SwingWorker<Usuario, Void>() {
      protected Usuario doInBackground() throws Exception {
        return servicio.login(correo, clave);
      }

      protected void done() {
        try {
          usuario = get();
          if (usuario == null)
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
          login.dispose();
          abrir();
        } catch (Exception e) {
          login.estado.setText("No se pudo ingresar.");
          error(login, e);
        } finally {
          login.entrar.setEnabled(true);
          login.clave.setText("");
        }
      }
    }.execute();
  }

  private void abrir() {
    view = new MainView(usuario);
    view.modulo.addActionListener(e -> cargar());
    view.reporte.addActionListener(e -> cargar());
    view.refrescar.addActionListener(e -> cargar());
    view.nuevo.addActionListener(e -> formulario(false));
    view.editar.addActionListener(e -> formulario(true));
    view.eliminar.addActionListener(e -> borrar());
    view.prestar.addActionListener(e -> prestar());
    view.devolver.addActionListener(e -> devolver());
    view.salir.addActionListener(
        e -> {
          view.dispose();
          iniciar();
        });
    view.filtro
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void insertUpdate(DocumentEvent e) {
                filtrar();
              }

              public void removeUpdate(DocumentEvent e) {
                filtrar();
              }

              public void changedUpdate(DocumentEvent e) {
                filtrar();
              }
            });
    view.setVisible(true);
    cargar();
  }

  private String modulo() {
    return (String) view.modulo.getSelectedItem();
  }

  private void controles() {
    String m = modulo();
    boolean crud =
        usuario.esBibliotecario()
            && java.util.List.of("Libros", "Estudiantes", "Categorías").contains(m);
    view.nuevo.setVisible(crud);
    view.editar.setVisible(crud);
    view.eliminar.setVisible(crud);
    view.reporte.setVisible("Reportes".equals(m));
    view.prestar.setVisible(!"Categorías".equals(m) && !"Reportes".equals(m));
    view.devolver.setVisible("Préstamos".equals(m) || "Mis préstamos".equals(m));
  }

  private void bloqueo(boolean b) {
    ocupado = b;
    for (JButton x :
        new JButton[] {
          view.nuevo,
          view.editar,
          view.eliminar,
          view.prestar,
          view.devolver,
          view.refrescar,
          view.salir
        }) x.setEnabled(!b);
    view.modulo.setEnabled(!b);
    view.reporte.setEnabled(!b);
  }

  private <T> void tarea(Callable<T> op, java.util.function.Consumer<T> fin) {
    if (ocupado) return;
    bloqueo(true);
    view.estado.setText("Procesando en segundo plano…");
    new SwingWorker<T, Void>() {
      protected T doInBackground() throws Exception {
        return op.call();
      }

      protected void done() {
        bloqueo(false);
        try {
          T r = get();
          view.estado.setText("Listo");
          fin.accept(r);
        } catch (Exception e) {
          view.estado.setText("Operación no completada");
          error(view, e);
        }
      }
    }.execute();
  }

  private record Datos(
      ConsultaDAO.Tabla tabla,
      java.util.List<Libro> libros,
      java.util.List<Estudiante> estudiantes,
      java.util.List<Categoria> categorias) {}

  private void cargar() {
    if (ocupado) return;
    controles();
    String m = modulo(), rep = (String) view.reporte.getSelectedItem();
    Integer est = null;
    if ("Reportes".equals(m) && "Historial por estudiante".equals(rep)) {
      if (estudiantes.isEmpty()) {
        JOptionPane.showMessageDialog(view, "No hay estudiantes registrados.");
        return;
      }
      Estudiante e =
          (Estudiante)
              JOptionPane.showInputDialog(
                  view,
                  "Selecciona estudiante",
                  "Historial",
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  estudiantes.toArray(),
                  estudiantes.get(0));
      if (e == null) return;
      est = e.getId();
    }
    final Integer elegido = est;
    tarea(
        () -> {
          var ls = servicio.libros.listar();
          var es =
              usuario.esBibliotecario()
                  ? servicio.estudiantes.listar()
                  : java.util.List.of(servicio.propio(usuario));
          var cs = servicio.categorias.listar();
          ConsultaDAO.Tabla t;
          switch (m) {
            case "Libros", "Catálogo" -> t = servicio.inventario();
            case "Estudiantes" -> {
              var rows = new ArrayList<Object[]>();
              for (Estudiante e : es) {
                Object[] vals = e.valores(), r = new Object[5];
                r[0] = e.getId();
                System.arraycopy(vals, 0, r, 1, 4);
                rows.add(r);
              }
              t =
                  new ConsultaDAO.Tabla(
                      new String[] {"ID", "Nombre", "RUT", "Curso", "Correo"}, rows);
            }
            case "Categorías" -> {
              var rows = new ArrayList<Object[]>();
              for (Categoria c : cs) rows.add(new Object[] {c.getId(), c.toString()});
              t = new ConsultaDAO.Tabla(new String[] {"ID", "Nombre"}, rows);
            }
            case "Préstamos", "Mis préstamos" -> t = servicio.prestamos(usuario, true, null);
            case "Mi historial" -> t = servicio.prestamos(usuario, false, null);
            case "Reportes" ->
                t =
                    "Libros más prestados".equals(rep)
                        ? servicio.populares(usuario)
                        : servicio.prestamos(usuario, "Préstamos activos".equals(rep), elegido);
            default -> throw new IllegalArgumentException("Módulo desconocido");
          }
          return new Datos(t, ls, es, cs);
        },
        d -> {
          libros = d.libros;
          estudiantes = d.estudiantes;
          categorias = d.categorias;
          view.mostrar(d.tabla.columnas(), d.tabla.filas());
          filtrar();
          view.estado.setText(d.tabla.filas().size() + " registros · " + m);
        });
  }

  @SuppressWarnings("unchecked")
  private void filtrar() {
    if (view.tabla.getRowSorter() instanceof TableRowSorter<?> s) {
      String f = view.filtro.getText();
      ((TableRowSorter<TableModel>) s)
          .setRowFilter(
              f.isBlank()
                  ? null
                  : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(f)));
    }
  }

  private void formulario(boolean editar) {
    try {
      String m = modulo();
      int id = editar ? view.seleccionado() : 0;
      Object[] vals = null;
      if (editar) {
        if (m.equals("Libros"))
          vals = libros.stream().filter(x -> x.getId() == id).findFirst().orElseThrow().valores();
        else if (m.equals("Estudiantes"))
          vals =
              estudiantes.stream().filter(x -> x.getId() == id).findFirst().orElseThrow().valores();
        else
          vals =
              categorias.stream().filter(x -> x.getId() == id).findFirst().orElseThrow().valores();
      }
      String[] etiquetas =
          m.equals("Libros")
              ? new String[] {
                "Título", "Autor", "ISBN", "Editorial", "Stock disponible", "Categoría"
              }
              : m.equals("Estudiantes")
                  ? new String[] {
                    "Nombre",
                    "RUT (sin puntos)",
                    "Curso",
                    "Correo",
                    "Contraseña (vacío conserva actual)"
                  }
                  : new String[] {"Nombre"};
      JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
      JComponent[] campos = new JComponent[etiquetas.length];
      for (int i = 0; i < campos.length; i++) {
        panel.add(new JLabel(etiquetas[i]));
        if (m.equals("Libros") && i == 5) {
          if (categorias.isEmpty())
            throw new IllegalArgumentException("Registra una categoría primero.");
          JComboBox<Categoria> combo = new JComboBox<>(categorias.toArray(new Categoria[0]));
          if (vals != null)
            for (Categoria c : categorias)
              if (c.getId() == ((Number) vals[5]).intValue()) combo.setSelectedItem(c);
          campos[i] = combo;
        } else {
          JTextField f =
              m.equals("Estudiantes") && i == 4 ? new JPasswordField(25) : new JTextField(25);
          if (vals != null && i < vals.length) f.setText(String.valueOf(vals[i]));
          if (m.equals("Libros") && i == 4 && vals == null) f.setText("1");
          campos[i] = f;
        }
        panel.add(campos[i]);
      }
      if (JOptionPane.showConfirmDialog(
              view, panel, (editar ? "Modificar " : "Registrar ") + m, JOptionPane.OK_CANCEL_OPTION)
          != JOptionPane.OK_OPTION) return;
      String[] texts = new String[campos.length];
      for (int i = 0; i < campos.length; i++)
        if (campos[i] instanceof JTextField f) {
          texts[i] = f.getText().trim();
          if (texts[i].isEmpty() && !(m.equals("Estudiantes") && i == 4))
            throw new IllegalArgumentException("Completa " + etiquetas[i] + ".");
        }
      if (m.equals("Estudiantes") && !texts[3].matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
        throw new IllegalArgumentException("Correo inválido.");
      if (m.equals("Libros")) {
        int stock = Integer.parseInt(texts[4]);
        Categoria c = (Categoria) ((JComboBox<?>) campos[5]).getSelectedItem();
        Libro l = new Libro(id, texts[0], texts[1], texts[2], texts[3], stock, c.getId());
        tarea(
            () -> {
              servicio.guardarLibro(usuario, l);
              return true;
            },
            r -> cargar());
      } else if (m.equals("Estudiantes")) {
        Estudiante e = new Estudiante(id, texts[0], texts[1], texts[2], texts[3]);
        tarea(
            () -> {
              servicio.guardarEstudiante(usuario, e, texts[4]);
              return true;
            },
            r -> cargar());
      } else {
        Categoria c = new Categoria(id, texts[0]);
        tarea(
            () -> {
              servicio.guardarCategoria(usuario, c);
              return true;
            },
            r -> cargar());
      }
    } catch (Exception e) {
      error(view, e);
    }
  }

  private void borrar() {
    try {
      int id = view.seleccionado();
      String m = modulo();
      if (JOptionPane.showConfirmDialog(
              view,
              "¿Eliminar el registro seleccionado?",
              "Confirmar eliminación",
              JOptionPane.YES_NO_OPTION)
          != JOptionPane.YES_OPTION) return;
      tarea(
          () -> {
            servicio.eliminar(usuario, m, id);
            return true;
          },
          r -> cargar());
    } catch (Exception e) {
      error(view, e);
    }
  }

  private void prestar() {
    try {
      var disponibles = libros.stream().filter(l -> l.getStock() > 0).toList();
      if (disponibles.isEmpty()) throw new IllegalArgumentException("No hay libros disponibles.");
      if (estudiantes.isEmpty())
        throw new IllegalArgumentException("Registra un estudiante primero.");
      JComboBox<Libro> l = new JComboBox<>(disponibles.toArray(new Libro[0]));
      JComboBox<Estudiante> e = new JComboBox<>(estudiantes.toArray(new Estudiante[0]));
      if (!usuario.esBibliotecario()) e.setEnabled(false);
      JPanel p = new JPanel(new GridLayout(0, 1, 8, 8));
      p.add(new JLabel("Estudiante"));
      p.add(e);
      p.add(new JLabel("Libro"));
      p.add(l);
      p.add(
          new JLabel(
              "Plazo: "
                  + cl.duoc.biblioteca.config.DatabaseConnection.getInstance().diasPrestamo()
                  + " días"));
      if (JOptionPane.showConfirmDialog(view, p, "Nuevo préstamo", JOptionPane.OK_CANCEL_OPTION)
          != JOptionPane.OK_OPTION) return;
      int ei = ((Estudiante) e.getSelectedItem()).getId(),
          li = ((Libro) l.getSelectedItem()).getId();
      tarea(
          () -> {
            servicio.prestar(usuario, ei, li);
            return true;
          },
          r -> {
            JOptionPane.showMessageDialog(view, "Préstamo registrado y stock actualizado.");
            cargar();
          });
    } catch (Exception e) {
      error(view, e);
    }
  }

  private void devolver() {
    try {
      int id = view.seleccionado();
      if (JOptionPane.showConfirmDialog(
              view, "¿Confirmar devolución?", "Devolver libro", JOptionPane.YES_NO_OPTION)
          != JOptionPane.YES_OPTION) return;
      tarea(
          () -> {
            servicio.devolver(usuario, id);
            return true;
          },
          r -> {
            JOptionPane.showMessageDialog(view, "Devolución registrada. Stock recuperado.");
            cargar();
          });
    } catch (Exception e) {
      error(view, e);
    }
  }

  private void error(Component c, Exception e) {
    Throwable t = e;
    while (t.getCause() != null) t = t.getCause();
    String msg = t.getMessage();
    if (t instanceof java.sql.SQLIntegrityConstraintViolationException)
      msg =
          "No se pudo guardar o eliminar: RUT/ISBN repetido, categoría inválida o registro con"
              + " préstamos asociados.";
    else if (t instanceof NumberFormatException) msg = "El stock debe ser un número entero.";
    else if (t instanceof java.sql.SQLException)
      msg =
          "Error de base de datos: "
              + msg
              + "\nRevisa MySQL en XAMPP, el puerto 3307 y config.properties.";
    JOptionPane.showMessageDialog(c, msg, "Atención", JOptionPane.ERROR_MESSAGE);
  }
}
