package ar.edu.ubp.das.ristorinoapi.resources;

import ar.edu.ubp.das.ristorinoapi.services.ClickNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recurso REST manual para disparar la notificación de clicks pendientes.
 * <p>Este endpoint no registra nuevos clicks, sólo procesa aquellos que ya existen y están marcados como no notificados.</p>
 * <p>La autenticación contra la API externa del restaurante (JWT Bearer) se maneja dentro de {@link ClickNotificationService}.
 * Es recomendable proteger este recurso con autenticación propia para evitar ejecuciones no autorizadas.</p>
 * <p>Uso típico:
 * <pre>POST /api/manual/notify-clicks              --> Notifica todos los pendientes
 * POST /api/manual/notify-clicks?nroRestaurante=5 --> Sólo pendientes del restaurante 5</pre></p>
 * Respuesta JSON:
 * <pre>{
 *   "notificadosExitosos": 3,
 *   "nroRestauranteFilter": 5,
 *   "timestamp": "2025-11-13T18:20:01Z"
 * }</pre>
 */
@RestController
@RequestMapping("/api/manual")
public class ManualNotificationResource {

    private static final Logger log = LoggerFactory.getLogger(ManualNotificationResource.class);

    @Autowired
    private ClickNotificationService clickNotificationService;

    /**
     * Dispara la notificación de todos los clicks pendientes (opcionalmente filtrando por restaurante).
     * Cada click exitoso se marca como notificado en la base de datos.
     * @param nroRestaurante filtro opcional por restaurante (null = todos)
     * @return ResponseEntity con cuerpo JSON detallando resultado del proceso
     */
    @PostMapping(value = "/notify-clicks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> notifyClicks(@RequestParam(required = false) Integer nroRestaurante) {
        try {
            int count = clickNotificationService.notifyAllPendingClicks(nroRestaurante);
            Map<String, Object> body = new LinkedHashMap<>(); // Map.of no admite valores potencialmente null
            body.put("notificadosExitosos", count);
            if (nroRestaurante != null) {
                body.put("nroRestauranteFilter", nroRestaurante);
            }
            body.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("Error en notificación manual", e);
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            err.put("timestamp", Instant.now().toString());
            return ResponseEntity.internalServerError().body(err);
        }
    }
}
