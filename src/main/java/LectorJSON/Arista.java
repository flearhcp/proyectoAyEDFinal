package LectorJSON;

import java.util.List;

public class Arista {
    private Vertice origen;
    private Vertice destino;
    private Calle calle;
    private double peso;
    private double distancia;
    private List<Coordenada> geometria;

    public Arista(Vertice origen,Vertice destino,Calle calle,double peso,double distancia, List<Coordenada> geometria) {
        this.origen = origen;
        this.destino = destino;
        this.calle = calle;
        this.peso = peso;
        this.distancia = distancia;
        this.geometria = geometria;
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

    public List<Coordenada> getGeometria(){ return this.geometria;}

}
