package contenedores;

import recursos.OperacionesCL1;

import recursos.Nodo;

public abstract class ColaLinkedList <T> implements OperacionesCL1<T>{
	protected Nodo<T> frenteC, finalC;
	
	public ColaLinkedList(){
		limpiar();
	}

	public boolean estaVacia(){
		return (this.frenteC==null);} 


	public abstract void meter(T elemento);
	
	public T sacar(){
		T elemento = null;
		if (!estaVacia()){
			elemento=this.frenteC.getNodoInfo();
			this.frenteC=this.frenteC.getNextNodo();
			if (estaVacia()){
				this.finalC=null; 
			}
		}else{
			System.out.println("Error sacar. Cola vacia");
		}
		return elemento;
	}
	
	public void limpiar(){
		this.frenteC=this.finalC=null;
	}	
	
}
