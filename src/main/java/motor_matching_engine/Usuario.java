package motor_matching_engine;

public class Usuario {
    private int ID;
    private int verticeIDOrigen;
    private int verticeIDDestino;

    public Usuario(int ID,int verticeOrigen, int verticeDestino){
        this.ID = ID;
        this.verticeIDOrigen = verticeOrigen;
        this.verticeIDDestino = verticeDestino;
    }

    public int getID() {
        return ID;
    }

    public void setID(int iD) {
        ID = iD;
    }

    public int getVerticeIDOrigen() {
        return verticeIDOrigen;
    }

    public void setVerticeIDOrigen(int verticeIDOrigen) {
        this.verticeIDOrigen = verticeIDOrigen;
    }

    public int getVerticeIDDestino() {
        return verticeIDDestino;
    }

    public void setVerticeIDDestino(int verticeIDDestino) {
        this.verticeIDDestino = verticeIDDestino;
    }
    
}
