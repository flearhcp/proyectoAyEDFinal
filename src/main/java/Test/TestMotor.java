package Test;

import LectorJSON.*;
import Grafos.*;
import motor_matching_engine.*;
import contenedores.*;

public class TestMotor {
    public static void main(String[] args) {
        try {
            // Se recomienda usar una ruta relativa desde la raíz del proyecto.
            // Asegúrate de que el archivo 'export.json' se encuentre en la ubicación correcta.
            // Por ejemplo, si está en una carpeta 'data' en la raíz del proyecto, la ruta sería "data/export.json".
            // Para este ejemplo, asumiré que está en una carpeta de recursos como en TestLector.
            LectorJson mapa = new LectorJson("D:/AYED_2026/practico_Final/export.json");
            System.out.println("Archivo JSON cargado exitosamente.");
            DatosMapa datos = mapa.generarDatosMapa();
            GrafoMapa grafo = new GrafoMapa(datos.getCantidadVertices(), datos);
            grafo.cargarGrafo();
            MotorDespacho motor = new MotorDespacho(grafo);
            // Generar un usuario aleatorio en el mapa
            Usuario pasajero = motor.usuarioRandom();
            System.out.println("Pasajero generado en vértice: " + pasajero.getVerticeIDOrigen());

            // Generar una flota de vehículos aleatorios
            ListaDoubleLinkedL flota = new ListaDoubleLinkedL();
            for (int i = 0; i < 5; i++) {
                flota.insertar(motor.vehiculoRandom(), i);
            }

            // Despachar el viaje al vehículo más cercano (menor ETA)
            System.out.println("Iniciando despacho de viaje...");
            ResultadoDespacho resultado = motor.despacharViaje(pasajero, flota);
            Vehiculo asignado = resultado.getVehiculoAsignado();

            if (asignado != null) {
                System.out.println("Despacho exitoso.");
            } else {
                System.out.println("No se pudo encontrar un vehículo disponible.");
            }
            
        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
