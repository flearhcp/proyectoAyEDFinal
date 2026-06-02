package Test;

import contenedores.*;
public class App {
    public static void main(String[] args) throws Exception {
        ColaPrioridad<String> colaPrueba = new ColaPrioridad<>();
        //Integer[] lista = {10,211,3,5,23};
        String[] lista = {"Y ella","Mamita","Papito","Helga","Mauro"};
        for (int i = 0; i < 5; i++) {
            colaPrueba.meter(lista[i]);
        }
        System.out.println("Cola: ");
        while(!colaPrueba.estaVacia()){
            System.out.println(" "+ colaPrueba.sacar());
        }
    }
}
