package motor_matching_engine;

import java.util.Random;

import contenedores.*;
import Grafos.*;

//import Grafo.*;

/**
 * Clase encargada de la lógica de negocio para el emparejamiento entre usuarios y vehículos.
 * Utiliza el grafo del mapa para calcular tiempos estimados de llegada (ETA) y seleccionar
 * el vehículo más adecuado para un viaje.
 */
public class MotorDespacho {
    /** Referencia al grafo que representa el mapa de la ciudad. */
    private GrafoMapa miGrafo;

    /**
     * Constructor del motor de despacho.
     * @param grafo El grafo con la información de calles y costos.
     */
    public MotorDespacho(GrafoMapa grafo){
        this.miGrafo = grafo;
    }

    /**
     * Gestiona el despacho de un viaje buscando el vehículo disponible más cercano al pasajero.
     * El método calcula el costo de camino mínimo (Dijkstra) desde la posición de cada vehículo
     * disponible hasta el origen del pasajero y los ordena en una cola de prioridad.
     *
     * @param pasajero El objeto {@code Usuario} que solicita el viaje.
     * @param flota Una {@code ListaDoubleLinkedL} con los vehículos registrados en el sistema.
     * @return Un objeto {@code ResultadoDespacho} que contiene el vehículo asignado (el de menor ETA)
     *         y una cola de candidatos alternativos.
     */
    public ResultadoDespacho despacharViaje(Usuario pasajero,ListaDoubleLinkedL flota){
        ColaPrioridad<Vehiculo> colaDePrioridad = new ColaPrioridad<>();
        Vehiculo elegido,aux;
        for (int i = 0; i < flota.tamanio(); i++) {
            aux = (Vehiculo)flota.devolver(i);
            if (aux.getDisponible()) {
                double eta = miGrafo.calcularCostoDijkstra(aux.getVerticeIndiceOrigen(),pasajero.getVerticeIDOrigen());
                aux.setActualEta(eta);
                colaDePrioridad.meter(aux);
            }
        }
        if(!colaDePrioridad.estaVacia()){ //Aca deberia estar la opcion si el conductor acepta
            elegido = colaDePrioridad.sacar();
            elegido.setDisponible(false); // Lo marcamos como ocupado
            //System.out.println("Vehiculo elegido: "+elegido.getID()+" Asignado con ETA: "+ elegido.getETA());
            return new ResultadoDespacho(elegido, colaDePrioridad);
        }
        return new ResultadoDespacho(null, colaDePrioridad);
    }

    /**
     * Genera un usuario con ubicación de origen y destino aleatoria dentro del mapa.
     * @return Un nuevo objeto {@code Usuario} con datos aleatorios.
     */
    public Usuario usuarioRandom(){
        Usuario generado; Random posRandom;int posO,posD;
        posRandom = new Random();
        posD = posRandom.nextInt(this.miGrafo.getOrden());
        posO = posRandom.nextInt(this.miGrafo.getOrden());
        generado = new Usuario(posRandom.nextInt(), posO,posD);
        return generado;
    }

    /**
     * Genera un vehículo con una ubicación inicial aleatoria en el mapa.
     * @return Un nuevo objeto {@code Vehiculo} con ubicación aleatoria y patente autogenerada.
     */
    public Vehiculo vehiculoRandom(){
        Vehiculo generado; Random posRandom;int posO;
        posRandom = new Random();
        posO = posRandom.nextInt(this.miGrafo.getOrden());
        generado = new Vehiculo(posRandom.nextInt(), posO);
        return generado;
    }

    /**
     * Crea una flota inicial de prueba consistente en 5 vehículos posicionados aleatoriamente.
     * @return Una {@code ListaDoubleLinkedL} conteniendo la flota de vehículos.
     */
    public ListaDoubleLinkedL generaFlota(){
        ListaDoubleLinkedL lista = new ListaDoubleLinkedL();
        Vehiculo generado;
        for (int i = 0; i < 5; i++) {
            generado = this.vehiculoRandom();
            lista.insertar(generado, i);
        }
        return lista;
    }
}
