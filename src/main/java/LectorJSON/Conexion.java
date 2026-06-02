package LectorJSON;

public class Conexion {
    private int origen;
    private int destino;

    public Conexion(int origen, int destino) {

        this.origen = origen;
        this.destino = destino;
    }

    public int getOrigen() {
        return origen;
    }

    public int getDestino() {
        return destino;
    }
        @Override
    public String toString() {

        return origen + " -> " + destino;
    }
}
