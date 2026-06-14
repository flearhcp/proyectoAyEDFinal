package motor_matching_engine;

/**
 * Representa a un pasajero en el sistema que solicita un servicio de transporte.
 * Almacena la información de identificación y los puntos geográficos (vértices)
 * de inicio y fin de su trayecto.
 */
public class Usuario {
    /** Identificador único del usuario. */
    private int ID;
    /** Índice del vértice en el grafo que representa el punto de partida. */
    private int verticeIDOrigen;
    /** Índice del vértice en el grafo que representa el destino final. */
    private int verticeIDDestino;

    /**
     * Constructor para la clase Usuario.
     * @param ID Identificador único.
     * @param verticeOrigen Índice del vértice de origen.
     * @param verticeDestino Índice del vértice de destino.
     */
    public Usuario(int ID,int verticeOrigen, int verticeDestino){
        this.ID = ID;
        this.verticeIDOrigen = verticeOrigen;
        this.verticeIDDestino = verticeDestino;
    }

    /** @return El ID del usuario. */
    public int getID() {
        return ID;
    }

    /** @param iD El nuevo ID a asignar. */
    public void setID(int iD) {
        ID = iD;
    }

    /**
     * Obtiene el vértice donde el usuario espera ser recogido.
     * @return Índice del vértice de origen.
     */
    public int getVerticeIDOrigen() {
        return verticeIDOrigen;
    }

    /**
     * Define el punto de partida del usuario.
     * @param verticeIDOrigen Índice del vértice de origen.
     */
    public void setVerticeIDOrigen(int verticeIDOrigen) {
        this.verticeIDOrigen = verticeIDOrigen;
    }

    /**
     * Obtiene el vértice de destino del viaje.
     * @return Índice del vértice de destino.
     */
    public int getVerticeIDDestino() {
        return verticeIDDestino;
    }

    public void setVerticeIDDestino(int verticeIDDestino) {
        this.verticeIDDestino = verticeIDDestino;
    }
    
}
