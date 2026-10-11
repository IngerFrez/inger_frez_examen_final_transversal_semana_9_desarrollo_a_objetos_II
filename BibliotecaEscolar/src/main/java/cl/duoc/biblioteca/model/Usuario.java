package cl.duoc.biblioteca.model;

public class Usuario extends Persona {
  private final String clave, rol;

  public Usuario(int id, String n, String r, String c, String clave, String rol) {
    super(id, n, r, c);
    this.clave = clave;
    this.rol = rol;
  }

  public boolean esBibliotecario() {
    return "bibliotecario".equals(rol);
  }

  public String getRol() {
    return rol;
  }

  @Override
  public Object[] valores() {
    return new Object[] {nombre, rut, correo, clave, rol};
  }
}
