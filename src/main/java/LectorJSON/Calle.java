package LectorJSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una vía o calle del mapa, extrayendo sus propiedades desde OpenStreetMap.
 * Contiene información sobre el sentido de circulación, velocidad y tipo de camino.
 */
public class Calle {

    /** Identificador único de la vía en OpenStreetMap. */
    private long osmId;
    /** Nombre completo de la calle. */
    private String nombre;
    /** Nombre abreviado de la calle si está disponible. */
    private String shortName;
    /** Indica si la calle es de sentido único (true) o doble mano (false). */
    private boolean manoUnica;
    /** Límite de velocidad permitido en km/h. */
    private int velocidadMaxima;
    /** Clasificación de la vía según OpenStreetMap (ej. "residential", "primary"). */
    private String tipo;
    /** Lista de coordenadas que definen la forma geométrica de la calle. */
    private List<Coordenada> geometria;

    /**
     * Constructor para la clase Calle.
     * @param osmId Identificador OSM del camino.
     * @param nombre Nombre de la vía.
     * @param shortName Abreviatura del nombre.
     * @param manoUnica Booleano que indica si es sentido único.
     * @param velocidadMaxima Velocidad máxima en km/h.
     * @param tipo Categoría de la carretera.
     */
    public Calle( long osmId,String nombre, String shortName, boolean manoUnica,int velocidadMaxima,String tipo) {
        this.osmId = osmId;
        this.nombre = nombre;
        this.shortName = shortName;
        this.manoUnica = manoUnica;
        this.velocidadMaxima = velocidadMaxima;
        this.tipo = tipo;
        geometria = new ArrayList<>();
    }

    /** @return El ID de OpenStreetMap. */
    public long getOsmID(){
        return this.osmId;
    }
    
    /** @return El nombre completo de la calle. */
    public String getNombre() {
        return nombre;
    }

    /** @return El nombre corto de la calle. */
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

    /** @return true si es mano única, false si es doble mano. */
    public boolean esManoUnica() {
        return manoUnica;
    }

    /** @return La velocidad máxima permitida. */
    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }

    /** @return El tipo de vía (highway tag). */
    public String getTipo(){
        return this.tipo;
    }
    /** @return La lista de coordenadas que componen la geometría de la calle. */
    public List<Coordenada> getGeometria(){
        return this.geometria;
    }

}
