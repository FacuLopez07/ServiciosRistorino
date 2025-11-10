package ar.edu.ubp.das.ristorinoapi.repositories;

import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import ar.edu.ubp.das.ristorinoapi.components.SimpleJdbcCallFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Repositorio encargado de obtener las promociones de un restaurante vía SP SQL Server.
 * El procedimiento {@code dbo.usp_get_promociones_restaurante} devuelve un JSON (FOR JSON PATH)
 * que se parsea y normaliza para mapearlo a {@link RestaurantResponse}.
 */
@Repository
public class PromotionRepository {

    private static final Logger logger = LoggerFactory.getLogger(PromotionRepository.class);

    @Autowired
    private SimpleJdbcCallFactory simpleJdbcCallFactory;

    private final Gson gson = new Gson();

    /**
     * Obtiene promociones para el restaurante default (id=1) sin filtros.
     * Delegado al método parametrizado para mantener una única lógica.
     */
    public RestaurantResponse getPromotionsWithRestaurant() {
        // Por ahora usamos el restaurante 1, sin filtro de vigencia ni sucursal
        return getPromotionsWithRestaurant(1, null, null);
    }

    /**
     * Obtiene promociones invocando el SP con parámetros.
     * @param nroRestaurante id del restaurante (obligatorio)
     * @param soloVigentes true para filtrar solo vigentes, null equivale a false
     * @param nroSucursal sucursal específica o null para todas/globales
     * @return respuesta mapeada a {@link RestaurantResponse}
     */
    public RestaurantResponse getPromotionsWithRestaurant(Integer nroRestaurante, Boolean soloVigentes, Integer nroSucursal) {
        try {
            logger.info("Ejecutando SP: dbo.usp_get_promociones_restaurante");

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("nro_restaurante", nroRestaurante)
                    .addValue("soloVigentes", soloVigentes == null ? 0 : (soloVigentes ? 1 : 0))
                    .addValue("nro_sucursal", nroSucursal);

            // Ejecutar y recuperar todo (incluye result sets)
            Map<String, Object> out = simpleJdbcCallFactory.executeReturningEverything(
                    "usp_get_promociones_restaurante",
                    "dbo",
                    params
            );

            // SQL Server suele devolver el FOR JSON en el primer result set como filas que pueden venir fragmentadas.
            // Clave convencional: "#result-set-1"
            Object rs1 = out.get("#result-set-1");
            if (!(rs1 instanceof List)) {
                throw new RuntimeException("El procedimiento no devolvió el result set esperado (#result-set-1)");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) rs1;
            if (rows.isEmpty()) {
                throw new RuntimeException("No se obtuvieron resultados del procedimiento");
            }

            // Concatenar los fragmentos (cada fila trae un único valor: el trozo de JSON)
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> row : rows) {
                // Tomamos el primer valor de la fila (columna sin alias en el SP)
                Object firstVal = row.values().stream().findFirst().orElse(null);
                if (firstVal != null) {
                    sb.append(String.valueOf(firstVal));
                }
            }

            String jsonResult = sb.toString();
            logger.debug("JSON obtenido (longitud={}): {}", jsonResult.length(), jsonResult.length() < 2048 ? jsonResult : "<omitted>");

            if (jsonResult.isEmpty()) {
                throw new RuntimeException("El JSON devuelto está vacío");
            }

            // Normalizar JSON cuando 'contenidos' viene doblemente serializado (string)
            JsonObject root = JsonParser.parseString(jsonResult).getAsJsonObject();
            if (root.has("contenidos")) {
                JsonElement contenidos = root.get("contenidos");
                if (contenidos.isJsonPrimitive() && contenidos.getAsJsonPrimitive().isString()) {
                    try {
                        JsonElement parsed = JsonParser.parseString(contenidos.getAsString());
                        root.add("contenidos", parsed);
                        logger.debug("Campo 'contenidos' normalizado desde string JSON a arreglo/objeto JSON.");
                    } catch (Exception ex) {
                        logger.warn("No se pudo parsear 'contenidos' como JSON embebido: {}", ex.getMessage());
                    }
                }
            }

            // Parsear a objeto usando Gson
            RestaurantResponse restaurantResponse = gson.fromJson(root, RestaurantResponse.class);

            if (restaurantResponse == null) {
                throw new RuntimeException("No se pudo parsear el JSON a RestaurantResponse");
            }

            if (restaurantResponse.getContenidos() == null) {
                logger.warn("'contenidos' vino nulo en el JSON");
            } else {
                logger.info("Número de contenidos: {}", restaurantResponse.getContenidos().size());
            }

            return restaurantResponse;

        } catch (Exception e) {
            logger.error("Error al obtener promociones: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener promociones desde la base de datos", e);
        }
    }
}

