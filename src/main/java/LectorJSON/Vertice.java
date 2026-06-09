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
        if (!calles.contains(calle)){
            calles.add(calle);
            cantCalles++;
        }
    }

    public List<Calle> getCalles() {
        return calles;
    }

    public String getNombreInterseccion(){
        if (calles == null || calles.isEmpty()) {
            return "Intersección sin nombre (ID: " + osmId + ")";
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
