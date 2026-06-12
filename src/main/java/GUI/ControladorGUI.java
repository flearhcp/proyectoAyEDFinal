package GUI;

//Importamos todas las librerias de JavaFX

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.control.ToggleButton;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.concurrent.Task;

//Otras librerias

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import Grafos.GrafoMapa;
import LectorJSON.*;
import contenedores.*;
import motor_matching_engine.*;

/**
 * Controlador para la interfaz gráfica de usuario (GUI) de la aplicación de mapas.
 * Gestiona la interacción del usuario, la visualización del mapa y la lógica de despacho.
 */
public class ControladorGUI implements Initializable{
    @FXML private Canvas canvasMapa;
    @FXML private Pane contenedorMapa;
    @FXML private ListView<String> listaVehiculos;
    @FXML private ListView<String> listaDespacho;
    @FXML private ToggleButton btnMostrarFondo;
    //private Image imagenFondo;
    private Image imagenVehiculoDisponible;
    private Image imagenVehiculoOcupado;
    private Image imagenPasajero;
    private Image imagenPasajeroOscuro;
    private Image imagenBandera;
    private Image imagenBanderaOscura;
    private GrafoMapa grafo;
    private MotorDespacho motor;
    private DatosMapa datos;
    private ListaDoubleLinkedL caminoResaltado;
    private ListaDoubleLinkedL flota; // Guardamos la flota a nivel de clase para reutilizarla
    private Usuario pasajeroActual; // Guardamos el pasajero actual para dibujarlo
    private boolean pasajeroRecogido; // Estado para alternar entre icono de pasajero y punto amarillo
    private boolean mostrarFondo = false; // Controla si se deben dibujar los tiles de OSM
    private int colocar = 0; /* Este atributo controla si se coloca un vehiculo o usuario en
    en mapa, cuando esta en 0 no coloca nada*/
    private int origenPasajero = -1; /* este atributo guarda el vertice en donde colocamos al pasajero cuando
    hacemos click por primera vez*/
    
    // Variables para mapear coordenadas del mouse con el Canvas
    private Tooltip tooltipMapa;
    private double mapScale, mapFactorLon, mapMinLon, mapMaxLat,mapMinLat,mapMaxLon;
    private double mapOffsetX;
    private double mapOffsetY;
    //Para zoom
    private double mouseAnchorY;
    private double mouseAnchorX;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double factorZoom = 1.0;
    private Vertice nodoPreview; // Nodo sobre el que está el mouse para previsualizar colocación
    //para mostrar fondo
    private final ProveedorTiles proveedorTiles = new ProveedorTiles();

    /**
     * Método de inicialización llamado automáticamente por JavaFX al cargar el FXML.
     * Configura el lienzo del mapa, los listeners de eventos, las fábricas de celdas para las listas,
     * carga las imágenes y establece los tooltips.
     * @param location La ubicación utilizada para resolver rutas relativas para el objeto raíz, o null si no se conoce.
     * @param resource Los recursos utilizados para localizar el objeto raíz, o null si el objeto raíz no fue localizado.
     */
    @Override
    public void initialize(URL location, ResourceBundle resource){
        // 1. Verificamos que el contenedor no sea nulo antes de operar con él
        if (contenedorMapa != null) {
            // Asignamos un tamaño inicial por defecto para evitar que colapse a 0x0.
            contenedorMapa.setPrefWidth(800);
            contenedorMapa.setPrefHeight(600);
            contenedorMapa.setMinWidth(400); // Tamaño mínimo de seguridad
            contenedorMapa.setMinHeight(300);

            // Enlazamos el Canvas para que siga al contenedor
            canvasMapa.widthProperty().bind(contenedorMapa.widthProperty());
            canvasMapa.heightProperty().bind(contenedorMapa.heightProperty());
        }

        // Inicializamos el botón de alternancia con el valor por defecto
        if (btnMostrarFondo != null) {
            btnMostrarFondo.setSelected(mostrarFondo);
            btnMostrarFondo.setText(mostrarFondo ? "Ocultar Fondo" : "Mostrar Fondo");
        }
        
        // Configuramos la lista para mostrar círculos de colores reales al aldo de cada vehiculo;
        listaVehiculos.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Circle indicador = new Circle(6);
                    if (item.startsWith("DISP|")) {
                        indicador.setFill(Color.LIMEGREEN);
                    } else {
                        indicador.setFill(Color.RED);
                    }

                    // Creamos un contenedor horizontal para alinear el punto con la primera línea
                    HBox contenedor = new HBox(0);
                    contenedor.setAlignment(Pos.TOP_LEFT);
                    
                    // Ajustamos un margen superior al círculo para que baje un poco y se alinee con el texto "Vehículo"
                    HBox.setMargin(indicador, new Insets(4, 0, 0, 0));
                    
                    Label texto = new Label(item.replace("DISP|", "").replace("OCUP|", ""));
                    contenedor.getChildren().addAll(indicador, texto);

                    setGraphic(contenedor);
                    setText(null); // Anulamos el texto por defecto para usar nuestro contenedor
                }
            }
        });
        

        GraphicsContext gc = canvasMapa.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvasMapa.getWidth(), canvasMapa.getHeight());
         try {
            imagenVehiculoDisponible = new Image("/auto.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto del auto");
        }
         try {
            imagenVehiculoOcupado = new Image("/autoRojo.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto del auto");
        }
        try {
            imagenPasajero = new Image("/hombre.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto del pasajero");
        }
        try {
            imagenPasajeroOscuro = new Image("/hombreNegro.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto del pasajero");
        }
        try {
            imagenBandera = new Image("/bandera.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto de la bandera");
        }
        try {
            imagenBanderaOscura = new Image("/banderaNegra.png");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar foto de la bandera oscura");
        }
       /*  try {
            imagenFondo = new Image("recursos/5Cuadras.jpg");
        } catch (Exception e) {
            System.err.println("No se pudo encontrar el fondo");
        } */
        tooltipMapa = new Tooltip();
        canvasMapa.setOnMouseMoved(this::handleMouseMoved);
        canvasMapa.setOnMouseClicked(this::handleMouseClicked);
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

        // Listener para redibujar el mapa apenas el Canvas obtenga sus dimensiones reales
        canvasMapa.widthProperty().addListener((observable, oldValue, newValue) -> dibujarGrafo());
        canvasMapa.heightProperty().addListener((observable, oldValue, newValue) -> dibujarGrafo());
    }

    /**
     * Maneja el evento de movimiento del ratón sobre el lienzo del mapa.
     * Muestra tooltips con información de vehículos o pasajeros y actualiza la previsualización de colocación.
     * @param event El evento de ratón.
     */
    private void handleMouseMoved(MouseEvent event) {
        if (this.flota == null || this.datos == null) {
            tooltipMapa.hide();
            return;
        }
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        boolean hover = false;

        // Lógica de previsualización al colocar elementos
        if (this.colocar > 0) {
            Vertice detectado = buscarVerticeCercano(mouseX, mouseY);
            if (detectado != this.nodoPreview) {
                this.nodoPreview = detectado;
                dibujarGrafo();
            }
        }

        // Revisar si el ratón está sobre algún vehículo
        for (int i = 0; i < this.flota.tamanio(); i++) {
            Vehiculo v = (Vehiculo) this.flota.devolver(i);
            Vertice nodoV = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
            if (nodoV != null) {
                double xV = (mapOffsetX + ((nodoV.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                double yV = (mapOffsetY + (mapMaxLat - nodoV.getLatitud()) * mapScale) * factorZoom + offsetY;
                
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
                double xP = (mapOffsetX + ((nodoP.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                double yP = (mapOffsetY + (mapMaxLat - nodoP.getLatitud()) * mapScale) * factorZoom + offsetY;
                
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
    
    /**
     * Actualiza la lista de vehículos disponibles en la interfaz gráfica.
     */
    private void mostrarVehiculosDisponibles(){
        /* listaVehiculos.getItems().clear();
        if (this.flota != null) {
            for (int i = 0; i < this.flota.tamanio(); i++) {
                Vehiculo v = (Vehiculo) this.flota.devolver(i);
                String estado = v.getDisponible() ? "Disponible" : "Ocupado";
                
                // Buscamos el objeto Vertice real usando el índice que tiene el vehículo
                Vertice nodo = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
                String nombreInterseccion = (nodo != null) ? nodo.getNombreInterseccion() : "Desconocido";
                
                listaVehiculos.getItems().add("Vehículo " + v.getID() + " (" + estado + ") - " + nombreInterseccion);
            }
        } */
        listaVehiculos.getItems().clear();
        if (this.flota == null) return;

        for (int i = 0; i < this.flota.tamanio(); i++) {
            Vehiculo v = (Vehiculo) this.flota.devolver(i);
            
            // Usamos un prefijo para que el CellFactory sepa qué color dibujar
            String prefijo = v.getDisponible() ? "DISP|" : "OCUP|";
            
            Vertice nodo = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
            String nombreInterseccion = (nodo != null) ? nodo.getNombreInterseccion() : "Desconocido";
            
            // Construimos el string con el indicador al inicio
            listaVehiculos.getItems().add(prefijo + " Vehículo " + v.getID() + "\n   📍 " + nombreInterseccion);
        }
    }

    
    /**
     * Maneja el evento de una nueva solicitud de viaje.
     */
    @FXML
    private void handleNuevaSolicitud(){
        this.caminoResaltado = null; // Limpiamos el camino anterior si existiera
        this.pasajeroRecogido = false; // Resetear estado al pedir un nuevo viaje
        
        if (this.motor == null || this.flota == null) {
            System.err.println("El motor o la flota no han sido inicializados.");
            return;
        }

        listaDespacho.getItems().clear();
        listaDespacho.getItems().add("Calculando despacho...");

        Task<ResultadoDespacho> taskDespacho = new Task<>() {
            @Override
            protected ResultadoDespacho call() throws Exception {
                 if (pasajeroActual == null) {
                    pasajeroActual = motor.usuarioRandom();
                }

                return motor.despacharViaje(pasajeroActual, flota);
            }
        };

        taskDespacho.setOnSucceeded(event -> {
            ResultadoDespacho resultado = taskDespacho.getValue();
            listaDespacho.getItems().clear();
            Vehiculo asignado = resultado.getVehiculoAsignado();
            
            if (asignado != null) {
                listaDespacho.getItems().add("1. Vehiculo: " + asignado.getID() + " Llega en en " + asignado.getTiempoEspera() + " (Asignado)");
                
                ColaPrioridad<Vehiculo> cola = resultado.getVehiculosCandidatos();
                int i = 2;
                while (!cola.estaVacia()) {
                    Vehiculo v = cola.sacar();
                    listaDespacho.getItems().add(i + ". Vehiculo: " + v.getID() + " tiempo de llegada " + v.getTiempoEspera());
                    i++;
                }
                
                this.caminoResaltado = grafo.caminoDijkstra(asignado.getVerticeIndiceOrigen(), this.pasajeroActual.getVerticeIDOrigen());
                iniciarAnimacionViaje(asignado, this.pasajeroActual, this.caminoResaltado);
            } else {
                listaDespacho.getItems().add("No hay vehículos disponibles.");
                this.pasajeroActual = null;
            }
            mostrarVehiculosDisponibles();
            dibujarGrafo();
        });

        taskDespacho.setOnFailed(e -> System.err.println("Error en el despacho: " + taskDespacho.getException().getMessage()));

        new Thread(taskDespacho).start();
    }

    @FXML
    private void handleToggleFondo() {
        // Actualizamos el estado y forzamos un redibujado
        this.mostrarFondo = btnMostrarFondo.isSelected();
        btnMostrarFondo.setText(mostrarFondo ? "Ocultar Fondo" : "Mostrar Fondo");
        dibujarGrafo();
    }

    /**
     * Maneja el evento de cargar un nuevo mapa desde un archivo JSON.
     */
    @FXML
    private void handleCargarNuevoMapa() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo JSON del nuevo mapa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON", "*.json"));
        
        Stage currentStage = (Stage) contenedorMapa.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile != null) {
            Task<Void> loadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Procesamos el nuevo archivo en segundo plano
                    LectorJson lector = new LectorJson(selectedFile.getAbsolutePath());
                    DatosMapa nuevosDatos = lector.generarDatosMapa();
                    GrafoMapa nuevoGrafo = new GrafoMapa(nuevosDatos.getCantidadVertices(), nuevosDatos);
                    nuevoGrafo.cargarGrafo();

                    Platform.runLater(() -> {
                        try {
                            // Creamos una nueva instancia de la GUI para este mapa
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventana.fxml"));
                            Parent root = loader.load();
                            
                            ControladorGUI nuevoControlador = loader.getController();
                            nuevoControlador.setGrafoyDatos(nuevoGrafo, nuevosDatos);
                            
                            Stage newStage = new Stage();
                            newStage.setTitle("Mapa: " + selectedFile.getName());
                            newStage.setScene(new Scene(root));
                            newStage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    return null;
                }
            };

            // Capturamos errores para que no fallen en silencio
            loadTask.setOnFailed(e -> {
                System.err.println("Error al cargar el nuevo mapa:");
                loadTask.getException().printStackTrace();
            });

            new Thread(loadTask).start();
        }
    }

    /**
     * Inicia la animación de un viaje para un vehículo y un pasajero.
     * @param vehiculo El vehículo que realizará el viaje.
     * @param pasajero El pasajero que será transportado.
     * @param rutaHaciaPasajero La ruta que el vehículo debe seguir para recoger al pasajero.
     */
    private void iniciarAnimacionViaje(Vehiculo vehiculo, Usuario pasajero, ListaDoubleLinkedL rutaHaciaPasajero) {
        // 1. Animamos el vehículo hacia el pasajero
        Timeline animacionHaciaPasajero = crearTimelineViaje(vehiculo, rutaHaciaPasajero, () -> {
            
            // Cuando el vehículo llega al pasajero, calculamos la ruta hacia el destino
            ListaDoubleLinkedL rutaAlDestino = grafo.caminoDijkstra(pasajero.getVerticeIDOrigen(), pasajero.getVerticeIDDestino());
            
            // Si el usuario no hizo clic en "Nueva Solicitud" mientras tanto, actualizamos la línea roja
            if (this.pasajeroActual == pasajero) {
                this.pasajeroRecogido = true; // El auto llegó al pasajero
                this.caminoResaltado = rutaAlDestino;
            }
            
            // 2. Animamos el vehículo hacia el destino final
            Timeline animacionAlDestino = crearTimelineViaje(vehiculo, rutaAlDestino, () -> {
                vehiculo.setDisponible(true); // El viaje terminó, liberamos el vehículo
                if (this.pasajeroActual == pasajero) {
                    this.caminoResaltado = null;
                    this.pasajeroActual = null;
                    this.pasajeroRecogido = false;
                }
                mostrarVehiculosDisponibles();
                dibujarGrafo();
            });
            animacionAlDestino.play();
        });
        animacionHaciaPasajero.play();
    }

    /**
     * Crea una línea de tiempo (Timeline) para animar el movimiento de un vehículo a lo largo de una ruta.
     * @param vehiculo El vehículo a animar.
     * @param ruta La lista de vértices que componen la ruta.
     * @param alTerminar Un {@code Runnable} que se ejecuta cuando la animación de la ruta ha terminado.
     * @return Un objeto {@code Timeline} configurado para la animación.
     */
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

    /**
     * Maneja el evento de limpiar la selección actual, reseteando el estado del mapa y los vehículos.
     */
    @FXML
    private void handleLimpiarSeleccion(){
        listaDespacho.getItems().clear();
        this.caminoResaltado = null;
        this.pasajeroActual = null;
        this.pasajeroRecogido = false;
        
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

    /**
     * Establece el modo de colocación en "agregar vehículo".
     * El usuario podrá hacer clic en un vértice del mapa para añadir un nuevo vehículo.
     */
     @FXML
    private void handleAgregarVehiculo() {
        this.colocar = 1;
        System.out.println("MODO: Selecciona un vértice para colocar el Vehículo.");
    }

    /**
     * Establece el modo de colocación en "agregar pasajero".
     * El usuario primero seleccionará el origen y luego el destino del pasajero.
     */
    @FXML
    private void handleAgregarPasajero() {
        // Limpiamos el pasajero actual y el preview para evitar conflictos
        this.pasajeroActual = null;
        this.colocar = 2;
        System.out.println("MODO: Selecciona un vértice para el ORIGEN del pasajero.");
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

    /**
     * Dibuja el grafo en el lienzo del mapa, incluyendo aristas, nodos, flota, pasajero y previsualizaciones.
     */
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
       /*  if(imagenFondo != null){
            double anchoGrafoPixeles,altoGrafoPixeles;
            anchoGrafoPixeles = (this.datos.getMaxLon() - this.mapMinLon) * mapFactorLon * mapScale * factorZoom;
            altoGrafoPixeles = (mapMaxLat - this.datos.getMinLat()) * mapScale * factorZoom;
            gc.drawImage(imagenFondo, mapOffsetX * factorZoom + offsetX, mapOffsetY * factorZoom + offsetY, anchoGrafoPixeles, altoGrafoPixeles);
        } */
        
        // Si el fondo está habilitado y no está listo (cargando), detenemos el dibujo para que las calles no "floten"
        if (mostrarFondo) {
           dibujarFondoMapa(gc);
        }

        dibujarAristas(gc);
        if (factorZoom > 2.0) dibujarNodos(gc); // Solo dibujar nodos si hay zoom suficiente para verlos
        dibujarCaminoResaltado(gc);
        dibujarFlota(gc);
        dibujarPasajero(gc);
        dibujarPreviews(gc);
    }

    /**
     * Actualiza las escalas y offsets para el mapeo de coordenadas geográficas a píxeles del lienzo.
     * @return true si la actualización fue exitosa, false si los datos del mapa no están disponibles.
     */
    private boolean actualizarEscalasYOffsets() {
        this.datos = grafo.getDatosMapa();
        if (datos == null) return false;

        this.mapMinLat = datos.getMinLat();
        this.mapMaxLat = datos.getMaxLat();
        this.mapMinLon = datos.getMinLon();
        this.mapMaxLon = datos.getMaxLon();

        double diffLon = mapMaxLon - this.mapMinLon;
        double diffLat = this.mapMaxLat - mapMinLat;

        double latMedia = Math.toRadians((mapMinLat + this.mapMaxLat) / 2.0);
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

    /**
     * Dibuja las aristas del grafo en el lienzo del mapa.
     */
    private void dibujarAristas(GraphicsContext gc) {
        gc.setStroke(mostrarFondo ? Color.rgb(128, 128, 128, 0.3) : Color.GRAY);
        // El grosor empieza en 1.0 y crece gradualmente con el factorZoom
        double grosorEscalado = 1.0 + (factorZoom > 1.0 ? (factorZoom - 1.0) * 0.2 : 0);
        gc.setLineWidth(grosorEscalado);
        gc.beginPath();

        // Usamos la lista plana de DatosMapa para evitar el recorrido O(N) de la lista enlazada custom
        for (Arista arco : datos.getAristas()) {
            List<Coordenada> geometria = arco.getGeometria();
            if (geometria != null && !geometria.isEmpty()) {
                Coordenada c0 = geometria.get(0);
                double x = (mapOffsetX + ((c0.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                double y = (mapOffsetY + (mapMaxLat - c0.getLatitud()) * mapScale) * factorZoom + offsetY;
                gc.moveTo(x, y);

                for (int k = 1; k < geometria.size(); k++) {
                    Coordenada c = geometria.get(k);
                    x = (mapOffsetX + ((c.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                    y = (mapOffsetY + (mapMaxLat - c.getLatitud()) * mapScale) * factorZoom + offsetY;
                    gc.lineTo(x, y);
                }
            }
        }
        gc.stroke(); // Dibujamos todas las aristas de una sola vez
    }

    /**
     * Dibuja los nodos (vértices) del grafo en el lienzo del mapa.
     */
    private void dibujarNodos(GraphicsContext gc) {
        gc.setFill(mostrarFondo ? Color.rgb(30, 144, 255, 0.3) : Color.DODGERBLUE);
        for (int i = 0; i < grafo.getOrden(); i++) {
            Vertice nodo = datos.getVerticePorIndice(i);
            if (nodo == null) continue;

            double x = (mapOffsetX + ((nodo.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
            double y = (mapOffsetY + (mapMaxLat - nodo.getLatitud()) * mapScale) * factorZoom + offsetY;
            gc.fillOval(x - 1.5, y - 1.5, 3, 3);
        }
    }

   /**
    * Dibuja el camino resaltado (ruta de Dijkstra) en el lienzo del mapa.
    */
   private void dibujarCaminoResaltado(GraphicsContext gc) {
        if (this.caminoResaltado != null && this.caminoResaltado.tamanio() > 1) {
            gc.setStroke(Color.RED);
            gc.setLineWidth(3.0);
            gc.beginPath();
            boolean firstPoint = true;
           
            for (int i = 0; i < this.caminoResaltado.tamanio() - 1; i++) {
                int idOrigen = (int) this.caminoResaltado.devolver(i);
                int idDestino = (int) this.caminoResaltado.devolver(i + 1);

                /* Vertice vO = datos.getVerticePorIndice(idOrigen);
                Vertice vD = datos.getVerticePorIndice(idDestino);

                if (vO != null && vD != null) {
                    double px1 = (mapOffsetX + ((vO.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                    double py1 = (mapOffsetY + (mapMaxLat - vO.getLatitud()) * mapScale) * factorZoom + offsetY;
                    double px2 = (mapOffsetX + ((vD.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                    double py2 = (mapOffsetY + (mapMaxLat - vD.getLatitud()) * mapScale) * factorZoom + offsetY;
                    gc.strokeLine(px1, py1, px2, py2); */
                     // Buscamos la arista real que une estos dos vértices para obtener su geometría
                Arista arcoActual = null;
                ListaDoubleLinkedL adyacentes = grafo.getAdyacentes(idOrigen);
                for (int j = 0; j < adyacentes.tamanio(); j++) {
                    Arista a = (Arista) adyacentes.devolver(j);
                    if (a.getDestino().getIndice() == idDestino) {
                        arcoActual = a;
                        break;
                    }
                }
                 if (arcoActual != null) {
                    List<Coordenada> geo = arcoActual.getGeometria();
                    if (geo != null && !geo.isEmpty()) {
                        for (Coordenada c : geo) {
                            double[] p = convertirCoordenadasAPixeles(c.getLatitud(), c.getLongitud());
                            if (firstPoint) {
                                gc.moveTo(p[0], p[1]);
                                firstPoint = false;
                            } else {
                                gc.lineTo(p[0], p[1]);
                            }
                        }
                    }
                } else {
                    // Fallback: si no se encuentra la arista, dibujamos línea recta entre vértices
                    Vertice vO = datos.getVerticePorIndice(idOrigen);
                    Vertice vD = datos.getVerticePorIndice(idDestino);
                    if (vO != null && vD != null) {
                        double[] p1 = convertirCoordenadasAPixeles(vO.getLatitud(), vO.getLongitud());
                        double[] p2 = convertirCoordenadasAPixeles(vD.getLatitud(), vD.getLongitud());
                        if (firstPoint) {
                            gc.moveTo(p1[0], p1[1]);
                            firstPoint = false;
                        }
                        gc.lineTo(p2[0], p2[1]);
                    }
                }
            }
            gc.stroke();
        }
    }


    /**
     * Dibuja la flota de vehículos en el lienzo del mapa.
     */
    private void dibujarFlota(GraphicsContext gc) {
        
        if (this.flota != null) {
            for (int i = 0; i < this.flota.tamanio(); i++) {
                Vehiculo v = (Vehiculo) this.flota.devolver(i);
               /*  if (v.getDisponible()) {
                    gc.setFill(Color.LIMEGREEN); // Verde para disponibles
                } else {
                    gc.setFill(Color.RED); // Rojo para ocupados
                } */
                Vertice nodoV = datos.getVerticePorIndice(v.getVerticeIndiceOrigen());
                if (nodoV != null) {
                    double xV = (mapOffsetX + ((nodoV.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                    double yV = (mapOffsetY + (mapMaxLat - nodoV.getLatitud()) * mapScale) * factorZoom + offsetY;
                    //gc.fillOval(xV - 3, yV - 3, 6, 6);
                     if (v.getDisponible()) {
                        gc.drawImage(imagenVehiculoDisponible, xV - 16, yV -16, 32, 32);
                    } else {
                        gc.drawImage(imagenVehiculoOcupado, xV - 16, yV -16, 32, 32);
                    }
                }
            }
        } else if (this.origenPasajero != -1) {
            // Si el pasajero aún no se creó pero ya fijamos el origen (estamos eligiendo destino)
            Vertice nodoO = datos.getVerticePorIndice(this.origenPasajero);
            if (nodoO != null) {
                double[] pos = convertirCoordenadasAPixeles(nodoO.getLatitud(), nodoO.getLongitud());
                Image imgP = mostrarFondo ? imagenPasajeroOscuro : imagenPasajero;
                gc.drawImage(imgP, pos[0] - 8, pos[1] - 8, 16, 16);
            }
        }
    }

    /**
     * Dibuja el pasajero (origen y destino) en el lienzo del mapa.
     */
    private void dibujarPasajero(GraphicsContext gc) {
        if (this.pasajeroActual != null) {
            //gc.setFill(Color.YELLOW);
            Vertice nodoP = datos.getVerticePorIndice(this.pasajeroActual.getVerticeIDOrigen());
            if (nodoP != null) {
                double xP = (mapOffsetX + ((nodoP.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                double yP = (mapOffsetY + (mapMaxLat - nodoP.getLatitud()) * mapScale) * factorZoom + offsetY;
                //gc.fillOval(xP - 4, yP - 4, 8, 8);
                //gc.drawImage(imagenPasajero, xP - (16/2), yP-(16/2), 16, 16);
                if (!pasajeroRecogido) {
                    // Fase 1: Pasajero esperando (mostrar icono PNG)
                    Image imgP = mostrarFondo ? imagenPasajeroOscuro : imagenPasajero;
                    gc.drawImage(imgP, xP - 8, yP - 8, 16, 16);
                } else {
                    // Fase 2: Pasajero en viaje (punto amarillo en origen y bandera en destino)
                    gc.setFill(mostrarFondo ? Color.BLACK : Color.rgb(226, 228, 170));
                    gc.fillOval(xP - 4, yP - 4, 8, 8);

                    Vertice nodoD = datos.getVerticePorIndice(this.pasajeroActual.getVerticeIDDestino());
                    if (nodoD != null) {
                        double xD = (mapOffsetX + ((nodoD.getLongitud() - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
                        double yD = (mapOffsetY + (mapMaxLat - nodoD.getLatitud()) * mapScale) * factorZoom + offsetY;

                        Image imgB = mostrarFondo ? imagenBanderaOscura : imagenBandera;
                        if (imgB != null) {
                            gc.drawImage(imgB, xD - 8, yD - 8, 16, 16); // Centrado en la base
                        }
                    }
                }
            }
        }
    }
  
    /**
     * Dibuja el fondo del mapa utilizando tiles de OpenStreetMap.
     * Calcula el nivel de zoom óptimo y descarga/cachea los tiles necesarios.
     * @param gc El contexto gráfico 2D del lienzo.
     */
    private void dibujarFondoMapa(GraphicsContext gc) {
    // Calculamos el nivel de zoom de OpenStreetMap óptimo según la escala actual de la pantalla
    // La escala 'mapScale * factorZoom' es píxeles por grado de longitud proyectada.
    // Ajustamos por mapFactorLon para obtener píxeles por grado de longitud real.
    double pixelsPerLonDegree = mapScale * factorZoom * mapFactorLon;
    
    // Aplicamos logaritmo en base 2 para encontrar el nivel de zoom de OSM más cercano
    int z = (int) Math.round(Math.log(pixelsPerLonDegree * 360.0 / 256.0) / Math.log(2));
    z = Math.max(0, Math.min(19, z)); // Limitamos al rango estándar de OSM

    // 1. Averiguamos qué rango de baldosas X e Y cubren nuestra zona actual con el zoom calculado
    int minX = proveedorTiles.lonToTileX(mapMinLon, z);
    int maxX = proveedorTiles.lonToTileX(mapMaxLon, z);
    int minY = proveedorTiles.latToTileY(mapMaxLat, z); 
    int maxY = proveedorTiles.latToTileY(mapMinLat, z);


    // 2. Recorremos la cuadrícula de baldosas necesarias
    for (int x = minX; x <= maxX; x++) {
        for (int y = minY; y <= maxY; y++) {
            Image tile = proveedorTiles.obtenerTile(x, y, z, this::dibujarGrafo);
            
            // Validamos que el tile no sea null y esté cargado
            if (tile != null && tile.getProgress() >= 1.0) {
                double[] tl = convertirCoordenadasAPixeles(proveedorTiles.tileYToLat(y, z), proveedorTiles.tileXToLon(x, z));
                double[] br = convertirCoordenadasAPixeles(proveedorTiles.tileYToLat(y + 1, z), proveedorTiles.tileXToLon(x + 1, z));
                gc.drawImage(tile, tl[0], tl[1], br[0] - tl[0], br[1] - tl[1]);
            }
        }
    }
}

    /**
     * Convierte coordenadas de latitud y longitud a coordenadas de píxeles en el lienzo.
     * Aplica las transformaciones de escala, offset y zoom actuales.
     * @param lat La latitud a convertir.
     * @param lon La longitud a convertir.
     * @return Un arreglo de dos elementos {@code double[]} donde el primer elemento es la coordenada X
     *         y el segundo elemento es la coordenada Y en píxeles.
     */
    private double[] convertirCoordenadasAPixeles(double lat, double lon) {
        double x = (mapOffsetX + ((lon - mapMinLon) * mapFactorLon) * mapScale) * factorZoom + offsetX;
        double y = (mapOffsetY + (mapMaxLat - lat) * mapScale) * factorZoom + offsetY;
        return new double[]{x, y};
    }

    /**
     * Dibuja las previsualizaciones de colocación de vehículos o pasajeros en el lienzo.
     */
     private void dibujarPreviews(GraphicsContext gc) {
        if (this.colocar == 0 || this.nodoPreview == null) return;

        double[] pos = convertirCoordenadasAPixeles(nodoPreview.getLatitud(), nodoPreview.getLongitud());
        
        // Dibujamos con un poco de transparencia para indicar que es una "previsualización"
        gc.setGlobalAlpha(0.6);

        if (this.colocar == 1) { // Previsualizar Auto
            gc.drawImage(imagenVehiculoDisponible, pos[0] - 16, pos[1] - 16, 32, 32);
        } else if (this.colocar == 2) { // Previsualizar Pasajero (Origen)
            Image imgP = mostrarFondo ? imagenPasajeroOscuro : imagenPasajero;
            gc.drawImage(imgP, pos[0] - 8, pos[1] - 8, 16, 16);
        } else if (this.colocar == 3) { // Previsualizar Bandera (Destino)
            Image imgB = mostrarFondo ? imagenBanderaOscura : imagenBandera;
            gc.drawImage(imgB, pos[0] - 8, pos[1] - 8, 16, 16);
        }

        gc.setGlobalAlpha(1.0);
    }

    /**
     * Busca el vértice del grafo más cercano a las coordenadas de píxeles dadas.
     * @param mouseX Coordenada X del ratón en píxeles.
     * @param mouseY Coordenada Y del ratón en píxeles.
     * @return El objeto {@code Vertice} más cercano, o null si no se encuentra ninguno dentro de un umbral.
     */
    private Vertice buscarVerticeCercano(double mouseX, double mouseY) {
        if (datos == null) return null;
        for (int i = 0; i < datos.getCantidadVertices(); i++) {
            Vertice nodo = datos.getVerticePorIndice(i);
            if (nodo != null) {
                double[] pos = convertirCoordenadasAPixeles(nodo.getLatitud(), nodo.getLongitud());
                // Sensibilidad de 8 píxeles para detectar el nodo
                if (Math.abs(mouseX - pos[0]) <= 8 && Math.abs(mouseY - pos[1]) <= 8) {
                    return nodo;
                }
            }
        }
        return null;
    }

    /**
     * Maneja el evento de clic del ratón en el lienzo del mapa.
     * Permite agregar vehículos o pasajeros según el modo de colocación actual.
     * @param event El evento de ratón.
     */
    private void handleMouseClicked(MouseEvent event) {
        if (this.colocar == 0 || this.datos == null) return;

        /* double mouseX = event.getX();
        double mouseY = event.getY();
        Vertice verticeClickeado = null;

        for (int i = 0; i < datos.getCantidadVertices(); i++) {
            Vertice nodo = datos.getVerticePorIndice(i);

            if (nodo != null) {
                // Aplicamos la transformación de escala (zoom) y desplazamiento (pan) 
                // para que coincida con lo que el usuario ve en pantalla.
                double[] pos = convertirCoordenadasAPixeles(nodo.getLatitud(), nodo.getLongitud());
                double xNodo = pos[0];
                double yNodo = pos[1];

                if (Math.abs(mouseX - xNodo) <= 6 && Math.abs(mouseY - yNodo) <= 6) {
                    verticeClickeado = nodo;
                    break;
                }
            }
        } */
       Vertice verticeClickeado = buscarVerticeCercano(event.getX(), event.getY());

        if (verticeClickeado == null) return;

        if (this.colocar == 1) {
            // --- AGREGAR VEHICULO ---
            // Le pasamos un 'int' arbitrario. Tu constructor lo va a ignorar
            // internamente y va a generar la patente con setID().
            int idArbitrario = this.flota.tamanio() + 1000;

            // Usamos tu constructor (int ID, int posOrigen)
            Vehiculo nuevoVehiculo = new Vehiculo(idArbitrario, verticeClickeado.getIndice());

            this.flota.insertar(nuevoVehiculo, this.flota.tamanio());
            System.out.println("Vehículo agregado en nodo: " + verticeClickeado.getIndice() + " con patente: " + nuevoVehiculo.getID());

            this.colocar = 0;
            this.nodoPreview = null;
            mostrarVehiculosDisponibles();
            dibujarGrafo();

        } else if (this.colocar == 2) {
            // --- AGREGAR ORIGEN DEL PASAJERO ---
            this.origenPasajero = verticeClickeado.getIndice();
            System.out.println("Origen fijado en nodo: " + this.origenPasajero + ". Selecciona el destino.");
            this.colocar = 3;
            dibujarGrafo(); // Para que el pasajero aparezca inmediatamente

        } else if (this.colocar == 3) {
            // --- AGREGAR DESTINO DEL PASAJERO ---
            if (this.origenPasajero == verticeClickeado.getIndice()) {
                System.out.println("El destino debe ser diferente al origen. Elige otro.");
                return;
            }
            // Generamos un ID aleatorio único (ej. entre 1000 y 9999) para el nuevo pasajero manual
            int idUsuarioManual = (int) (Math.random() * 9000) + 1000;
            this.pasajeroActual = new Usuario(idUsuarioManual, this.origenPasajero, verticeClickeado.getIndice());

            System.out.println("Pasajero listo para viajar.");

            this.colocar = 0;
            this.nodoPreview = null;
            this.origenPasajero = -1;
            dibujarGrafo();
            handleNuevaSolicitud();
        }
    }

}
