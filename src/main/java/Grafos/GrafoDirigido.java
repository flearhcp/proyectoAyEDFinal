package Grafos;

import java.util.Scanner;

/**
 * Representa un grafo dirigido que extiende {@code AbsGrafoD}.
 * Permite cargar el grafo interactivamente a través de la consola.
 */
public class GrafoDirigido extends AbsGrafoD{
	/**
	 * Constructor para GrafoDirigido.
	 * @param ordenGrafo El número de vértices en el grafo.
	 */
	public GrafoDirigido(int ordenGrafo){
		super(ordenGrafo);
	}
	
	@Override
	/**
	 * Carga el grafo solicitando al usuario los costos de las aristas
	 * a través de la consola. Un costo de -1 se interpreta como infinito (sin conexión).
	 */
	public void cargarGrafo(){
		double currCost;		
		Scanner scanner = new Scanner(System.in);
		
		for (int i=0; i<getOrden();i++){
			for (int j=0;j<getOrden();j++){
				if (i!=j){
					System.out.println("Ingrese costo[" + i + "," + j + "] (sino -1)");
					currCost=scanner.nextDouble();
					if (currCost!=-1){
						this.matrizCosto.actualizar(currCost, i, j);	
					}else{
						this.matrizCosto.actualizar(infinito, i, j);
					}					
				}else{
					this.matrizCosto.actualizar(infinito, i, j);
				}
			}
		}
		scanner.close();
   	
	}
}
