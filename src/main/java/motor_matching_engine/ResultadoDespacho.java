package motor_matching_engine;

import contenedores.ColaPrioridad;

public class ResultadoDespacho {
    private Vehiculo vehiculoAsignado;
    private ColaPrioridad<Vehiculo> vehiculosCandidatos;

    public ResultadoDespacho(Vehiculo vehiculoAsignado, ColaPrioridad<Vehiculo> vehiculosCandidatos) {
        this.vehiculoAsignado = vehiculoAsignado;
        this.vehiculosCandidatos = vehiculosCandidatos;
    }

    public Vehiculo getVehiculoAsignado() {
        return vehiculoAsignado;
    }

    public ColaPrioridad<Vehiculo> getVehiculosCandidatos() {
        return vehiculosCandidatos;
    }
}