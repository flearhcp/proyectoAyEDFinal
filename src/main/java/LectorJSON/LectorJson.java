package LectorJSON;

import org.json.JSONArray;
//import org.json.JSONException;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
//notas: debo cambiar todas las arrayList por las clases de listas enlazadas de la catedra y cargar todas las coordenadas de las calles para dibujar el mapa
public class LectorJson {

    private JSONObject json;

    public LectorJson(String ruta) throws Exception {

        String contenido = new String(Files.readAllBytes(Paths.get(ruta)));
        json = new JSONObject(contenido);
    }

    public DatosMapa generarDatosMapa() throws Exception {
        //Actualizaciones: Con esta actualizacion los nodos se cargan sin que se repita las calles.
        JSONArray elements = json.getJSONArray("elements");
        /*JSONArray elementsOriginales = json.getJSONArray("elements");
        Map<Long, JSONObject> coordenadasNodos = new HashMap<>();
        List<JSONObject> listaWays = new ArrayList<>();
        Map<Long,Coordenada> coordenadasNodos1 = new HashMap<>();

        for (int i = 0; i < elementsOriginales.length(); i++) {
            JSONObject obj = elementsOriginales.getJSONObject(i);
            String type = obj.getString("type");
            
            if (type.equals("node")) {
                long nodeId = obj.getLong("id");
                double latitud = obj.getDouble("lat");
                double longitud = obj.getDouble("lon");
                coordenadasNodos1.put(nodeId, new Coordenada(latitud, longitud));
                coordenadasNodos.put(nodeId, obj); // Guardamos el nodo completo
            } else if (type.equals("way") && obj.has("tags")) {
                JSONObject tags = obj.getJSONObject("tags");
                if (tags.has("name")) {
                    listaWays.add(obj);
                }
            }
        }

        // 2. UNIFICAR TRAMOS POR NOMBRE
        Map<String, List<JSONObject>> callesAgrupadas = new HashMap<>();
        for (JSONObject way : listaWays) {
            String nombre = way.getJSONObject("tags").getString("name");
            callesAgrupadas.computeIfAbsent(nombre, k -> new ArrayList<>()).add(way);
        }

        // Reconstruimos el JSONArray de elementos pero ya unificados
        JSONArray elements = new JSONArray();

        callesAgrupadas.forEach((nombreCalle, tramos) -> {
            JSONObject calleUnificada = new JSONObject();
            try {
                calleUnificada.put("type", "way");
                calleUnificada.put("id", tramos.get(0).getLong("id")); // Mantiene el primer ID
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Fusionar nodos secuencialmente evitando duplicados en empalmes
            List<Long> nodosFusionados = new ArrayList<>();
            for (JSONObject tramo : tramos) {
                JSONArray nodesTramo = new JSONArray();
                try {
                    nodesTramo = tramo.getJSONArray("nodes");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < nodesTramo.length(); j++) {
                    long idNodo;
                    try {
                        idNodo = nodesTramo.getLong(j);
                        if (nodosFusionados.isEmpty() || nodosFusionados.get(nodosFusionados.size() - 1) != idNodo) {
                            nodosFusionados.add(idNodo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                calleUnificada.put("nodes", new JSONArray(nodosFusionados));
                calleUnificada.put("tags", tramos.get(0).getJSONObject("tags")); // Conserva tags base
                elements.put(calleUnificada);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
            */
        // CONTAR APARICIONES DE NODOS
        Map<Long, Integer> apariciones = new HashMap<>();
        Map<Long,Coordenada> coordenadasNodos1 = new HashMap<>();

        for (int i = 0; i < elements.length(); i++) {
            JSONObject obj = elements.getJSONObject(i);

            if (obj.getString("type").equals("node")) {
                long nodeId = obj.getLong("id");
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                coordenadasNodos1.put(nodeId, new Coordenada(lat, lon));
            } else if (obj.getString("type").equals("way")) {
                JSONArray nodes = obj.getJSONArray("nodes");
                JSONArray geometry = obj.optJSONArray("geometry");

                for (int j = 0; j < nodes.length(); j++) {
                    long nodeId = nodes.getLong(j);
                    apariciones.put(nodeId, apariciones.getOrDefault(nodeId, 0) + 1);
                    
                    // Guardamos la coordenada leyendo el arreglo "geometry"
                    if (geometry != null && !geometry.isNull(j)) {
                        JSONObject geo = geometry.getJSONObject(j);
                        coordenadasNodos1.put(nodeId, new Coordenada(geo.getDouble("lat"), geo.getDouble("lon")));
                    }
                }
            }
        }

        // ASIGNAR INDICES A INTERSECCIONES Y EXTREMOS
        Map<Long, Vertice> vertices = new HashMap<>();
        int contadorVertices = 0;

        for (int i = 0; i < elements.length(); i++) {
            JSONObject obj = elements.getJSONObject(i);

            if (obj.getString("type").equals("way")) {
                JSONArray nodes = obj.getJSONArray("nodes");

                for (int j = 0; j < nodes.length(); j++) {

                    long nodeId = nodes.getLong(j);
                    boolean esExtremo = (j == 0 || j == nodes.length() - 1);
                    boolean esInterseccion = apariciones.get(nodeId) > 1;  //si el nodo aparece en mas de una calle

                    if (esExtremo || esInterseccion) {

                        if (!vertices.containsKey(nodeId)) {
                            Coordenada coord = coordenadasNodos1.getOrDefault(nodeId,Coordenada.INVALIDA);
                            
                            // Solo agregamos el vértice si la coordenada es válida (evita comprimir el mapa)
                            if (coord.esValida()) {
                                Vertice vertice = new Vertice(contadorVertices++, nodeId, coord);
                                vertices.put(nodeId,vertice);
                            }
                        }
                    }
                }
            }
        }

        // CREAR aristas
        List <Arista> listaAristas = new ArrayList<>();

        for (int i = 0; i < elements.length(); i++) {
            JSONObject obj = elements.getJSONObject(i);

            if (obj.getString("type").equals("way")) {
                JSONArray nodes = obj.getJSONArray("nodes");
                JSONObject tags = obj.optJSONObject("tags");
                boolean oneway = tags.optString("oneway", "no").equals("yes");
                String nombre = tags.optString("name", "S/N");
                String tipo = tags.optString("highway", "unknown");
                int velocidad =tags.optInt("maxspeed", obtenerVelocidadPorDefecto(tipo));
                // Convertimos la velocidad de Km/h a Metros por Segundo (m/s) dividiendo por 3.6
                double velocidadMS = velocidad / 3.6;
                Calle calle =new Calle(obj.getLong("id"), nombre, oneway,velocidad,tipo);


                long ultimoVertice = -1;
                // Variables para calcular la distancia acumulada de la cuadra
                double distanciaAcumulada = 0.0;
                Coordenada coordAnterior = null;
                for (int j = 0; j < nodes.length();j++) {
                    long actual = nodes.getLong(j);
                    Coordenada coordActual = coordenadasNodos1.get(actual);

                    // 2. ACUMULAR DISTANCIA: Sumamos la distancia desde el nodo anterior al actual
                    // Esto incluye los "nodos curvos" del medio para que la longitud sea perfecta.
                    if (coordAnterior != null && coordActual != null) {
                        distanciaAcumulada += calcularDistanciaHaversine(coordAnterior, coordActual);
                    }
                    // Avanzamos el puntero de coordenadas
                    coordAnterior = coordActual;

                    boolean esVertice = vertices.containsKey(actual);

                    if (!esVertice) {
                        continue;// Si no es una esquina, pasamos al siguiente nodo de la cuadra
                    }

                    // primer vertice del 'way'/tramo
                    if (ultimoVertice == -1) {
                        ultimoVertice = actual;
                        distanciaAcumulada = 0.0; // Reseteamos la distancia desde esta esquina inicial
                        continue;
                    }
                    Vertice verticeU = vertices.get(ultimoVertice);
                    Vertice verticeV = vertices.get(actual);
                    // CALCULAR EL PESO (ETA)
                    // FÍSICA: Tiempo (segundos) = Distancia (metros) / Velocidad (m/s)
                    double tiempoEtaSegundos = distanciaAcumulada / velocidadMS;

                    verticeU.agregarCalle(calle);
                    verticeV.agregarCalle(calle);

                    // 4. GUARDAR LA ARISTA CON SUS NUEVOS VALORES CALCULADOS
                    listaAristas.add(new Arista(verticeU, verticeV, calle, distanciaAcumulada, tiempoEtaSegundos));

                    // Si es doble mano, agregamos el viaje de vuelta (con el mismo peso/tiempo)
                    if (!oneway) {
                        listaAristas.add(new Arista(verticeV, verticeU, calle, distanciaAcumulada, tiempoEtaSegundos));
                    }

                    // Nos preparamos para la siguiente cuadra de la misma calle
                    ultimoVertice = actual;
                    distanciaAcumulada = 0.0; // Reseteamos la distancia para empezar la próxima cuadra
                }
            }
        }
        List<Vertice> listaVertices = new ArrayList<>(vertices.values());
        listaVertices.sort((v1,v2)-> Integer.compare(v1.getIndice(), v2.getIndice()));
        return new DatosMapa(listaVertices,listaAristas);
    }
    
    private int obtenerVelocidadPorDefecto(String tipo){
        int vel;
        switch (tipo) {
            case "primary": vel = 60; break;
            case "secondary":vel = 40; break;
            case "tertiary": vel = 40; break;
            case "living_street": vel = 20;break;
            case "residential": vel = 40; break;             
            default: vel = 30;break;
        }
        return vel;
    }
    private double calcularDistanciaHaversine(Coordenada c1, Coordenada c2) {
        if (!c1.esValida() || !c2.esValida()) return 0.0;

        final int R = 6371000; // Radio de la Tierra en metros
        double lat1 = Math.toRadians(c1.getLatitud());
        double lon1 = Math.toRadians(c1.getLongitud());
        double lat2 = Math.toRadians(c2.getLatitud());
        double lon2 = Math.toRadians(c2.getLongitud());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Devuelve la distancia en metros
    }
}
