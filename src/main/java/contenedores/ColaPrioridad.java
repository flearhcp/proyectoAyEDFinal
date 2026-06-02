package contenedores;

import recursos.Nodo;

public class ColaPrioridad<T extends Comparable<T>> extends ColaLinkedList<T>{
	public boolean esMenor(T objA, T objB){
		return (objA.compareTo(objB) < 0);
	}
	public boolean esMayor(T objA, T objB){
		return (objA.compareTo(objB) > 0);
	}
	public boolean iguales(T objA, T objB){
		return (objA.compareTo(objB) == 0);
	}

	public void meter(T elemento){
		Nodo<T> node;
		node = new Nodo<T>(elemento);
		if (estaVacia()){
			this.frenteC=this.finalC= new Nodo<T>(elemento);
		} else{
			if (esMenor(elemento, this.frenteC.getNodoInfo())){
				node.setNextNodo(this.frenteC);
				this.frenteC=node;

			}else{
				Nodo<T> temp = this.frenteC;
				boolean flag=false;
				while (temp.getNextNodo()!=null && !flag){
					if (esMayor(elemento,temp.getNextNodo().getNodoInfo()) ||
							iguales(elemento,temp.getNextNodo().getNodoInfo())){
						temp=temp.getNextNodo();
					}else{
						flag=true;
					}
				}
				node.setNextNodo(temp.getNextNodo());
				temp.setNextNodo(node);				
			}
		}
	}	
}
