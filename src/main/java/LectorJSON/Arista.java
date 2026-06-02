package LectorJSON;

public class Arista {
    private Vertice origen;
    private Vertice destino;
    private Calle calle;
    private double peso; //calculado en segundos
    private double distancia; //calculado en metros

    public Arista(Vertice origen,Vertice destino,Calle calle, double peso, double distancia) {
        this.origen = origen;
        this.destino = destino;
        this.calle = calle;
        this.peso = peso;
        this.distancia = distancia;
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
    public double getDistancia(){ return this.distancia;}
 
}
