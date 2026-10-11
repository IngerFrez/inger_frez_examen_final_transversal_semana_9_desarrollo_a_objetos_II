package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.model.Usuario;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainView extends JFrame {
  private static final long serialVersionUID = 1L;
  public final JComboBox<String> modulo = new JComboBox<>();
  public final JComboBox<String> reporte =
      new JComboBox<>(
          new String[] {"Préstamos activos", "Libros más prestados", "Historial por estudiante"});
  public final JButton refrescar = new JButton("Actualizar"),
      nuevo = new JButton("Nuevo"),
      editar = new JButton("Modificar"),
      eliminar = new JButton("Eliminar"),
      prestar = new JButton("Prestar libro"),
      devolver = new JButton("Devolver seleccionado"),
      salir = new JButton("Cerrar sesión");
  public final JTable tabla = new JTable();
  public final JLabel estado = new JLabel("Listo");
  public final JTextField filtro = new JTextField(22);

  public MainView(Usuario u) {
    super("Biblioteca Escolar · " + u.getNombre());
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1100, 650);
    setMinimumSize(new Dimension(850, 500));
    setLocationRelativeTo(null);
    JPanel top = new JPanel(new BorderLayout(12, 12));
    top.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
    JLabel title = new JLabel("Biblioteca Escolar  |  " + u.getNombre() + " · " + u.getRol());
    title.setFont(new Font("SansSerif", Font.BOLD, 20));
    top.add(title, BorderLayout.NORTH);
    if (u.esBibliotecario())
      for (String s : new String[] {"Libros", "Estudiantes", "Categorías", "Préstamos", "Reportes"})
        modulo.addItem(s);
    else
      for (String s : new String[] {"Catálogo", "Mis préstamos", "Mi historial"}) modulo.addItem(s);
    JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT));
    nav.add(modulo);
    nav.add(reporte);
    nav.add(refrescar);
    nav.add(new JLabel("Buscar:"));
    nav.add(filtro);
    top.add(nav, BorderLayout.CENTER);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    for (JButton b : new JButton[] {nuevo, editar, eliminar, prestar, devolver, salir})
      buttons.add(b);
    top.add(buttons, BorderLayout.SOUTH);
    add(top, BorderLayout.NORTH);
    tabla.setRowHeight(30);
    tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tabla.setAutoCreateRowSorter(true);
    tabla.getTableHeader().setReorderingAllowed(false);
    JScrollPane scroll = new JScrollPane(tabla);
    scroll.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
    add(scroll);
    estado.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
    add(estado, BorderLayout.SOUTH);
  }

  public void mostrar(String[] cols, java.util.List<Object[]> rows) {
    DefaultTableModel m =
        new DefaultTableModel(cols, 0) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    for (Object[] r : rows) m.addRow(r);
    tabla.setModel(m);
  }

  public int seleccionado() {
    int r = tabla.getSelectedRow();
    if (r < 0) throw new IllegalArgumentException("Selecciona una fila de la tabla.");
    return ((Number) tabla.getValueAt(r, 0)).intValue();
  }
}
