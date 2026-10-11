package cl.duoc.biblioteca.model;

public class Libro extends Entidad {
  private final String titulo, autor, isbn, editorial;
  private final int stock, categoria;

  public Libro(int id, String t, String a, String i, String e, int s, int c) {
    this.id = id;
    titulo = t;
    autor = a;
    isbn = i;
    editorial = e;
    stock = s;
    categoria = c;
  }

  public int getStock() {
    return stock;
  }

  @Override
  public Object[] valores() {
    return new Object[] {titulo, autor, isbn, editorial, stock, categoria};
  }

  @Override
  public String toString() {
    return titulo + " (disponibles: " + stock + ")";
  }
}
