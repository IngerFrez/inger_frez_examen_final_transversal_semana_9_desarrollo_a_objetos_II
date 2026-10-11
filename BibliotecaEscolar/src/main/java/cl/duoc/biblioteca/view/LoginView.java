package cl.duoc.biblioteca.view;

import java.awt.*;
import javax.swing.*;

public class LoginView extends JFrame {
  private static final long serialVersionUID = 1L;
  public final JTextField correo = new JTextField("antonia@correo.cl", 24);
  public final JPasswordField clave = new JPasswordField(24);
  public final JButton entrar = new JButton("Ingresar");
  public final JLabel estado = new JLabel(" ");

  public LoginView() {
    super("Biblioteca Escolar · Acceso");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    JPanel p = new JPanel(new GridLayout(0, 1, 8, 8));
    p.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
    JLabel t = new JLabel("BIBLIOTECA ESCOLAR");
    t.setFont(new Font("SansSerif", Font.BOLD, 22));
    p.add(t);
    p.add(new JLabel("Correo electrónico"));
    p.add(correo);
    p.add(new JLabel("Contraseña"));
    p.add(clave);
    p.add(entrar);
    p.add(estado);
    setContentPane(p);
    getRootPane().setDefaultButton(entrar);
    pack();
    setResizable(false);
    setLocationRelativeTo(null);
  }
}
