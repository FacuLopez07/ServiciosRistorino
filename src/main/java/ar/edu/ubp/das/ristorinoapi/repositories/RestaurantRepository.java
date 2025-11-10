package ar.edu.ubp.das.ristorinoapi.repositories;

import ar.edu.ubp.das.ristorinoapi.components.SimpleJdbcCallFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repositorio para obtener el detalle completo de un restaurante vía SP dbo.usp_get_restaurante_detalle.
 */
@Repository
public class RestaurantRepository {

    private static final Logger log = LoggerFactory.getLogger(RestaurantRepository.class);

    @Autowired
    private SimpleJdbcCallFactory simpleJdbcCallFactory;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Ejecuta el SP y devuelve el JSON anidado como JsonNode.
     * @param nroRestaurante restaurante requerido
     * @param nroIdioma idioma preferido (si es null, usa 1)
     * @return JsonNode con el detalle o null si no hay resultados
     */
    public JsonNode getRestaurantDetails(Integer nroRestaurante, Integer nroIdioma) {
        try {
            if (nroRestaurante == null) {
                throw new IllegalArgumentException("nroRestaurante es requerido");
            }
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("nro_restaurante", nroRestaurante)
                    .addValue("nro_idioma", nroIdioma == null ? 1 : nroIdioma);

            Map<String, Object> out = simpleJdbcCallFactory.executeReturningEverything(
                    "usp_get_restaurante_detalle",
                    "dbo",
                    params
            );

            Object rs1 = out.get("#result-set-1");
            if (!(rs1 instanceof List)) {
                log.warn("El SP no retornó result set esperado (#result-set-1)");
                return null;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) rs1;
            if (rows.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> row : rows) {
                Object firstVal = row.values().stream().findFirst().orElse(null);
                if (firstVal != null) sb.append(firstVal);
            }
            String json = sb.toString();
            if (json.isEmpty()) {
                return null;
            }
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Error obteniendo detalle de restaurante: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener detalle del restaurante", e);
        }
    }
}

