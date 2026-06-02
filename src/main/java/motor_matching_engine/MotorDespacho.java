package motor_matching_engine;

import java.util.Random;

import contenedores.*;
import Grafos.*;

//import Grafo.*;

public class MotorDespacho {
    private GrafoMapa miGrafo;
    public MotorDespacho(GrafoMapa grafo){
        this.miGrafo = grafo;
    }

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
            System.out.println("Vehiculo elegido: "+elegido.getID()+" Asignado con ETA: "+ elegido.getETA());
            return new ResultadoDespacho(elegido, colaDePrioridad);
        }
        return new ResultadoDespacho(null, colaDePrioridad);
    }
    public Usuario usuarioRandom(){
        Usuario generado; Random posRandom;int posO,posD;
        posRandom = new Random();
        posD = posRandom.nextInt(this.miGrafo.getOrden());
        posO = posRandom.nextInt(this.miGrafo.getOrden());
        generado = new Usuario(posRandom.nextInt(), posO,posD);
        return generado;
    }
    public Vehiculo vehiculoRandom(){
        Vehiculo generado; Random posRandom;int posO;
        posRandom = new Random();
        posO = posRandom.nextInt(this.miGrafo.getOrden());
        generado = new Vehiculo(posRandom.nextInt(), posO);
        return generado;
    }
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
