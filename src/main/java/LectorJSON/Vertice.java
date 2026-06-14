package LectorJSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en el grafo del mapa, correspondiente a una intersección
 * de calles o un punto extremo de una vía en el mapa de la ciudad.
 */
public class Vertice {
    /** Índice correlativo del vértice dentro del arreglo de datos del grafo. */
    private int indice;
    /** Identificador único original del nodo en OpenStreetMap. */
    private long osmId;
    /** Ubicación geográfica (latitud y longitud) del vértice. */
    private Coordenada coordenada;
    /** Lista de calles que convergen o pasan por este vértice. */
    private List<Calle> calles;
    /** Cantidad de calles asociadas a este vértice. */
    private int cantCalles;

    /**
     * Constructor de Vertice.
     * @param indice Posición asignada en el grafo.
     * @param osmId ID original de OSM.
     * @param coordenada Objeto Coordenada con la ubicación física.
     */
    public Vertice(int indice,long osmId,Coordenada coordenada) {
        this.indice = indice;
        this.osmId = osmId;
        this.coordenada = coordenada;
        this.calles = new ArrayList<>(); 
        this.cantCalles=0;
    }

    /**
     * Asocia una calle a este vértice si no ha sido agregada anteriormente.
     * @param calle La calle a asociar.
     */
    public void agregarCalle(Calle calle) {
        if (!calles.contains(calle)){
            calles.add(calle);
            cantCalles++;
        }
    }

    /** @return La lista de calles asociadas a este vértice. */
    public List<Calle> getCalles() {
        return calles;
    }

    /**
     * Genera un nombre descriptivo para la intersección basado en los nombres de las calles.
     * @return Cadena con el formato "Calle A y Calle B" o el ID si no hay nombres.
     */
    public String getNombreInterseccion(){
        if (calles == null || calles.isEmpty()) {
            return "Intersección sin nombre (ID: " + String.valueOf(osmId) + ")";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < calles.size(); i++) {
            sb.append(calles.get(i).getNombreAMostrar());
            if (i < calles.size() - 1) {
                sb.append(" y ");
            }
        }
        return sb.toString();
    }

    /** @return El índice del vértice en el grafo. */
    public int getIndice() {
        return indice;
    }

    /** @return El objeto Coordenada del vértice. */
    public Coordenada getCoordenada (){
        return this.coordenada;
    }
    
    /** @return La latitud del vértice. */
    public double getLatitud() {
        return coordenada.getLatitud();
    }

    /** @return La longitud del vértice. */
    public double getLongitud() {
        return coordenada.getLongitud();
    }

    /** @return El identificador OSM original. */
    public double getOsmID (){
        return (double)osmId;
    }
    
    /** @return El número de calles que se cruzan en este vértice. */
    public int getCantidadCalles(){
        return cantCalles;
    }
}
