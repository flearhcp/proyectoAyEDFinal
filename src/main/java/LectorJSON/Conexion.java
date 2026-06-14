package LectorJSON;

/**
 * Representa una conexión lógica entre dos índices de vértices en el grafo.
 * Utilizada durante el procesamiento de datos para mapear rutas.
 */
public class Conexion {
    /** Índice del vértice origen de la conexión. */
    private int origen;
    /** Índice del vértice destino de la conexión. */
    private int destino;

    /**
     * Constructor para Conexion.
     * @param origen Índice del vértice de origen.
     * @param destino Índice del vértice de destino.
     */
    public Conexion(int origen, int destino) {
        this.origen = origen;
        this.destino = destino;
    }

    /** @return El índice del vértice origen. */
    public int getOrigen() {
        return origen;
    }

    /** @return El índice del vértice destino. */
    public int getDestino() {
        return destino;
    }

    /**
     * @return Representación textual de la conexión.
     */
    @Override
    public String toString() {
        return origen + " -> " + destino;
    }
}
