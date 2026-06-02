package LectorJSON;

public class Coordenada {
    private double latitud;
    private double longitud;
    public static final Coordenada INVALIDA = new Coordenada (999.0,999.0);

    public Coordenada(double latitud, double longitud) {

        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public boolean esValida (){
        return this.latitud != 999.0 && this.longitud != 999.0;
    }

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
