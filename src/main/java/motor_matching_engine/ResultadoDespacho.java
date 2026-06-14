package motor_matching_engine;

import contenedores.ColaPrioridad;

/**
 * Clase envoltorio (Wrapper) que contiene los resultados de una operación de despacho.
 * Provee el vehículo óptimo asignado y una lista de alternativas cercanas.
 */
public class ResultadoDespacho {
    /** El vehículo seleccionado como la mejor opción para el viaje. */
    private Vehiculo vehiculoAsignado;
    /** Una cola de prioridad con otros vehículos disponibles ordenados por cercanía. */
    private ColaPrioridad<Vehiculo> vehiculosCandidatos;

    /**
     * Constructor de ResultadoDespacho.
     * @param vehiculoAsignado El vehículo con menor ETA.
     * @param vehiculosCandidatos Cola con el resto de la flota disponible.
     */
    public ResultadoDespacho(Vehiculo vehiculoAsignado, ColaPrioridad<Vehiculo> vehiculosCandidatos) {
        this.vehiculoAsignado = vehiculoAsignado;
        this.vehiculosCandidatos = vehiculosCandidatos;
    }

    /**
     * @return El vehículo asignado al viaje, o {@code null} si no se encontró ninguno.
     */
    public Vehiculo getVehiculoAsignado() {
        return vehiculoAsignado;
    }

    /**
     * @return La cola de prioridad con los candidatos alternativos.
     */
    public ColaPrioridad<Vehiculo> getVehiculosCandidatos() {
        return vehiculosCandidatos;
    }
}