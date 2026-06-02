package Test;

import LectorJSON.*;
import GUI.ControladorGUI;
import Grafos.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        String ruta = "D:/Santi/Materias 2do/AyED/TPI FINAL/proyectoAyEDFinal/src/main/resources/5Cuadras.json";
        LectorJson lector = new LectorJson(ruta);
        DatosMapa datos = lector.generarDatosMapa();
        GrafoMapa grafo = new GrafoMapa(datos.getCantidadVertices(), datos);
        grafo.cargarGrafo();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventana.fxml"));
        Parent root = loader.load(); 
        
        ControladorGUI controlador = loader.getController();
        controlador.setGrafoyDatos(grafo,datos);
        
        primaryStage.setTitle("Sistema de despacho ETA - Salta MacroCentro");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
