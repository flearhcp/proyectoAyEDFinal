package LectorJSON;

import java.util.List;


public class DatosMapa {
    private List<Vertice> vertices;
    private List<Arista> aristas;
    private double minLat, maxLat, minLon, maxLon;

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

    public int getCantidadVertices() {
        return vertices.size();
    }

    public List<Vertice> getVertices () {
        return this.vertices;
    }
    
    public List<Arista> getAristas () {
        return this.aristas;
    }
    public Vertice getVerticePorIndice(int i){ return this.vertices.get(i);}
    
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
    
