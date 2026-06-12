package motor_matching_engine;

import java.util.Random;

public class Vehiculo implements Comparable<Vehiculo> {
    private String ID; //Puede ser patente
    private long IDVertice;
    private int verticeIndiceOrigen;
    private double ETA;
    private boolean disponible; //Disponibilidad

    public Vehiculo(int ID, int posOrigen){
        this.ID = setID();
        this.verticeIndiceOrigen = posOrigen;
        this.ETA = 0.0;
        this.disponible = true;
    }
    public void setActualEta(double ETA){
        this.ETA = ETA;
    }

    @Override
    public int compareTo(Vehiculo otro){return Double.compare(this.ETA, otro.ETA);}

    public String getID() {return this.ID;}
    
    public long getIDvertice(){return this.IDVertice;}
    
    public int getVerticeIndiceOrigen() {return this.verticeIndiceOrigen;}
    
    public double getETA() {return this.ETA;}
    
    public boolean getDisponible(){return this.disponible;}
    
    public void setDisponible(boolean disponible) {this.disponible = disponible;}
    
    public void setVerticeIndiceOrigen(int posOrigen) {this.verticeIndiceOrigen = posOrigen;}

    private String setID(){
        StringBuilder pat; Random ran; int numran; char letter;
        pat = new StringBuilder();
        ran = new Random();
        for (int i = 0; i < 3; i++) {
            letter = (char)('A'+ran.nextInt(25));
            pat.append(letter);
        }
        pat.append(" ");
        for (int i = 0; i < 3; i++) {
            numran = ran.nextInt(10);
            pat.append(numran);
        }
        return pat.toString();
    }
    public String getTiempoEspera(){
        double tiempoSegundos;
        int min,seg;
        tiempoSegundos = this.ETA / 11.11;
        min = (int)tiempoSegundos / 60;
        seg = (int)tiempoSegundos % 60;
        return String.format("%02d:%02d min",min,seg);
    }
}
