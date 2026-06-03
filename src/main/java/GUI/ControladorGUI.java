package GUI;

//Importamos todas las librerias de JavaFX

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

//Otras librerias

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import Grafos.GrafoMapa;
import LectorJSON.*;
import contenedores.*;
import motor_matching_engine.*;

public class ControladorGUI implements Initializable{
    @FXML private Canvas canvasMapa;
    @FXML private ListView<String> listaVehiculos;
    @FXML private ListView<String> listaDespacho;
    private Image imagenFondo;
    private GrafoMapa grafo;
    private MotorDespacho motor;
    private DatosMapa datos;
    private ListaDoubleLinkedL caminoResaltado;
    private ListaDoubleLinkedL flota; // Guardamos la flota a nivel de clase para reutilizarla
    private Usuario pasajeroActual; // Guardamos el pasajero actual para dibujarlo
    
    // Variables para mapear coordenadas del mouse con el Canvas
    private Tooltip tooltipMapa;
    private double mapScale, mapFactorLon, mapMinLon, mapMaxLat;
    private double mapOffsetX;
    private double mapOffsetY;
    //Para zoom
    private double mouseAnchorY;
    private double mouseAnchorX;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double factorZoom = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resource){
        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvasMapa.getWidth(), canvasMapa.getHeight());
        try {
            imagenFondo = new Image("/recursos/5Cuadras.jpg");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar el fondo");
        }
        tooltipMapa = new Tooltip();
        canvasMapa.setOnMouseMoved(this::handleMouseMoved);
        canvasMapa.setOnMouseExited(e -> tooltipMapa.hide());
        canvasMapa.setOnScroll(event -> {
            double factor = 1.1;
            if (event.getDeltaY() < 0){
                factor = 1/factor; //Zoom Out
            }
            factorZoom *= factor;

            //limitamos el zoom
            if (factorZoom<0.5) factorZoom =0.5;
            if (factorZoom> 20.0) factorZoom = 20.0;

            dibujarGrafo();
        });

        //eventos de paneo
        canvasMapa.setOnMousePressed(event -> {
            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();
        });

        canvasMapa.setOnMouseDragged(event ->{
            offsetX += (event.getX() -mouseAnchorX);
            offsetY += (event.getY() - mouseAnchorY);

            mouseAnchorX = event.getX();
            mouseAnchorY = event.getY();

            dibujarGrafo();
        });
    }

    private void handleMouseMoved(MouseEvent event) {
        if (this.flota == null || this.datos == null) {
            tooltipMapa.hide();
            return;
        }
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        boolean hover = false;

        // Revisar si el ratón está sobre algún vehículo
        for (int i = 0; i < this.flota.tamanio(); i++) {
            Vehiculo v = (Vehiculo) this.flota.devolver(i);
            Vertice nodoV = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
            if (nodoV != null) {
                double xV = mapOffsetX + ((nodoV.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                double yV = mapOffsetY + (mapMaxLat - nodoV.getLatitud()) * mapScale;
                
                // Le damos un pequeño margen (6 píxeles) para que sea fácil apuntar con el ratón
                if (Math.abs(mouseX - xV) <= 6 && Math.abs(mouseY - yV) <= 6) {
                    String estado = v.getDisponible() ? "Disponible" : "Ocupado";
                    tooltipMapa.setText("Vehículo ID: " + v.getID() + " (" + estado + ")");
                    tooltipMapa.show(canvasMapa, event.getScreenX() + 15, event.getScreenY() + 10);
                    hover = true;
                    break;
                }
            }
        }

        // (Bonus) Revisar también si el ratón está sobre el pasajero
        if (!hover && this.pasajeroActual != null) {
            Vertice nodoP = datos.getVerticePorIndice(this.pasajeroActual.getVerticeIDOrigen());
            if (nodoP != null) {
                double xP = mapOffsetX + ((nodoP.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                double yP = mapOffsetY + (mapMaxLat - nodoP.getLatitud()) * mapScale;
                
                if (Math.abs(mouseX - xP) <= 6 && Math.abs(mouseY - yP) <= 6) {
                    tooltipMapa.setText("Pasajero (Destino)");
                    tooltipMapa.show(canvasMapa, event.getScreenX() + 15, event.getScreenY() + 10);
                    hover = true;
                }
            }
        }
        
        if (!hover) {
            tooltipMapa.hide();
        }
    }
    
    private void mostrarVehiculosDisponibles(){
        listaVehiculos.getItems().clear();
        if (this.flota != null) {
            for (int i = 0; i < this.flota.tamanio(); i++) {
                Vehiculo v = (Vehiculo) this.flota.devolver(i);
                String estado = v.getDisponible() ? "Disponible" : "Ocupado";
                listaVehiculos.getItems().add("Vehículo " + v.getID() + " (" + estado + ") - Vértice: " + v.getVerticeIndiceOrigen());
            }
        }
    }
    
    @FXML
    private void handleNuevaSolicitud(){
        this.caminoResaltado = null; // Limpiamos el camino anterior si existiera
        
        if (this.motor == null || this.flota == null) {
            System.err.println("El motor o la flota no han sido inicializados.");
            return;
        }
        
        ResultadoDespacho resultado;
        
        this.pasajeroActual = motor.usuarioRandom();
        resultado = motor.despacharViaje(this.pasajeroActual, this.flota); // Reutilizamos la flota global
        
        listaDespacho.getItems().clear();
        Vehiculo asignado = resultado.getVehiculoAsignado();
        
        if (asignado != null) {
            listaDespacho.getItems().add("1. Vehiculo: " + asignado.getID() + " - ETA: " + String.format("%.2f", asignado.getETA()) + " (Asignado)");

            ColaPrioridad<Vehiculo> cola = resultado.getVehiculosCandidatos();
            int i = 2;
            while (!cola.estaVacia()) {
                Vehiculo v = cola.sacar();
                listaDespacho.getItems().add(i + ". Vehiculo: " + v.getID() + " - ETA: " + String.format("%.2f", v.getETA()));
                i++;
            }
            
            // Obtenemos el camino. 
            // NOTA: Ajustar "getPosicion()" al nombre real del método de tu clase Vehiculo que devuelve el índice.
            int posVehiculo = asignado.getVerticeIndiceOrigen(); 
            this.caminoResaltado = grafo.caminoDijkstra(posVehiculo, this.pasajeroActual.getVerticeIDOrigen());
            
            Usuario pasajeroAsignado = this.pasajeroActual; // Capturamos la referencia del pasajero
            iniciarAnimacionViaje(asignado, pasajeroAsignado, this.caminoResaltado);
        } else {
            listaDespacho.getItems().add("No hay vehículos disponibles en este momento.");
            this.pasajeroActual = null; // Anulamos el pasajero si ningún coche le fue asignado
        }
        
        mostrarVehiculosDisponibles(); // Actualizamos la lista con los nuevos estados
        dibujarGrafo(); // Redibujamos el Canvas para que se grafique la ruta
    }

    private void iniciarAnimacionViaje(Vehiculo vehiculo, Usuario pasajero, ListaDoubleLinkedL rutaHaciaPasajero) {
        // 1. Animamos el vehículo hacia el pasajero
        Timeline animacionHaciaPasajero = crearTimelineViaje(vehiculo, rutaHaciaPasajero, () -> {
            
            // Cuando el vehículo llega al pasajero, calculamos la ruta hacia el destino
            ListaDoubleLinkedL rutaAlDestino = grafo.caminoDijkstra(pasajero.getVerticeIDOrigen(), pasajero.getVerticeIDDestino());
            
            // Si el usuario no hizo clic en "Nueva Solicitud" mientras tanto, actualizamos la línea roja
            if (this.pasajeroActual == pasajero) {
                this.caminoResaltado = rutaAlDestino;
            }
            
            // 2. Animamos el vehículo hacia el destino final
            Timeline animacionAlDestino = crearTimelineViaje(vehiculo, rutaAlDestino, () -> {
                vehiculo.setDisponible(true); // El viaje terminó, liberamos el vehículo
                if (this.pasajeroActual == pasajero) {
                    this.caminoResaltado = null;
                    this.pasajeroActual = null;
                }
                mostrarVehiculosDisponibles();
                dibujarGrafo();
            });
            animacionAlDestino.play();
        });
        animacionHaciaPasajero.play();
    }

    private Timeline crearTimelineViaje(Vehiculo vehiculo, ListaDoubleLinkedL ruta, Runnable alTerminar) {
        Timeline timeline = new Timeline();
        double velocidadMS = 150.0; // Milisegundos por vértice (ajústalo para cambiar la velocidad)
        
        for (int i = 0; i < ruta.tamanio(); i++) {
            final int index = i;
            KeyFrame frame = new KeyFrame(Duration.millis(velocidadMS * (i + 1)), e -> {
                int idNodo = (int) ruta.devolver(index);
                vehiculo.setVerticeIndiceOrigen(idNodo); // Movemos el vehículo a la nueva posición
                dibujarGrafo(); // Redibujamos el mapa para aplicar el movimiento
            });
            timeline.getKeyFrames().add(frame);
        }
        
        // Evento especial que se dispara al finalizar el recorrido de toda la ruta
        KeyFrame fin = new KeyFrame(Duration.millis(velocidadMS * ruta.tamanio() + 10), e -> {
            if (alTerminar != null) alTerminar.run();
        });
        timeline.getKeyFrames().add(fin);
        
        return timeline;
    }

    @FXML
    private void handleLimpiarSeleccion(){
        listaDespacho.getItems().clear();
        this.caminoResaltado = null;
        this.pasajeroActual = null;
        
        // Liberamos los vehículos para que vuelvan a estar disponibles
        if (this.flota != null) {
            for (int i = 0; i < this.flota.tamanio(); i++) {
                Vehiculo v = (Vehiculo) this.flota.devolver(i);
                v.setDisponible(true);
            }
        }
        
        mostrarVehiculosDisponibles();
        dibujarGrafo();
    }
    public void setGrafoyDatos(GrafoMapa grafo, DatosMapa datos){
        this.grafo = grafo;
        this.datos = datos;
        
        // Inicializamos el motor y generamos la flota UNA SOLA VEZ al cargar el mapa
        this.motor = new MotorDespacho(this.grafo);
        this.flota = this.motor.generaFlota();
        mostrarVehiculosDisponibles(); // Mostramos la flota real en la lista de la izquierda
        
        // Aplazamos el dibujo hasta que JavaFX asigne el tamaño final al Canvas
        Platform.runLater(() -> {
            try {
                dibujarGrafo();
            } catch (Exception e) {
                System.out.println("No se pudo dibujar.");
                e.printStackTrace();
            }
        });
    }

    private void dibujarGrafo() {
        if (this.grafo == null || canvasMapa.getWidth() == 0 || canvasMapa.getHeight() == 0) {
            return;
        }

        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvasMapa.getWidth(), canvasMapa.getHeight());

        if (!actualizarEscalasYOffsets()) {
            return;
        }
        if(imagenFondo != null){
            double anchoGrafoPixeles,altoGrafoPixeles;
            anchoGrafoPixeles = (this.datos.getMaxLon() - this.mapMinLon) * mapFactorLon * mapScale;
            altoGrafoPixeles = (mapMaxLat - this.datos.getMinLat()) * mapScale;
            gc.drawImage(imagenFondo, mapOffsetX, mapOffsetY,anchoGrafoPixeles,altoGrafoPixeles);
        }
        dibujarAristas(gc);
        dibujarNodos(gc);
        dibujarCaminoResaltado(gc);
        dibujarFlota(gc);
        dibujarPasajero(gc);
    }

    private boolean actualizarEscalasYOffsets() {
        this.datos = grafo.getDatosMapa();
        if (datos == null) return false;

        double minLat = datos.getMinLat();
        this.mapMaxLat = datos.getMaxLat();
        this.mapMinLon = datos.getMinLon();
        double maxLon = datos.getMaxLon();

        double diffLon = maxLon - this.mapMinLon;
        double diffLat = this.mapMaxLat - minLat;

        double latMedia = Math.toRadians((minLat + this.mapMaxLat) / 2.0);
        this.mapFactorLon = Math.cos(latMedia);
        double diffLonAjustada = diffLon * this.mapFactorLon;

        double padding = 20.0;
        double usableW = canvasMapa.getWidth() - 2 * padding;
        double usableH = canvasMapa.getHeight() - 2 * padding;

        double scaleX = usableW / diffLonAjustada;
        double scaleY = usableH / diffLat;
        this.mapScale = Math.min(scaleX, scaleY); 

        this.mapOffsetX = padding + (usableW - (diffLonAjustada * this.mapScale)) / 2.0;
        this.mapOffsetY = padding + (usableH - (diffLat * this.mapScale)) / 2.0;
        //System.out.println("MaxLat: "+ this.mapMaxLat + " MaxLon: "+ maxLon + "\nMinLat: "+minLat+" MinLon: "+this.mapMinLon);
        return true;
    }

    private void dibujarAristas(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);

        for (int i = 0; i < grafo.getOrden(); i++) {
            ListaDoubleLinkedL adyacentes = grafo.getAdyacentes(i);

            if (adyacentes == null) continue;

            for (int j = 0; j < adyacentes.tamanio(); j++) {
                Arista arco = (Arista) adyacentes.devolver(j);
                List<Coordenada> geometria = arco.getGeometria();
                if(geometria != null && !geometria.isEmpty()){
                    Coordenada inicio = geometria.get(0);
                    double x1 = mapOffsetX + ((inicio.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                    double y1 = mapOffsetY + (mapMaxLat - inicio.getLatitud()) * mapScale;
                    gc.moveTo(x1,y1);
                    for (int k = 1; k < geometria.size(); k++) {
                        Coordenada c = geometria.get(k);
                        double x2 = mapOffsetX + ((c.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                        double y2 = mapOffsetY + (mapMaxLat - c.getLatitud()) * mapScale;
                        gc.lineTo(x2, y2);
                    }
                }
                gc.stroke();
            }
        }
    }

    private void dibujarNodos(GraphicsContext gc) {
        gc.setFill(Color.DODGERBLUE);
        for (int i = 0; i < grafo.getOrden(); i++) {
            Vertice nodo = datos.getVerticePorIndice(i);
            if (nodo == null) continue;

            double x = mapOffsetX + ((nodo.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
            double y = mapOffsetY + (mapMaxLat - nodo.getLatitud()) * mapScale;
            gc.fillOval(x - 1.5, y - 1.5, 3, 3);
        }
    }

    private void dibujarCaminoResaltado(GraphicsContext gc) {
        if (this.caminoResaltado != null && this.caminoResaltado.tamanio() > 1) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3.0);
            
            for (int i = 0; i < this.caminoResaltado.tamanio() - 1; i++) {
                int idOrigen = (int) this.caminoResaltado.devolver(i);
                int idDestino = (int) this.caminoResaltado.devolver(i + 1);

                Vertice vO = datos.getVerticePorIndice(idOrigen);
                Vertice vD = datos.getVerticePorIndice(idDestino);

                if (vO != null && vD != null) {
                    double px1 = mapOffsetX + ((vO.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                    double py1 = mapOffsetY + (mapMaxLat - vO.getLatitud()) * mapScale;
                    double px2 = mapOffsetX + ((vD.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                    double py2 = mapOffsetY + (mapMaxLat - vD.getLatitud()) * mapScale;
                    gc.strokeLine(px1, py1, px2, py2);
                }
            }
        }
    }

    private void dibujarFlota(GraphicsContext gc) {
        if (this.flota != null) {
            for (int i = 0; i < this.flota.tamanio(); i++) {
                Vehiculo v = (Vehiculo) this.flota.devolver(i);
                if (v.getDisponible()) {
                    gc.setFill(Color.LIMEGREEN); // Verde para disponibles
                } else {
                    gc.setFill(Color.RED); // Rojo para ocupados
                }
                Vertice nodoV = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
                if (nodoV != null) {
                    double xV = mapOffsetX + ((nodoV.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                    double yV = mapOffsetY + (mapMaxLat - nodoV.getLatitud()) * mapScale;
                    gc.fillOval(xV - 3, yV - 3, 6, 6);
                }
            }
        }
    }

    private void dibujarPasajero(GraphicsContext gc) {
        if (this.pasajeroActual != null) {
            gc.setFill(Color.YELLOW);
            Vertice nodoP = datos.getVerticePorIndice(this.pasajeroActual.getVerticeIDOrigen());
            if (nodoP != null) {
                double xP = mapOffsetX + ((nodoP.getLongitud() - mapMinLon) * mapFactorLon) * mapScale;
                double yP = mapOffsetY + (mapMaxLat - nodoP.getLatitud()) * mapScale;
                gc.fillOval(xP - 4, yP - 4, 8, 8);
            }
        }
    }
    
}
