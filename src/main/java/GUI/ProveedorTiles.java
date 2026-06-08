package GUI;

import javafx.scene.image.Image;
import javafx.application.Platform;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProveedorTiles {
    private final Map<String, Image> cacheMemoria = new ConcurrentHashMap<>();
    private final Set<String> descargasEnCurso = Collections.synchronizedSet(new HashSet<>());
    private final String CARPETA_CACHE = "cache_mapa";

    public ProveedorTiles() {
        //Cambiamos el user-agent paar que openStreetMap no nos bloquee
        System.setProperty("http.agent", "ProyectoAyED-UNSA/1.0");
    }
    // FÓRMULA 1: Convierte Longitud a la columna X del mapa de OpenStreetMap
    public int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << zoom));
    }

    // FÓRMULA 2: Convierte Latitud a la fila Y del mapa de OpenStreetMap
    public int latToTileY(double lat, int zoom) {
        return (int) Math.floor((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * (1 << zoom));
    }

    // FÓRMULA 3: Convierte el índice X de la baldosa de vuelta a Longitud (Esquina Superior Izquierda)
    public double tileXToLon(int x, int zoom) {
        return x / (double) (1 << zoom) * 360.0 - 180.0;
    }

    // FÓRMULA 4: Convierte el índice Y de la baldosa de vuelta a Latitud (Esquina Superior Izquierda)
    public double tileYToLat(int y, int zoom) {
        double n = Math.PI - 2.0 * Math.PI * y / (double) (1 << zoom);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    // Descarga u obtiene la imagen desde la caché de forma asíncrona (no congela la app)
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

    private void configurarListener(Image img, Runnable onLoaded) {
        if (onLoaded != null) {
            img.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() >= 1.0) {
                    Platform.runLater(onLoaded);
                }
            });
        }
    }

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