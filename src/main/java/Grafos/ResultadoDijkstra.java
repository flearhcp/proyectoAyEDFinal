package Grafos;

import contenedores.ListaDoubleLinkedL;

/**
 * Clase que encapsula los resultados del algoritmo de Dijkstra.
 * Contiene las distancias más cortas, los caminos (predecesores) y la solución (vértices visitados).
 */
public class ResultadoDijkstra {
    /** Lista de las distancias más cortas desde el vértice de origen a cada otro vértice. */
    private ListaDoubleLinkedL distancias;
    /** Lista de los predecesores en el camino más corto desde el origen a cada vértice. */
    private ListaDoubleLinkedL caminos;
    /** Lista que indica si un vértice fue incluido en la solución (visitado) por Dijkstra. */
    private ListaDoubleLinkedL solucion;

    /**
     * Constructor para ResultadoDijkstra.
     * @param distancias Lista de distancias.
     * @param caminos Lista de caminos (predecesores).
     * @param solucion Lista de vértices en la solución.
     */
    public ResultadoDijkstra(ListaDoubleLinkedL distancias, ListaDoubleLinkedL caminos, ListaDoubleLinkedL solucion) {
        this.distancias = distancias;
        this.caminos = caminos;
        this.solucion = solucion;
    }

    /**
     * Obtiene la lista de distancias.
     * @return La {@code ListaDoubleLinkedL} que contiene las distancias más cortas.
     */
    public ListaDoubleLinkedL getDistancias() { return distancias; }
    
    public ListaDoubleLinkedL getCaminos() { return caminos; }
    
    public ListaDoubleLinkedL getSolucion() { return solucion; }
}