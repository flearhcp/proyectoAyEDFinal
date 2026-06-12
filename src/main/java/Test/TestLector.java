package Test;

import java.util.List;
import LectorJSON.*;

/**
 * Clase de prueba para verificar el funcionamiento del {@code LectorJson}.
 */
public class TestLector {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DEL LECTOR JSON ===");
        
        try {
            // 1. Instancias tu lector (pásale la ruta de tu archivo JSON real de Overpass)
            // Asumo que tu constructor recibe la ruta del archivo o el JSONObject directamente
            LectorJson lector = new LectorJson("Archivos Muestra Salta - Centro 5 Cuadras --20260518\\centroSalta.json"); 
            
            // 2. Procesas el JSON y recuperas el contenedor de datos
            System.out.println("\nProcesando el archivo JSON...");
            DatosMapa datos = lector.generarDatosMapa();
            System.out.println("¡Procesamiento completado con éxito!");
            
            // 3. Extraemos las listas empaquetadas
            List<Vertice> listaVertices = datos.getVertices();
            List<Arista> listaAristas = datos.getAristas();
            
            // --- REPORTE DE VÉRTICES ---
            System.out.println("\n=======================================");
            System.out.println("REPORTE DE VÉRTICES DETECTADOS (" + datos.getCantidadVertices() + ")");
            System.out.println("=======================================");
            
            for (Vertice v : listaVertices) {
                System.out.print("Vértice Índice: [" + v.getIndice() + "]");
                System.out.print(" | OSM ID: " + v.getOsmID());
                
                // Controlamos si la coordenada es la de seguridad (inválida) o una real
                if (v.getCoordenada().esValida()) {
                    System.out.println(" | Coord: (" + v.getCoordenada().getLatitud() + ", " + v.getCoordenada().getLongitud() + ")");
                } else {
                    System.out.println(" | Coord: [⚠️ FALTANTE / INVALIDA]");
                }
            }
            
            // --- REPORTE DE ARISTAS ---
            System.out.println("\n=======================================");
            System.out.println("REPORTE DE CONEXIONES (ARISTAS) DETECTADAS (" + listaAristas.size() + ")");
            System.out.println("=======================================");
            
            for (Arista a : listaAristas) {
                Vertice orig = a.getOrigen();
                Vertice dest = a.getDestino();
                Calle calle = a.getCalle();
                
                System.out.print("Arista: Centro en Índice " + orig.getIndice() + " hacia Índice " + dest.getIndice());
                System.out.print(" | Calle: " + calle.getNombre());
                System.out.print(" | Velocidad: " + calle.getVelocidadMaxima() + " km/h");
                System.out.println(" | Tipo: " + calle.getTipo());
            }
            
            System.out.println("\n=======================================");
            System.out.println("=== PRUEBA FINALIZADA CORRECTAMENTE ===");
            System.out.println("=======================================");

        } catch (Exception e) {
            System.out.println("\n❌ Ocurrió un error durante la prueba:");
            e.printStackTrace();
        }
    }
 
}
