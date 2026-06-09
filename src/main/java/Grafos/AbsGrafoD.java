package Grafos;
import contenedores.*;
import recursos.*;

public abstract class AbsGrafoD extends AbsGrafo implements OperacionesGD{
	
	protected MatrizGrafo matrizCostoF,matrizCaminoF;

	// Clase interna de apoyo para ordenar las evaluaciones en Dijkstra
	protected static class NodoDistancia implements Comparable<NodoDistancia> {
		public int vertice;
		public double distancia;
		
		public NodoDistancia(int vertice, double distancia) {
			this.vertice = vertice;
			this.distancia = distancia;
		}
		@Override
		public int compareTo(NodoDistancia otro) {
			return Double.compare(this.distancia, otro.distancia);
		}
	}

	public AbsGrafoD(int ordenGrafo){
		super(ordenGrafo);
	}
		
	public abstract void cargarGrafo();
	
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
	
		
	/* public ResultadoDijkstra Dijkstra(int startVertex){
		ListaDoubleLinkedL listaDistancia = new ListaDoubleLinkedL();
		ListaDoubleLinkedL listaCamino = new ListaDoubleLinkedL();
		ListaDoubleLinkedL listaSolucion = new ListaDoubleLinkedL();
		ColaPrioridad<NodoDistancia> pq = new ColaPrioridad<>();
		
		for (int i=0; i<getOrden();i++){			
			listaSolucion.insertar(-1, i);
			listaCamino.insertar(-1, i);
			listaDistancia.insertar(infinito, i);
		}
		// El costo de llegar al punto de inicio es 0
		listaDistancia.reemplazar(0.0, startVertex);
		
		pq.meter(new NodoDistancia(startVertex, 0.0));
		
		while (!pq.estaVacia()) {
			NodoDistancia actual = pq.sacar();
			int minVertex = actual.vertice;
			double minCost = actual.distancia;
			
			// Si el vértice ya fue procesado, lo descartamos
			if ((int) listaSolucion.devolver(minVertex) != -1) {
				continue;
			}
			
			// Marcamos el vértice como procesado (agregado al conjunto solución)
			listaSolucion.reemplazar(minVertex, minVertex);
			
			// Evaluamos todos sus adyacentes a través de la matriz de costo
			for (int v = 0; v < getOrden(); v++) {
				if ((int) listaSolucion.devolver(v) == -1) { // Si el vecino no ha sido visitado
					double arcCost = (double) this.matrizCosto.devolver(minVertex, v);
					if (arcCost != infinito) { // Si existe una conexión entre ellos
						double currCost = (double) listaDistancia.devolver(v);
						// Relajación de Dijkstra
						if (minCost + arcCost < currCost) {
							listaDistancia.reemplazar(minCost + arcCost, v);
							listaCamino.reemplazar(minVertex, v);
							pq.meter(new NodoDistancia(v, minCost + arcCost));
						}
					}
				}
			}
		}
		return new ResultadoDijkstra(listaDistancia, listaCamino, listaSolucion);
	} */

	public ResultadoDijkstra Dijkstra(int startVertex){
		int n = getOrden();
		// Usamos arreglos primitivos para velocidad O(1) en acceso
		double[] distancias = new double[n];
		int[] caminos = new int[n];
		boolean[] visitado = new boolean[n];
		
		ColaPrioridad<NodoDistancia> pq = new ColaPrioridad<>();
		
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
