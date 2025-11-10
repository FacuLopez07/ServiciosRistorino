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
     * Registra click anónimo dado solo el nro_contenido. Resuelve restaurante e idioma.
     * Asume que nro_contenido identifica una única fila en contenidos_restaurantes; si hay varias, toma la primera.
     * @param nroContenido id del contenido a registrar click
     * @param fechaRegistro fecha/hora del click o null
     * @return respuesta parseada del SP
     */
    public Map<String, Object> registerAnonymousClickByContenido(Integer nroContenido, LocalDateTime fechaRegistro) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT TOP 1 nro_restaurante, nro_idioma FROM dbo.contenidos_restaurantes WHERE nro_contenido = ? ORDER BY nro_restaurante, nro_idioma",
                nroContenido
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Contenido inexistente para nro_contenido=" + nroContenido);
        }
        Map<String, Object> row = rows.get(0);
        Number nroRestN = (Number) row.get("nro_restaurante");
        Number nroIdioN = (Number) row.get("nro_idioma");
        if (nroRestN == null || nroIdioN == null) {
            throw new IllegalStateException("No se pudo resolver nro_restaurante/nro_idioma para nro_contenido=" + nroContenido);
        }
        Integer nroRestaurante = nroRestN.intValue();
        Integer nroIdioma = nroIdioN.intValue();
        log.info("Registrando click anónimo: contenido={}, restaurante={}, idioma={}", nroContenido, nroRestaurante, nroIdioma);
        return registerAnonymousClick(nroRestaurante, nroIdioma, nroContenido, fechaRegistro);
    }
}
