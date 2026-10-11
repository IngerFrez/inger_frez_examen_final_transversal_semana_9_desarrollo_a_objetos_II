package cl.duoc.biblioteca;

import cl.duoc.biblioteca.controller.BibliotecaController;
import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new BibliotecaController().iniciar();
          } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          }
        });
  }
}
