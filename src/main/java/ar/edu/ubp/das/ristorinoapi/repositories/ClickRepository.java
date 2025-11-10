package ar.edu.ubp.das.ristorinoapi.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para registrar clicks sobre contenidos promocionales.
 * Envuelve llamados a procedimientos almacenados y normaliza el JSON de respuesta.
 */
@Repository
public class ClickRepository {

    private static final Logger log = LoggerFactory.getLogger(ClickRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Ejecuta el procedimiento almacenado para registrar un click anónimo.
     * Devuelve un Map con keys "click" y "contenido" mapeadas como objetos (no strings JSON).
     * @param nroRestaurante restaurante asociado
     * @param nroIdioma idioma del contenido
     * @param nroContenido identificador del contenido
     * @param fechaRegistro fecha/hora a registrar; si es null la define el SP
     * @return mapa con la respuesta del SP parseada a estructuras Java
     */
    public Map<String, Object> registerAnonymousClick(Integer nroRestaurante,
                                                      Integer nroIdioma,
                                                      Integer nroContenido,
                                                      LocalDateTime fechaRegistro) {
        if (nroRestaurante == null || nroIdioma == null || nroContenido == null) {
            throw new IllegalArgumentException("Parametros requeridos nroRestaurante, nroIdioma y nroContenido no pueden ser null");
        }
        String sql = "EXEC dbo.usp_registrar_click_contenido_restaurante ?, ?, ?, ?, ?";
        return jdbcTemplate.query(con -> {
            var ps = con.prepareStatement(sql);
            ps.setInt(1, nroRestaurante);
            ps.setInt(2, nroIdioma);
            ps.setInt(3, nroContenido);
            if (fechaRegistro != null) {
                ps.setTimestamp(4, Timestamp.valueOf(fechaRegistro));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setNull(5, Types.INTEGER); // nro_cliente = NULL (anónimo)
            return ps;
        }, rs -> {
            // El SP termina con FOR JSON PATH, WITHOUT_ARRAY_WRAPPER, así que devuelve 1 columna con el JSON completo.
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                Object first = rs.getObject(1);
                if (first != null) sb.append(String.valueOf(first));
            }
            String json = sb.toString();
            if (json.isEmpty()) return Map.of();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
                return parsed;
            } catch (Exception ex) {
                // Si algo falla en el parse, devolvemos el JSON crudo para facilitar el diagnóstico
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("raw", json);
                fallback.put("error", "No se pudo parsear JSON devuelto: " + ex.getMessage());
                return fallback;
            }
        });
    }


    /**
     * Obtiene todos los clicks no notificados (notificado = 0) opcionalmente filtrando por restaurante, idioma o contenido.
     * Devuelve lista de mapas con las claves combinadas de 'click' y 'contenido'.
     * Cada elemento original del procedimiento trae { click: {...}, contenido: {...} } pero, por FOR JSON anidado,
     * a menudo vienen como strings JSON y hay que parsearlos.
     */
    public List<Map<String, Object>> getUnnotifiedClicks(Integer nroRestaurante, Integer nroIdioma, Integer nroContenido) {
        String sql = "EXEC dbo.usp_get_clicks_no_notificados ?, ?, ?";
        String json = jdbcTemplate.query(con -> {
            var ps = con.prepareStatement(sql);
            if (nroRestaurante != null) ps.setInt(1, nroRestaurante); else ps.setNull(1, Types.INTEGER);
            if (nroIdioma != null) ps.setInt(2, nroIdioma); else ps.setNull(2, Types.INTEGER);
            if (nroContenido != null) ps.setInt(3, nroContenido); else ps.setNull(3, Types.INTEGER);
            return ps;
        }, rs -> {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                Object first = rs.getObject(1);
                if (first != null) sb.append(first);
            }
            return sb.toString();
        });
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> raw = objectMapper.readValue(json, List.class);
            List<Map<String, Object>> flattened = new ArrayList<>();
            for (Map<String, Object> entry : raw) {
                Map<String, Object> flat = new HashMap<>();
                Object clickObj = entry.get("click");
                Object contenidoObj = entry.get("contenido");

                // click puede venir como Map o como String JSON
                if (clickObj instanceof Map<?, ?> clickMap) {
                    clickMap.forEach((k, v) -> flat.put(String.valueOf(k), v));
                } else if (clickObj instanceof String clickJson && !clickJson.isBlank()) {
                    try {
                        @SuppressWarnings("unchecked") Map<String, Object> clickParsed = objectMapper.readValue(clickJson, Map.class);
                        clickParsed.forEach((k, v) -> flat.put(String.valueOf(k), v));
                    } catch (Exception ex) {
                        log.warn("No se pudo parsear 'click' embebido: {}", ex.getMessage());
                    }
                }

                // contenido puede venir como Map o como String JSON
                if (contenidoObj instanceof Map<?, ?> contMap) {
                    contMap.forEach((k, v) -> flat.put(String.valueOf(k), v));
                } else if (contenidoObj instanceof String contJson && !contJson.isBlank()) {
                    try {
                        @SuppressWarnings("unchecked") Map<String, Object> contParsed = objectMapper.readValue(contJson, Map.class);
                        contParsed.forEach((k, v) -> flat.put(String.valueOf(k), v));
                    } catch (Exception ex) {
                        log.warn("No se pudo parsear 'contenido' embebido: {}", ex.getMessage());
                    }
                }

                flattened.add(flat);
            }
            return flattened;
        } catch (Exception e) {
            log.error("Error parseando JSON de clicks no notificados: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", json);
            fallback.put("error", e.getMessage());
            return List.of(fallback);
        }
    }

    /**
     * Marca un click específico como notificado ejecutando el SP de confirmación.
     * Devuelve true si se actualizó (@@ROWCOUNT = 1), false si no había fila pendiente.
     */
    public boolean confirmClickNotified(Integer nroRestaurante, Integer nroIdioma, Integer nroContenido, Integer nroClick) {
        String sql = "EXEC dbo.usp_confirmar_click_notificado ?, ?, ?, ?";
        String json = jdbcTemplate.query(con -> {
            var ps = con.prepareStatement(sql);
            ps.setInt(1, nroRestaurante);
            ps.setInt(2, nroIdioma);
            ps.setInt(3, nroContenido);
            ps.setInt(4, nroClick);
            return ps;
        }, rs -> {
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                Object first = rs.getObject(1);
                if (first != null) sb.append(first);
            }
            return sb.toString();
        });
        if (json == null || json.isEmpty()) return false;
        try {
            @SuppressWarnings("unchecked") Map<String,Object> parsed = objectMapper.readValue(json, Map.class);
            Object act = parsed.get("actualizado");
            if (act instanceof Number n) {
                return n.intValue() > 0;
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear confirmación de click: {}", e.getMessage());
        }
        return false;
    }
}
