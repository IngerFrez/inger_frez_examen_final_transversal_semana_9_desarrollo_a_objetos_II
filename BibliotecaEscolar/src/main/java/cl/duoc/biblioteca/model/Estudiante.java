package cl.duoc.biblioteca.model;

public class Estudiante extends Persona {
  private final String curso;

  public Estudiante(int id, String n, String r, String curso, String c) {
    super(id, n, r, c);
    this.curso = curso;
  }

  @Override
  public Object[] valores() {
    return new Object[] {nombre, rut, curso, correo};
  }
}
