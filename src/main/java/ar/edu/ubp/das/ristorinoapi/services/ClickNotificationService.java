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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Servicio manual para notificar a los restaurantes los clicks pendientes y marcarlos como notificados.
 * Este servicio NO se ejecuta automáticamente; debe invocarse manualmente (línea de comando o endpoint protegido).
 *
 * Contrato actual de notificación (API restaurante):
 * POST http://localhost:8085/api/v1/clicks
 * Body: { "codContenidoRestaurante": "MilaPapaBeb_1", "costoClick": 42.50 }
 *
 * Autenticación actual: JWT Bearer con secreto HS256 y payload fijo {"registrador":"ristorino"}.
 */
@Service
public class ClickNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ClickNotificationService.class);

    @Autowired
    private ClickRepository clickRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // URL destino del restaurante (por ahora fija, podría parametrizarse/por restaurante)
    private static final String DEST_URL = "http://localhost:8085/api/v1/clicks";

    // Secreto provisto para generar el token JWT HS256
    private static final String JWT_SECRET = "ClaveSuperDuperHiperMegaSecreta12345";
    // Duración del token en segundos (configurable). Evitamos generar token nuevo por cada click.
    private static final long TOKEN_TTL_SECONDS = 300; // 5 minutos

    // Cache simple del token actual y su expiración Unix epoch seconds
    private String cachedToken;
    private long cachedTokenExpEpoch;

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
        String bearerToken = getJwtToken();
        for (Map<String, Object> row : rows) {
            // Extraer datos necesarios desde 'click'
            Integer nroRestaurante = asInt(row.get("nro_restaurante"));
            Integer nroIdioma = asInt(row.get("nro_idioma"));
            Integer nroContenido = asInt(row.get("nro_contenido"));
            Integer nroClick = asInt(row.get("nro_click"));
            Double costoClick = asDouble(row.getOrDefault("costo_click", row.get("costoClick")));
            // Extraer código del contenido (del bloque 'contenido')
            Object codContenidoObj = row.getOrDefault("cod_contenido_restaurante", row.get("codContenidoRestaurante"));
            String codContenidoRestaurante = codContenidoObj != null ? codContenidoObj.toString() : null;

            if (nroRestaurante == null || nroContenido == null || nroClick == null) {
                log.warn("Fila inválida, faltan claves: {}", row);
                continue;
            }
            if (codContenidoRestaurante == null || codContenidoRestaurante.isBlank()) {
                log.warn("Fila sin cod_contenido_restaurante, se omite notificación: click={}", nroClick);
                continue;
            }

            try {
                // Construir payload para el restaurante según contrato vigente
                Map<String, Object> payload = Map.of(
                        "codContenidoRestaurante", codContenidoRestaurante,
                        "costoClick", costoClick != null ? costoClick : 0.0
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.setBearerAuth(bearerToken); // Authorization: Bearer <token>
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
                    log.warn("Falla notificando click {}: status {} body {}", nroClick, resp.getStatusCode(), resp.getBody());
                }
            } catch (Exception ex) {
                log.error("Error notificando click {}: {}", nroClick, ex.getMessage());
            }
        }
        log.info("Notificaciones exitosas: {} de {}", okCount, rows.size());
        return okCount;
    }

    /**
     * Obtiene un token JWT HS256 con payload fijo {"registrador":"ristorino"}. Regenera cuando expira.
     */
    private synchronized String getJwtToken() {
        long now = Instant.now().getEpochSecond();
        if (cachedToken != null && now < cachedTokenExpEpoch) {
            return cachedToken;
        }
        long exp = now + TOKEN_TTL_SECONDS;
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format("{\"registrador\":\"%s\",\"iat\":%d,\"exp\":%d}", "ristorino", now, exp);
        String headerB64 = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String toSign = headerB64 + "." + payloadB64;
        String signatureB64 = hmacSha256Base64Url(toSign, JWT_SECRET);
        cachedToken = toSign + "." + signatureB64;
        cachedTokenExpEpoch = exp - 5; // margen de seguridad
        return cachedToken;
    }

    private String hmacSha256Base64Url(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64Url(raw);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar firma JWT", e);
        }
    }

    private String base64Url(byte[] bytes) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        // JWT base64url: reemplaza + / y quita =
        return b64.replace('+', '-').replace('/', '_').replaceAll("=+$", "");
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
