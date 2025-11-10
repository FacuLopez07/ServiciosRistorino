package ar.edu.ubp.das.ristorinoapi.resources;

import ar.edu.ubp.das.ristorinoapi.services.ClickNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Recurso manual para disparar la notificación de clicks pendientes.
 * Se podría proteger con alguna forma de auth básica o clave, por ahora expuesto simple.
 */
@RestController
@RequestMapping("/api/manual")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = { RequestMethod.POST })
public class ManualNotificationResource {

    private static final Logger log = LoggerFactory.getLogger(ManualNotificationResource.class);

    @Autowired
    private ClickNotificationService clickNotificationService;

    /**
     * Dispara la notificación de todos los clicks pendientes.
     * @param nroRestaurante (opcional) filtra por restaurante específico.
     */
    @PostMapping("/notify-clicks")
    public ResponseEntity<?> notifyClicks(@RequestParam(required = false) Integer nroRestaurante) {
        try {
            int count = clickNotificationService.notifyAllPendingClicks(nroRestaurante);
            return ResponseEntity.ok("Clicks notificados exitosamente: " + count);
        } catch (Exception e) {
            log.error("Error en notificación manual", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}

