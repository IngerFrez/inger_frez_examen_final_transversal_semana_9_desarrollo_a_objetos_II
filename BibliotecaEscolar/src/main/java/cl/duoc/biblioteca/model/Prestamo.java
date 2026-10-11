package cl.duoc.biblioteca.model;

import java.time.LocalDate;

public class Prestamo extends Entidad {
  private final int estudiante, libro;
  private final LocalDate inicio, vencimiento, real;
  private final boolean devuelto;

  public Prestamo(int id, int e, int l, LocalDate i, LocalDate v, boolean d, LocalDate r) {
    this.id = id;
    estudiante = e;
    libro = l;
    inicio = i;
    vencimiento = v;
    devuelto = d;
    real = r;
  }

  public boolean estaAtrasado(LocalDate hoy) {
    return vencimiento != null && (real != null ? real : hoy).isAfter(vencimiento);
  }

  @Override
  public Object[] valores() {
    return new Object[] {estudiante, libro, inicio, vencimiento, devuelto, real};
  }
}
