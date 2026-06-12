package GUI;

import javafx.scene.image.Image;
import javafx.application.Platform;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase encargada de proveer y gestionar los tiles (baldosas) de mapas de OpenStreetMap.
 * Incluye lógica para convertir coordenadas geográficas a índices de tile,
 * así como para descargar y cachear tiles de forma asíncrona.
 */
public class ProveedorTiles {
    /** Cache en memoria para almacenar los tiles de imagen cargados. */
    private final Map<String, Image> cacheMemoria = new ConcurrentHashMap<>();
    /** Conjunto para rastrear las descargas de tiles que están actualmente en curso. */
    private final Set<String> descargasEnCurso = Collections.synchronizedSet(new HashSet<>());
    /** Nombre de la carpeta local para el cache de tiles en disco. */
    private final String CARPETA_CACHE = "cache_mapa";

    /**
     * Constructor de ProveedorTiles.
     * Configura el User-Agent del sistema para las solicitudes HTTP a OpenStreetMap.
     */
    public ProveedorTiles() {
        //Cambiamos el user-agent paar que openStreetMap no nos bloquee
        System.setProperty("http.agent", "ProyectoAyED-UNSA/1.0");
    }
    /**
     * Convierte una longitud geográfica a la coordenada X de un tile de OpenStreetMap
     * para un nivel de zoom dado.
     * @param lon La longitud geográfica.
     * @param zoom El nivel de zoom.
     * @return La coordenada X del tile.
     */
    public int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << zoom));
    }

    /**
     * Convierte una latitud geográfica a la coordenada Y de un tile de OpenStreetMap
     * para un nivel de zoom dado.
     * @param lat La latitud geográfica.
     * @param zoom El nivel de zoom.
     * @return La coordenada Y del tile.
     */
    public int latToTileY(double lat, int zoom) {
        return (int) Math.floor((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 << zoom));
    }

    /**
     * Convierte la coordenada X de un tile de OpenStreetMap de vuelta a una longitud geográfica
     * (representando la esquina superior izquierda del tile).
     * @param x La coordenada X del tile.
     * @param zoom El nivel de zoom.
     * @return La longitud geográfica correspondiente.
     */
    public double tileXToLon(int x, int zoom) {
        return x / (double) (1 << zoom) * 360.0 - 180.0;
    }

    // FÓRMULA 4: Convierte el índice Y de la baldosa de vuelta a Latitud (Esquina Superior Izquierda)
    public double tileYToLat(int y, int zoom) {
        double n = Math.PI - 2.0 * Math.PI * y / (double) (1 << zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /**
     * Obtiene un tile de mapa. Primero busca en la caché de memoria, luego en la caché de disco.
     * Si no lo encuentra, lo descarga de OpenStreetMap de forma asíncrona.
     * @param x La coordenada X del tile.
     * @param y La coordenada Y del tile.
     * @param zoom El nivel de zoom.
     * @param onLoaded Un {@code Runnable} que se ejecuta cuando el tile ha sido cargado (útil para redibujar).
     * @return La imagen del tile si está disponible en caché de memoria, o null si está en proceso de descarga.
     */
    public Image obtenerTile(int x, int y, int zoom, Runnable onLoaded) {
        String tilePath = zoom + "/" + x + "/" + y + ".png";
        
        // 1. Intentar desde Memoria (Rápido)
        if (cacheMemoria.containsKey(tilePath)) {
            return cacheMemoria.get(tilePath);
        }

        // 2. Intentar desde Disco (Persistente)
        File archivoLocal = new File(CARPETA_CACHE, tilePath);
        if (archivoLocal.exists()) {
            Image imgLocal = new Image(archivoLocal.toURI().toString(), true);
            cacheMemoria.put(tilePath, imgLocal);
            configurarListener(imgLocal, onLoaded);
            return imgLocal;
        }

        // 3. Descargar de Internet (Si no está descargándose ya)
        if (!descargasEnCurso.contains(tilePath)) {
            descargasEnCurso.add(tilePath);
            descargarTileAsync(tilePath, archivoLocal, onLoaded);
        }

        return null; // Retornamos null momentáneamente mientras se descarga
    }

    /**
     * Configura un listener para la propiedad de progreso de una imagen, ejecutando un {@code Runnable}
     * cuando la imagen ha terminado de cargar.
     * @param img La imagen a la que se le adjuntará el listener.
     * @param onLoaded El {@code Runnable} a ejecutar al finalizar la carga.
     */
    private void configurarListener(Image img, Runnable onLoaded) {
        if (onLoaded != null) {
            img.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() >= 1.0) {
                    Platform.runLater(onLoaded);
                }
            });
        }
    }

    /**
     * Descarga un tile de OpenStreetMap de forma asíncrona y lo guarda en la caché de disco.
     * @param tilePath La ruta relativa del tile (ej. "zoom/x/y.png").
     * @param destino El archivo local donde se guardará el tile.
     * @param onLoaded Un {@code Runnable} que se ejecuta cuando el tile ha sido descargado y cargado en memoria.
     */
    private void descargarTileAsync(String tilePath, File destino, Runnable onLoaded) {
        Thread hilo = new Thread(() -> {
            try {
                destino.getParentFile().mkdirs();
                URL url = new URL("https://tile.openstreetmap.org/" + tilePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "ProyectoAyED-UNSA/1.0");

                try (InputStream in = connection.getInputStream();
                     OutputStream out = new FileOutputStream(destino)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
                
                // Una vez guardado en disco, lo cargamos a memoria
                Platform.runLater(() -> {
                    Image img = new Image(destino.toURI().toString(), true);
                    cacheMemoria.put(tilePath, img);
                    configurarListener(img, onLoaded);
                    descargasEnCurso.remove(tilePath);
                    onLoaded.run();
                });

            } catch (Exception e) {
                descargasEnCurso.remove(tilePath);
                System.err.println("Error descargando tile: " + tilePath);
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }
}