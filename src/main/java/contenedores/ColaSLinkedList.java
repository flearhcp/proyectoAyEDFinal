package contenedores;

import recursos.Nodo;

public class ColaSLinkedList<T> extends ColaLinkedList<T>{

	public void meter(T elemento){
		if (!estaVacia()){
			this.finalC.setNextNodo(new Nodo<T>(elemento));
			this.finalC=this.finalC.getNextNodo();
			// nuevo nodo es el ultimo.
		}else{
			this.frenteC=this.finalC= new Nodo<T>(elemento);	
		}
	}
		
}
