package Grafos;

import contenedores.ListaDoubleLinkedL;

public class ResultadoDijkstra {
    private ListaDoubleLinkedL distancias;
    private ListaDoubleLinkedL caminos;
    private ListaDoubleLinkedL solucion;

    public ResultadoDijkstra(ListaDoubleLinkedL distancias, ListaDoubleLinkedL caminos, ListaDoubleLinkedL solucion) {
        this.distancias = distancias;
        this.caminos = caminos;
        this.solucion = solucion;
    }

    public ListaDoubleLinkedL getDistancias() { return distancias; }
    
    public ListaDoubleLinkedL getCaminos() { return caminos; }
    
    public ListaDoubleLinkedL getSolucion() { return solucion; }
}