package LectorJSON;

/**
 * Representa un punto geográfico definido por su latitud y longitud.
 * Provee métodos para validación y comparación de ubicaciones espaciales.
 */
public class Coordenada {
    /** Latitud en grados decimales. */
    private double latitud;
    /** Longitud en grados decimales. */
    private double longitud;
    /** Constante que representa una coordenada no válida para manejo de errores. */
    public static final Coordenada INVALIDA = new Coordenada (999.0,999.0);

    /**
     * Constructor de Coordenada.
     * @param latitud Latitud geográfica.
     * @param longitud Longitud geográfica.
     */
    public Coordenada(double latitud, double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /** @return La latitud. */
    public double getLatitud() {
        return latitud;
    }

    /** @return La longitud. */
    public double getLongitud() {
        return longitud;
    }

    /**
     * Verifica si la coordenada posee valores geográficos realistas.
     * @return true si la coordenada no es la constante INVALIDA.
     */
    public boolean esValida (){
        return this.latitud != 999.0 && this.longitud != 999.0;
    }

    /**
     * Devuelve una representación en cadena de la coordenada.
     * @return Cadena formateada como "(latitud, longitud)".
     */
    @Override
    public String toString() {
        return "(" + latitud + ", " + longitud + ")";
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Coordenada)) {
            return false;
        }

        Coordenada otra = (Coordenada) obj;

        return this.latitud == otra.latitud &&
               this.longitud == otra.longitud;
    }
}
