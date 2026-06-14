package LectorJSON;

import java.util.List;

/**
 * Representa un tramo de calle que conecta dos vértices en el grafo del mapa.
 * Contiene el peso para algoritmos de ruta, la calle a la que pertenece
 * y la geometría detallada para su dibujo.
 */
public class Arista {
    /** Vértice de origen de la arista. */
    private Vertice origen;
    /** Vértice de destino de la arista. */
    private Vertice destino;
    /** Referencia a la calle a la que pertenece este tramo. */
    private Calle calle;
    /** Peso de la arista, usualmente costo acumulado o tiempo ETA. */
    private double peso;
    /** Distancia física del tramo en metros. */
    private double distancia;
    /** Lista de coordenadas que definen la forma del tramo (curvas). */
    private List<Coordenada> geometria;

    /**
     * Constructor de Arista.
     * @param origen Vértice de inicio.
     * @param destino Vértice de fin.
     * @param calle Calle asociada.
     * @param peso Costo del tramo.
     * @param distancia Distancia en metros.
     * @param geometria Puntos intermedios.
     */
    public Arista(Vertice origen,Vertice destino,Calle calle,double peso,double distancia, List<Coordenada> geometria) {
        this.origen = origen;
        this.destino = destino;
        this.calle = calle;
        this.peso = peso;
        this.distancia = distancia;
        this.geometria = geometria;
    }

    /** @return El peso/costo de la arista. */
    public double getPeso(){
        return this.peso;
    }

    /** @return La calle a la que pertenece el tramo. */
    public Calle getCalle() {
        return calle;
    }

    /** @return El vértice de origen. */
    public Vertice getOrigen (){
        return this.origen;
    }

    /** @return El vértice de destino. */
    public Vertice getDestino (){
        return this.destino;
    }

    /** @return La distancia del tramo. */
    public double getDistancia(){ return this.distancia;}

    /** @return La lista de coordenadas de la arista. */
    public List<Coordenada> getGeometria(){ return this.geometria;}

}
