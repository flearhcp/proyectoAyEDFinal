package LectorJSON;

import java.util.ArrayList;
import java.util.List;

public class Calle {

    private long osmId;
    private String nombre;
    private String shortName;
    private boolean manoUnica;
    private int velocidadMaxima;
    private String tipo;
    private List<Coordenada> geometria;

    public Calle( long osmId,String nombre, String shortName, boolean manoUnica,int velocidadMaxima,String tipo) {
        this.osmId = osmId;
        this.nombre = nombre;
        this.shortName = shortName;
        this.manoUnica = manoUnica;
        this.velocidadMaxima = velocidadMaxima;
        this.tipo = tipo;
        geometria = new ArrayList<>();
    }

    public long getOsmID(){
        return this.osmId;
    }
    
    public String getNombre() {
        return nombre;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * Devuelve el nombre corto si existe, de lo contrario devuelve el nombre completo.
     */
    public String getNombreAMostrar() {
        if (shortName != null && !shortName.isEmpty()) {
            return shortName;
        }
        return nombre;
    }

    public boolean esManoUnica() {
        return manoUnica;
    }

    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }

    public String getTipo(){
        return this.tipo;
    }
    public List<Coordenada> getGeometria(){
        return this.geometria;
    }
/*     private String nombre;
    private String tipo;
    private boolean manoUnica;

    private List<Coordenada> puntos;

    public Calle(
            String nombre,
            String tipo,
            boolean manoUnica) {

        this.nombre = nombre;
        this.tipo = tipo;
        this.manoUnica = manoUnica;

        puntos = new ArrayList<>();
    }

    public void agregarPunto(Coordenada c) {

        puntos.add(c);
    }

    @Override
    public String toString() {

        return "Calle{" +
                "nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", manoUnica=" + manoUnica +
                ", puntos=" + puntos.size() +
                '}';
    } */
}
