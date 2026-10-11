package cl.duoc.biblioteca.model;

/** Abstracción común. El DAO invoca valores() de forma polimórfica. */
public abstract class Entidad {
  protected int id;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public abstract Object[] valores();
}
