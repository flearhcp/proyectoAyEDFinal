package recursos;

public class Nodo<T> {
	private T nodoInfo;
	private Nodo<T> nextNodo;
	
	public Nodo(T nodoInfo){
		this(nodoInfo,null);
	} 
	
	public Nodo(T nodoInfo, Nodo<T> nextNodo){
		this.nodoInfo=nodoInfo;
		this.nextNodo=nextNodo; 
	}
	public void setNodoInfo(T nodoInfo){
		this.nodoInfo=nodoInfo; 
	}
	
	public void setNextNodo(Nodo<T> nextNodo){
		this.nextNodo=nextNodo; 
	}
	
	public T getNodoInfo(){
		return this.nodoInfo; 
	}
	public Nodo<T> getNextNodo(){
		return this.nextNodo; 
	}
	
	
}
