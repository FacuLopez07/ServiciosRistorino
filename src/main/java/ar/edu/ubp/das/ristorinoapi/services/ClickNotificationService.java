package ar.edu.ubp.das.ristorinoapi.services;

import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Servicio para notificar a la API externa de los restaurantes los clicks pendientes de confirmación.
 * <p>Recorre los registros no notificados y realiza POST al endpoint remoto. Si el POST es exitoso se marca
 * el click como notificado en la base de datos. El endpoint remoto actualmente espera un JSON sencillo con
 * el código externo del contenido y el costo del click.</p>
 * <p>La autenticación se basa en JWT HS256 generado localmente con un secreto compartido y payload fijo
 * que incluye campos estándar iat/exp para control de expiración.</p>
 * <p>Este servicio fue concebido para ejecución manual (endpoint protegido o tarea administrativa); no se
 * programa todavía como tarea automática/scheduler.</p>
 */
@Service
public class ClickNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ClickNotificationService.class);

    @Autowired
    private ClickRepository clickRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // URL destino del restaurante y secreto JWT ahora externalizados en application.properties
    @Value("${ristorino.notification.dest-url}")
    private String destUrl;
    @Value("${ristorino.notification.jwt-secret}")
    private String jwtSecret;
    @Value("${ristorino.notification.jwt-ttl-seconds:300}")
    private long tokenTtlSeconds;

    // Cache simple del token actual y su expiración (epoch seconds)
    private String cachedToken;
    private long cachedTokenExpEpoch;

    /**
     * Notifica todos los clicks no notificados obtenidos desde la base de datos.
     * @param nroRestauranteFilter filtro opcional por restaurante (null = todos)
     * @return cantidad de clicks notificados exitosamente
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
            Integer nroRestaurante = asInt(row.get("nro_restaurante"));
            Integer nroIdioma = asInt(row.get("nro_idioma"));
            Integer nroContenido = asInt(row.get("nro_contenido"));
            Integer nroClick = asInt(row.get("nro_click"));
            Double costoClick = asDouble(row.getOrDefault("costo_click", row.get("costoClick")));
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
                Map<String, Object> payload = Map.of(
                        "codContenidoRestaurante", codContenidoRestaurante,
                        "costoClick", costoClick != null ? costoClick : 0.0
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.setBearerAuth(bearerToken);
                HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);

                var resp = restTemplate.postForEntity(destUrl, req, String.class);
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
     * Obtiene (o reutiliza) un token JWT HS256 con payload fijo {"registrador":"ristorino"}.
     * Se agrega iat y exp para control de vigencia.
     * @return token JWT listo para usar en Authorization Bearer
     */
    private synchronized String getJwtToken() {
        long now = Instant.now().getEpochSecond();
        if (cachedToken != null && now < cachedTokenExpEpoch) {
            return cachedToken;
        }
        long exp = now + tokenTtlSeconds;
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = String.format("{\"registrador\":\"%s\",\"iat\":%d,\"exp\":%d}", "ristorino", now, exp);
        String headerB64 = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String toSign = headerB64 + "." + payloadB64;
        String signatureB64 = hmacSha256Base64Url(toSign, jwtSecret);
        cachedToken = toSign + "." + signatureB64;
        cachedTokenExpEpoch = exp - 5; // margen de seguridad antes de expirar
        return cachedToken;
    }

    /**
     * Genera firma HMAC SHA256 y la codifica en base64url.
     */
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

    /**
     * Codifica bytes en Base64 y adapta a representación URL-safe sin padding.
     */
    private String base64Url(byte[] bytes) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return b64.replace('+', '-').replace('/', '_').replaceAll("=+$", "");
    }

    /** Conversión segura a Integer desde objetos genéricos. */
    private Integer asInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        try { return o != null ? Integer.parseInt(o.toString()) : null; } catch (Exception e) { return null; }
    }
    /** Conversión segura a Double desde objetos genéricos. */
    private Double asDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return o != null ? Double.parseDouble(o.toString()) : null; } catch (Exception e) { return null; }
    }
}
