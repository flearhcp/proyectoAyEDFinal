package LectorJSON;

import java.util.List;

/**
 * Contenedor de datos que representa la estructura procesada de un mapa.
 * Almacena los vértices y aristas resultantes del parseo del JSON de OSM,
 * además de calcular los límites geográficos (bounding box) del área para su renderizado.
 */
public class DatosMapa {
    /** Lista de todos los vértices del grafo. */
    private List<Vertice> vertices;
    /** Lista de todas las aristas (tramos de calle). */
    private List<Arista> aristas;
    /** Límites geográficos para el encuadre del mapa. */
    private double minLat, maxLat, minLon, maxLon;

    /**
     * Constructor de DatosMapa.
     * @param vertices Lista de vértices procesados.
     * @param aristas Lista de aristas procesadas.
     */
    public DatosMapa(List<Vertice> vertices,List<Arista> aristas) {
        this.vertices = vertices;
        this.aristas = aristas;
        calcularLimites();
    }

    private void calcularLimites() {
        if (vertices == null || vertices.isEmpty()) {
            minLat = maxLat = minLon = maxLon = 0.0;
            return;
        }
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;
        for (Vertice v : vertices) {
            double lat = v.getLatitud();
            double lon = v.getLongitud();
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
            if (lon < minLon) minLon = lon;
            if (lon > maxLon) maxLon = lon;
        }
    }

    /** @return La cantidad total de vértices en el mapa. */
    public int getCantidadVertices() {
        return vertices.size();
    }

    /** @return La lista completa de vértices. */
    public List<Vertice> getVertices () {
        return this.vertices;
    }
    
    /** @return La lista completa de aristas. */
    public List<Arista> getAristas () {
        return this.aristas;
    }

    /**
     * Obtiene un vértice específico por su índice interno.
     * @param i El índice del vértice.
     * @return El objeto {@code Vertice} correspondiente.
     */
    public Vertice getVerticePorIndice(int i){ return this.vertices.get(i);}
    
    /** @return La latitud máxima encontrada. */
    public double getMaxLat() {
        return maxLat;
    }

    public double getMinLat() {
        return minLat;
    }

    public double getMinLon() {
        return minLon;
    }

    public double getMaxLon() {
        return maxLon;
    }
}
    
