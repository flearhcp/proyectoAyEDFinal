package LectorJSON;

import org.json.JSONArray;
//import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.*;
import java.util.*;
//notas: debo cambiar todas las arrayList por las clases de listas enlazadas de la catedra y cargar todas las coordenadas de las calles para dibujar el mapa
public class LectorJson {

    private JSONObject json;

    public LectorJson(String ruta) throws Exception {

        InputStream is = getClass().getResourceAsStream(ruta);

        // Si no se encuentra como recurso interno, intentamos cargarlo desde el sistema de archivos
        if (is == null) {
            File f = new File(ruta);
            if (f.exists()) {
                is = new FileInputStream(f);
            }
        }

        if(is == null){
            throw  new RuntimeException("¡Error critico! No se pudo cargar el recurso o archivo: " + ruta);
        }
        JSONTokener tokener = new JSONTokener(is);
        this.json = new JSONObject(tokener);
    }

    public DatosMapa generarDatosMapa() throws Exception {
        JSONArray elements = json.getJSONArray("elements");

        // CONTAR APARICIONES DE NODOS
        Map<Long, Integer> apariciones = new HashMap<>();
        Map<Long,Coordenada> coordenadasNodos1 = new HashMap<>();
        List<JSONObject> listaWays = new ArrayList<>();

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
        Map<String,Calle> diccionarioCalles = new HashMap<>();

        for (int i = 0; i < elements.length(); i++) {
            JSONObject obj = elements.getJSONObject(i);

            if (obj.getString("type").equals("way")) {
                JSONArray nodes = obj.getJSONArray("nodes");
                JSONObject tags = obj.optJSONObject("tags");
                //Normalizacion del nombre
                String nombreOriginal = (tags != null) ? tags.optString("name","Calle sin nombre"): "Calle sin nombre";
                String nombreNorm = normalizarNombre(nombreOriginal);
                String shortName = (tags != null) ? tags.optString("short_name", null) : null;

                boolean oneway = tags.optString("oneway", "no").equals("yes");
                String tipo = tags.optString("highway", "unknown");
                int velocidad =tags.optInt("maxspeed", obtenerVelocidadPorDefecto(tipo));
                double velocidadMs = velocidad / 3.6;
                Calle calle = diccionarioCalles.computeIfAbsent(nombreNorm, k -> new Calle(obj.optLong("id"), nombreNorm, shortName, oneway, velocidad, tipo));


                long ultimoVertice = -1;
                int indiceUltimoVertice = 0;

                double distanciaAcumulada = 0.0;
                Coordenada coorAnterior = null;

                for (int j = 0; j < nodes.length();j++) {
                    long actual = nodes.getLong(j);
                    boolean esVertice = vertices.containsKey(actual);
                    Coordenada coordActual = coordenadasNodos1.get(actual);

                    if (coorAnterior != null && coordActual != null){
                        distanciaAcumulada += calcularDistanciaHaversine(coorAnterior,coordActual);
                    }
                    coorAnterior = coordActual;

                    if (!esVertice) {
                        continue;
                    }

                    // primer vertice del 'way'/tramo
                    if (ultimoVertice == -1) {
                        ultimoVertice = actual;
                        distanciaAcumulada = 0.0;
                        continue;
                    }
                    List<Coordenada> geometriaTramo = new ArrayList<>();
                    for (int k = indiceUltimoVertice; k <= j; k++) {
                        long idNodo = nodes.getLong(k);
                        geometriaTramo.add(coordenadasNodos1.get(idNodo));
                    }
                    Vertice verticeU = vertices.get(ultimoVertice);
                    Vertice verticeV = vertices.get(actual);

                    double tiempoETASegundos = distanciaAcumulada / velocidadMs;

                    verticeU.agregarCalle(calle);
                    verticeV.agregarCalle(calle);
                    
                    listaAristas.add(new Arista(verticeU, verticeV, calle,distanciaAcumulada,tiempoETASegundos,geometriaTramo));
                    // doble mano
                    if (!oneway) {
                        List<Coordenada> geometriaInvertida = new ArrayList<>(geometriaTramo);
                        Collections.reverse(geometriaInvertida);
                        listaAristas.add(new Arista(verticeV, verticeU, calle,distanciaAcumulada,tiempoETASegundos,geometriaInvertida));
                    }
                    ultimoVertice = actual;
                    indiceUltimoVertice=j;
                    distanciaAcumulada= 0.0;
                }
            }
        }
        List<Vertice> listaVertices = new ArrayList<>(vertices.values());
        this.json = null; // Liberar la memoria del árbol JSON original
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
    private String normalizarNombre (String nombre){
        if(nombre == null) return "Desconocida";

        String norm = nombre.toLowerCase().trim().replaceAll("\\s+"," ");
        norm = norm.replace("av.","avenida");
        norm = norm.replace("av ","avenida");
        norm = norm.replace("gral.","general");
        norm = norm.replace("pje.","pasaje");

        return norm;
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
