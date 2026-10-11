package cl.duoc.biblioteca.model;

public abstract class Persona extends Entidad {
  protected String nombre, rut, correo;

  public Persona(int id, String nombre, String rut, String correo) {
    this.id = id;
    this.nombre = nombre;
    this.rut = rut;
    this.correo = correo;
  }

  public String getRut() {
    return rut;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCorreo() {
    return correo;
  }

  @Override
  public String toString() {
    return nombre + " — " + rut;
  }
}
