package Grafos;
import contenedores.*;
import recursos.*;

public abstract class AbsGrafoD extends AbsGrafo implements OperacionesGD{
	
	/** Matriz de costos para el algoritmo de Floyd-Warshall. */
	protected MatrizGrafo matrizCostoF,matrizCaminoF;

	/**
	 * Clase interna de apoyo para ordenar las evaluaciones en el algoritmo de Dijkstra.
	 * Implementa {@code Comparable} para permitir su uso en una cola de prioridad.
	 */
	protected static class NodoDistancia implements Comparable<NodoDistancia> {
		/** El vértice representado por este nodo. */
		public int vertice;
		/** La distancia acumulada hasta este vértice desde el origen. */
		public double distancia;
		
		/**
		 * Constructor para NodoDistancia.
		 * @param vertice El índice del vértice.
		 * @param distancia La distancia acumulada hasta el vértice.
		 */
		public NodoDistancia(int vertice, double distancia) {
			this.vertice = vertice;
			this.distancia = distancia;
		}
		/**
		 * Compara este NodoDistancia con otro basándose en sus distancias.
		 * @param otro El otro NodoDistancia con el que comparar.
		 * @return Un valor negativo si esta distancia es menor, cero si son iguales, o un valor positivo si es mayor.
		 */
		@Override
		public int compareTo(NodoDistancia otro) {
			return Double.compare(this.distancia, otro.distancia);
		}
	}

	/**
	 * Constructor para AbsGrafoD.
	 * @param ordenGrafo El número de vértices en el grafo.
	 */
	public AbsGrafoD(int ordenGrafo){
		super(ordenGrafo);
	}
		
	/**
	 * Método abstracto para cargar el grafo. Debe ser implementado por las subclases.
	 */
	public abstract void cargarGrafo();
	
	/**
	 * Muestra los costos y caminos más cortos desde un vértice de inicio
	 * a todos los demás vértices utilizando el algoritmo de Dijkstra.
	 * Imprime el costo y un camino reconstruido para cada vértice alcanzable.
	 *
	 * @param startVertex El vértice de inicio para el cálculo de Dijkstra.
	 */
	public void muestraDijkstra(int startVertex){
		double currCost; int w;
		
		ResultadoDijkstra resultado = Dijkstra(startVertex);
		
		for (int v=0; v<getOrden();v++){
			System.out.println("vertice " + v);
			if (v!=startVertex){
				currCost=(double)resultado.getDistancias().devolver(v);
				System.out.println("costo desde " + startVertex + " a " + v + "->" + currCost);
			
				System.out.println("mostrando un camino desde "+ v + " a " + startVertex);
				
				w=(int)resultado.getCaminos().devolver(v);
				
				do{
					System.out.println("camino " + w);
					w=(int)resultado.getCaminos().devolver(w);
				}while(w!=-1);//recordemos que al inicializar cambiamos todos los -1 salvo el startVertex
			}			
		}
		
	}

	/**
	 * Implementa el algoritmo de Dijkstra para encontrar los caminos más cortos
	 * desde un vértice de inicio dado a todos los demás vértices en el grafo.
	 *
	 * @param startVertex El vértice de inicio para el algoritmo.
	 * @return Un objeto {@code ResultadoDijkstra} que contiene las distancias,
	 *         los caminos y los vértices visitados.
	 */
	public ResultadoDijkstra Dijkstra(int startVertex){
		int n = getOrden();
		// Usamos arreglos primitivos para velocidad O(1) en acceso
		double[] distancias = new double[n];
		int[] caminos = new int[n];
		boolean[] visitado = new boolean[n];
		
		ColaPrioridad<NodoDistancia> pq = new ColaPrioridad<>();
		//Inicializamos las listas.
		for (int i=0; i < n; i++){			
			distancias[i] = infinito;
			caminos[i] = -1;
			visitado[i] = false;
		}
		
		// El costo de llegar al punto de inicio es 0
		
		distancias[startVertex] = 0.0;
		pq.meter(new NodoDistancia(startVertex, 0.0));
		
		// 3. Algoritmo de Dijkstra
		while (!pq.estaVacia()) {
			NodoDistancia actual = pq.sacar();
			int minVertex = actual.vertice;
			
			// Si el vértice ya fue procesado, lo descartamos
			if (visitado[minVertex]) {
				continue;
			}
			// Marcamos el vértice como procesado (agregado al conjunto solución)
			visitado[minVertex] = true;
			
			// Evaluamos todos sus adyacentes a través de la matriz de costo			
			for (int v = 0; v < n; v++) {
				if (!visitado[v]) {// Si el vecino no ha sido visitado
					double arcCost = (double) this.matrizCosto.devolver(minVertex, v);
					// Con matriz de adyacencia, debemos escanear todos los posibles vecinos
					if (arcCost != infinito) { // Si existe una conexión entre ellos
						// Relajación de Dijkstra
						if (distancias[minVertex] + arcCost < distancias[v]) {
							distancias[v] = distancias[minVertex] + arcCost;
							caminos[v] = minVertex;
							pq.meter(new NodoDistancia(v, distancias[v]));
						}
					}
				}
			}
		}
		// Convertimos los arreglos de vuelta a las listas custom para el objeto Resultado
		ListaDoubleLinkedL listaDistancia = new ListaDoubleLinkedL();
		ListaDoubleLinkedL listaCamino = new ListaDoubleLinkedL();
		ListaDoubleLinkedL listaSolucion = new ListaDoubleLinkedL();
		for(int i=0; i < n; i++){
			listaDistancia.insertar(distancias[i], i);
			listaCamino.insertar(caminos[i], i);
			listaSolucion.insertar(visitado[i] ? i : -1, i);
		}
		
		return new ResultadoDijkstra(listaDistancia, listaCamino, listaSolucion);
	}
	
	/**
	 * Muestra el grafo imprimiendo los costos de las aristas existentes
	 * entre cada par de vértices.
	 */
	public void muestraGrafo(){
		double currCost;
		for (int i=0; i<getOrden();i++){
			for (int j=0; j<getOrden();j++){
				if (i!=j){
					currCost=(double)this.matrizCosto.devolver(i, j);
					if (currCost!=infinito){
						System.out.println("costo " + i + " a " + j + "->" + currCost);
					}				
				}
			}			
		}		
	}
	
	
	
	
}
