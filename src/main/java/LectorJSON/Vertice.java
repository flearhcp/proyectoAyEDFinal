package LectorJSON;

import java.util.ArrayList;
import java.util.List;

public class Vertice {
    private int indice;
    private long osmId;
    private Coordenada coordenada;
    private List<Calle> calles;
    private int cantCalles;
    public Vertice(int indice,long osmId,Coordenada coordenada) {
        this.indice = indice;
        this.osmId = osmId;
        this.coordenada = coordenada;
        this.calles = new ArrayList<>(); 
        this.cantCalles=0;
    }

    public void agregarCalle(Calle calle) { //falta verificar repetidas antes de agregar
        calles.add(calle);
        cantCalles++;
    }

    public int getIndice() {
        return indice;
    }

    public Coordenada getCoordenada (){
        return this.coordenada;
    }
    public double getLatitud() {
        return coordenada.getLatitud();
    }

    public double getLongitud() {
        return coordenada.getLongitud();
    }

    public double getOsmID (){
        return osmId;
    }
    
    public int getCantidadCalles(){
        return cantCalles;
    }
}
