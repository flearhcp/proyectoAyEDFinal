package Grafos;

import LectorJSON.Arista;
import LectorJSON.DatosMapa;
import contenedores.*;

/**
 * Representa un grafo dirigido específico para mapas, extendiendo {@code AbsGrafoD}.
 * Utiliza datos de mapa (vértices y aristas) para construir y operar sobre el grafo.
 */
public class GrafoMapa extends AbsGrafoD{
	/** Datos del mapa que incluyen vértices y aristas. */
	protected DatosMapa datos;
	/** Lista de adyacencia para representar las conexiones del grafo. */
	private ListaDoubleLinkedL[] listaDeAdyacencia;

	/**
	 * Constructor para GrafoMapa.
	 * @param orden El número de vértices en el grafo.
	 * @param datos Los datos del mapa que se utilizarán para construir el grafo.
	 */
	public GrafoMapa(int orden, DatosMapa datos) {
		super(orden);
		this.datos = datos;
		this.inicializarMapa();
		this.construirListaDeAdyacencia();
	}
	@Override
	/*
	 * Carga el grafo poblando la matriz de costos con los pesos de las aristas
	 * obtenidas de los datos del mapa.
	 */
	public void cargarGrafo(){
		for(Arista arista: this.datos.getAristas()) {
			this.matrizCosto.actualizar(arista.getPeso(), arista.getOrigen().getIndice(), arista.getDestino().getIndice());
		}
	}
	
	/**
	 * Inicializa la matriz de costos del grafo. Establece el costo infinito para aristas no existentes
	 * y cero para la conexión de un vértice consigo mismo.
	 */
	private void inicializarMapa() {
		for(int i = 0; i < this.getOrden(); i++) {
			for(int j = 0; j < this.getOrden(); j++) {
				if(!(i == j)) {
					this.matrizCosto.actualizar(infinito, i, j);
				}else {
					this.matrizCosto.actualizar(0., i, j);
				}
			}
		}
	}
	/**
	 * Construye la lista de adyacencia del grafo a partir de las aristas de los datos del mapa.
	 * Esta lista es utilizada principalmente por {@code ControladorGUI} para dibujar las aristas.
	 */
	private void construirListaDeAdyacencia() {
		this.listaDeAdyacencia = new ListaDoubleLinkedL[this.getOrden()];
		for (int i = 0; i < this.getOrden(); i++) {
			this.listaDeAdyacencia[i] = new ListaDoubleLinkedL();
		}

		int[] tamanos = new int[this.getOrden()];

		for (Arista arista : this.datos.getAristas()) {
			int indiceOrigen = arista.getOrigen().getIndice();
			this.listaDeAdyacencia[indiceOrigen].insertar(arista, tamanos[indiceOrigen]);
			tamanos[indiceOrigen]++;
		}
	}
	/**
	 * Calcula el costo (ETA) desde un vértice de origen a uno de destino utilizando el algoritmo de Dijkstra.
	 * Este método primero ejecuta el algoritmo completo desde el origen y luego devuelve el costo específico
	 * hacia el destino.
	 * @param vertexOr Vértice de origen.
	 * @param vertexDes Vértice de destino.
	 * @return El costo del camino más corto.
	 */
	public double calcularCostoDijkstra(int vertexOr, int vertexDes){
		ResultadoDijkstra resultado = this.Dijkstra(vertexOr);
		return (double)resultado.getDistancias().devolver(vertexDes);
	}
	/**
	 * Calcula el camino de Dijkstra desde la posición del Vehiculo hasta la posición del Pasajero.
	 * El metodo devuelve una Lista enlazada Doble donde se encuentra el camino a recorrer en el grafo.
	 * @param posVehiculo Vértice de origen.
	 * @param posPasajero Vértice de destino.
	 * @return El camino del costo más corto.
	 */
	public ListaDoubleLinkedL caminoDijkstra(int posVehiculo, int posPasajero){
		ListaDoubleLinkedL camino = new ListaDoubleLinkedL();
		int actual,predecesor;
		ResultadoDijkstra resultado = this.Dijkstra(posVehiculo);
		actual = posPasajero;
		camino.insertar(actual, 0);
		predecesor = (int)resultado.getCaminos().devolver(actual);
		while (predecesor != -1) {
			camino.insertar(predecesor, 0);
			actual = predecesor;
			predecesor = (int)resultado.getCaminos().devolver(actual);
		}
		return camino;		
	}
	/**
	 * Obtiene los datos del mapa asociados a este grafo.
	 * @return El objeto {@code DatosMapa} que contiene la información de vértices y aristas.
	 */
	public DatosMapa getDatosMapa(){return this.datos;}
	
	/**
	 * Obtiene la lista de adyacencia para un vértice dado.
	 * Esta lista contiene objetos {@code Arista} que representan las conexiones salientes del vértice.
	 *
	 * @param i El índice del vértice.
	 * @return Una {@code ListaDoubleLinkedL} de aristas adyacentes al vértice {@code i}.
	 */
	public ListaDoubleLinkedL getAdyacentes(int i){return this.listaDeAdyacencia[i];}
	/*
    @Override
    public ResultadoDijkstra Dijkstra(int startVertex) {
        int n = getOrden();
        // Usamos arreglos primitivos para acceso O(1). 
        // Usar ListaDoubleLinkedL aquí dentro causaría lentitud extrema O(N^2).
        double[] distancias = new double[n];
        int[] caminos = new int[n];
        boolean[] visitados = new boolean[n];

        for (int i = 0; i < n; i++) {
            distancias[i] = infinito; // heredado de AbsGrafo
            caminos[i] = -1;
            visitados[i] = false;
        }

        distancias[startVertex] = 0.0;
        ColaPrioridad<NodoDistancia> pq = new ColaPrioridad<>();
        pq.meter(new NodoDistancia(startVertex, 0.0));

        while (!pq.estaVacia()) {
            NodoDistancia actual = pq.sacar();
            int u = actual.vertice;

            if (visitados[u]) continue;
            visitados[u] = true;

            // En lugar de recorrer todo el grafo (O(V)), recorremos solo los ADYACENTES (O(E))
            ListaDoubleLinkedL ady = this.listaDeAdyacencia[u];
            int numAdy = ady.tamanio();
            for (int i = 0; i < numAdy; i++) {
                Arista arco = (Arista) ady.devolver(i);
                int v = arco.getDestino().getIndice();
                
                if (!visitados[v]) {
                    double peso = arco.getPeso();
                    if (distancias[u] + peso < distancias[v]) {
                        distancias[v] = distancias[u] + peso;
                        caminos[v] = u;
                        pq.meter(new NodoDistancia(v, distancias[v]));
                    }
                }
            }
        }

        // Convertimos los resultados de vuelta a las listas requeridas por la cátedra
        // solo al final del algoritmo (una sola vez).
        ListaDoubleLinkedL resDist = new ListaDoubleLinkedL();
        ListaDoubleLinkedL resCam = new ListaDoubleLinkedL();
        ListaDoubleLinkedL resSol = new ListaDoubleLinkedL();

        for (int i = 0; i < n; i++) {
            resDist.insertar(distancias[i], i);
            resCam.insertar(caminos[i], i);
            // El resultado espera el ID del vértice si está en la solución, sino -1
            resSol.insertar(visitados[i] ? i : -1, i);
        }

        return new ResultadoDijkstra(resDist, resCam, resSol);
    }*/
}
