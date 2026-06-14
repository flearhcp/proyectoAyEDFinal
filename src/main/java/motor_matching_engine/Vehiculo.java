package motor_matching_engine;

import java.util.Random;

/**
 * Representa un vehículo dentro de la flota del sistema.
 * Implementa {@code Comparable} para permitir que las colas de prioridad los ordenen
 * automáticamente según su tiempo estimado de llegada (ETA).
 */
public class Vehiculo implements Comparable<Vehiculo> {
    /** Patente o identificador alfanumérico generado aleatoriamente. */
    private String ID; 
    /** Identificador OSM del vértice (actualmente no utilizado para cálculos directos). */
    private long IDVertice;
    /** Índice del vértice en el grafo donde se encuentra el vehículo actualmente. */
    private int verticeIndiceOrigen;
    /** Tiempo estimado de llegada al objetivo en unidades de costo del grafo. */
    private double ETA;
    /** Estado de disponibilidad del vehículo para aceptar nuevos viajes. */
    private boolean disponible;

    /**
     * Constructor de Vehiculo. Genera automáticamente una patente aleatoria.
     * @param ID Identificador numérico base (se usa internamente para la lógica de creación).
     * @param posOrigen Índice del vértice inicial en el mapa.
     */
    public Vehiculo(int ID, int posOrigen){
        this.ID = setID();
        this.verticeIndiceOrigen = posOrigen;
        this.ETA = 0.0;
        this.disponible = true;
    }

    /**
     * Actualiza el valor de ETA tras un cálculo de Dijkstra.
     * @param ETA Nuevo valor de tiempo estimado.
     */
    public void setActualEta(double ETA){
        this.ETA = ETA;
    }

    /**
     * Compara este vehículo con otro basándose en el ETA.
     * @param otro El otro vehículo a comparar.
     * @return Un valor negativo si este vehículo llega antes, positivo si llega después.
     */
    @Override
    public int compareTo(Vehiculo otro){return Double.compare(this.ETA, otro.ETA);}

    /** @return La patente del vehículo. */
    public String getID() {return this.ID;}
    
    /** @return El ID de vértice OSM. */
    public long getIDvertice(){return this.IDVertice;}
    
    /** @return El índice del vértice actual en el grafo. */
    public int getVerticeIndiceOrigen() {return this.verticeIndiceOrigen;}
    
    /** @return El valor de costo/tiempo acumulado (ETA). */
    public double getETA() {return this.ETA;}
    
    /** @return true si el vehículo puede recibir viajes. */
    public boolean getDisponible(){return this.disponible;}
    
    /** @param disponible El nuevo estado de disponibilidad. */
    public void setDisponible(boolean disponible) {this.disponible = disponible;}
    
    /** @param posOrigen Actualiza la posición del vehículo en el mapa. */
    public void setVerticeIndiceOrigen(int posOrigen) {this.verticeIndiceOrigen = posOrigen;}

    /**
     * Genera un identificador de patente aleatorio con formato "AAA 111".
     * @return Una cadena con la patente generada.
     */
    private String setID(){
        StringBuilder pat; Random ran; int numran; char letter;
        pat = new StringBuilder();
        ran = new Random();
        for (int i = 0; i < 3; i++) {
            letter = (char)('A'+ran.nextInt(25));
            pat.append(letter);
        }
        pat.append(" ");
        for (int i = 0; i < 3; i++) {
            numran = ran.nextInt(10);
            pat.append(numran);
        }
        return pat.toString();
    }

    /**
     * Convierte el valor de ETA (costo) a un formato de tiempo legible.
     * Asume una conversión basada en la escala de velocidad del mapa.
     * @return Una cadena formateada como "MM:SS min".
     */
    public String getTiempoEspera(){
        double tiempoSegundos;
        int min,seg;
        tiempoSegundos = this.ETA / 11.11;
        min = (int)tiempoSegundos / 60;
        seg = (int)tiempoSegundos % 60;
        return String.format("%02d:%02d min",min,seg);
    }
}
