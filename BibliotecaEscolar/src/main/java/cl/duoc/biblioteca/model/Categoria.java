package cl.duoc.biblioteca.model;

public class Categoria extends Entidad {
  private final String nombre;

  public Categoria(int id, String n) {
    this.id = id;
    nombre = n;
  }

  @Override
  public Object[] valores() {
    return new Object[] {nombre};
  }

  @Override
  public String toString() {
    return nombre;
  }
}
