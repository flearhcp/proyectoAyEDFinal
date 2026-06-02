package LectorJSON;

public class Arista {
    private Vertice origen;
    private Vertice destino;
    private Calle calle;
    private double peso;
  /*   private double distancia;
    private double tiempo; */

    public Arista(Vertice origen,Vertice destino,Calle calle) {
        this.origen = origen;
        this.destino = destino;
        this.calle = calle;
        this.peso = calle.getVelocidadMaxima() / 3.6;
     /*    this.distancia = distancia;
        this.tiempo = tiempo; */
    }

    public double getPeso(){
        return this.peso;
    }

    public Calle getCalle() {
        return calle;
    }

    public Vertice getOrigen (){
        return this.origen;
    }

     public Vertice getDestino (){
        return this.destino;
    }
 
}
