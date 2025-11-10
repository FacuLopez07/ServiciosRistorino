package ar.edu.ubp.das.ristorinoapi.services;

import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servicio manual para notificar a los restaurantes los clicks pendientes y marcarlos como notificados.
 * Este servicio NO se ejecuta automáticamente; debe invocarse manualmente (línea de comando o endpoint protected).
 */
@Service
public class ClickNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ClickNotificationService.class);

    @Autowired
    private ClickRepository clickRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // URL destino del restaurante (por ahora fija, podría parametrizarse/por restaurante)
    private static final String DEST_URL = "http://localhost:8085/api/v1/clicks";

    /**
     * Recorre todos los clicks no notificados y los notifica uno a uno al restaurante correspondiente.
     * Tras éxito de la notificación, marca el click como notificado en la base de datos.
     * Devuelve la cantidad de notificaciones realizadas con éxito.
     */
    public int notifyAllPendingClicks(Integer nroRestauranteFilter) {
        List<Map<String, Object>> rows = clickRepository.getUnnotifiedClicks(nroRestauranteFilter, null, null);
        if (rows.isEmpty()) {
            log.info("No hay clicks pendientes de notificar.");
            return 0;
        }
        int okCount = 0;
        for (Map<String, Object> row : rows) {
            // Extraer datos necesarios
            Integer nroRestaurante = asInt(row.get("nro_restaurante"));
            Integer nroIdioma = asInt(row.get("nro_idioma"));
            Integer nroContenido = asInt(row.get("nro_contenido"));
            Integer nroClick = asInt(row.get("nro_click"));
            Double costoClick = asDouble(row.getOrDefault("costo_click", row.get("costoClick")));

            if (nroRestaurante == null || nroContenido == null || nroClick == null) {
                log.warn("Fila inválida, faltan claves: {}", row);
                continue;
            }

            try {
                // Construir payload para el restaurante
                Map<String, Object> payload = Map.of(
                        "nroRestaurante", nroRestaurante,
                        "nroContenido", nroContenido,
                        "costoClick", costoClick != null ? costoClick : 0.0
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

                // POST al restaurante
                var resp = restTemplate.postForEntity(DEST_URL, req, String.class);
                if (resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode().value() == 201) {
                    boolean updated = clickRepository.confirmClickNotified(nroRestaurante, nroIdioma, nroContenido, nroClick);
                    if (updated) {
                        okCount++;
                        log.info("Click {} notificado y confirmado.", nroClick);
                    } else {
                        log.warn("Click {} notificado pero no se confirmó en BD.", nroClick);
                    }
                } else {
                    log.warn("Falla notificando click {}: status {}", nroClick, resp.getStatusCode());
                }
            } catch (Exception ex) {
                log.error("Error notificando click {}: {}", nroClick, ex.getMessage());
            }
        }
        log.info("Notificaciones exitosas: {} de {}", okCount, rows.size());
        return okCount;
    }

    private Integer asInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o != null ? Integer.parseInt(o.toString()) : null; } catch (Exception e) { return null; }
    }
    private Double asDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return o != null ? Double.parseDouble(o.toString()) : null; } catch (Exception e) { return null; }
    }
}

