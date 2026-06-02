package LectorJSON;

import java.util.List;


public class DatosMapa {
    private List<Vertice> vertices;
    private List<Arista> aristas;

    public DatosMapa(List<Vertice> vertices,List<Arista> aristas) {
        this.vertices = vertices;
        this.aristas = aristas;
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
        double max;
        max = -999999;
        for (Vertice vertice : vertices) {
            if( vertice.getLatitud() > max){
                max = vertice.getLatitud();
            }
        }
        return max;
    }

    public double getMinLat() {
        double min;
        min = 999999;
        for (Vertice vertice : vertices) {
            if(vertice.getLatitud() < min){
                min = vertice.getLatitud();
            }
        }
        return min;
    }

    public double getMinLon() {
        double min;
        min = 99999;
        for (Vertice v : vertices) {
            if(v.getLongitud() < min){
                min = v.getLongitud();
            }
        }
        return min;
    }

    public double getMaxLon() {
        double max;
        max = -999999;
        for (Vertice v : vertices) {
            if(v.getLongitud() > max)
                max = v.getLongitud();
        }
        return max;
    }

}
    

