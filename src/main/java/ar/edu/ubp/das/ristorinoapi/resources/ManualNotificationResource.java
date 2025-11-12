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
 * Recurso manual para disparar la notificación de clicks pendientes.
 * <p>La autenticación JWT hacia la API del restaurante ahora se realiza dentro de
 * {@link ClickNotificationService}, por lo que este endpoint sólo coordina el proceso.
 * Se recomienda protegerlo (por ejemplo con un token interno o Spring Security) para evitar
 * ejecuciones no autorizadas.</p>
 * <p>Uso: POST /api/manual/notify-clicks?nroRestaurante=1 (parámetro opcional).
 * Devuelve un JSON con la cantidad de clicks notificados y marca como notificados en BD.</p>
 */
@RestController
@RequestMapping("/api/manual")
public class ManualNotificationResource {

    private static final Logger log = LoggerFactory.getLogger(ManualNotificationResource.class);

    @Autowired
    private ClickNotificationService clickNotificationService;

    /**
     * Dispara la notificación de todos los clicks pendientes (opcionalmente filtrando por restaurante).
     * @param nroRestaurante filtro opcional por restaurante.
     * @return JSON con total exitosos y metadata.
     */
    @PostMapping(value = "/notify-clicks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> notifyClicks(@RequestParam(required = false) Integer nroRestaurante) {
        try {
            int count = clickNotificationService.notifyAllPendingClicks(nroRestaurante);
            // Map.of no permite valores null -> usar mapa mutable
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("notificadosExitosos", count);
            if (nroRestaurante != null) {
                body.put("nroRestauranteFilter", nroRestaurante);
            }
            body.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("Error en notificación manual", e);
            Map<String, Object> err = new LinkedHashMap<>();
            if (e.getMessage() != null) {
                err.put("error", e.getMessage());
            } else {
                err.put("error", e.getClass().getSimpleName());
            }
            err.put("timestamp", Instant.now().toString());
            return ResponseEntity.internalServerError().body(err);
        }
    }
}
