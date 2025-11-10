package ar.edu.ubp.das.ristorinoapi.resources;

// CAMBIO: Agregar import de RestaurantResponse
import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
// import ar.edu.ubp.das.ristorinoapi.beans.PromotionContent; // Import necesario para lista directa
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
// Agregados para registrar clicks
import ar.edu.ubp.das.ristorinoapi.beans.ClickRequest;
import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS })
public class PromotionResource {

    private static final Logger log = LoggerFactory.getLogger(PromotionResource.class);

    @Autowired
    private PromotionRepository promotionRepository;
    // Inyección repositorio de clicks
    @Autowired
    private ClickRepository clickRepository;

    @GetMapping
    public ResponseEntity<?> getPromotions() {
        try {
            System.out.println("=== SOLICITUD RECIBIDA PARA OBTENER PROMOCIONES (solo lista) ===");

            RestaurantResponse restaurantData = promotionRepository.getPromotionsWithRestaurant();
            if (restaurantData == null) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            System.out.println("Restaurante: " + restaurantData.getRazon_social());
            if (restaurantData.getContenidos() == null) {
                System.out.println("Promociones enviadas al cliente: 0 (lista vacía por contenidos nulo)");
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
            System.out.println("Promociones enviadas al cliente: " + restaurantData.getContenidos().size());
            // DEVOLVER directamente la lista de contenidos
            return ResponseEntity.ok(restaurantData.getContenidos());

        } catch (Exception e) {
            System.err.println("=== ERROR EN CONTROLLER ===");
            System.err.println("Error: " + e.getMessage());

            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    // Nuevo endpoint con parámetros
    @GetMapping("/{nroRestaurante}")
    public ResponseEntity<?> getPromotionsForRestaurant(
            @PathVariable Integer nroRestaurante,
            @RequestParam(required = false) Boolean soloVigentes,
            @RequestParam(required = false) Integer nroSucursal
    ) {
        try {
            RestaurantResponse restaurantData = promotionRepository.getPromotionsWithRestaurant(nroRestaurante, soloVigentes, nroSucursal);
            return ResponseEntity.ok(restaurantData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    // Endpoint para registrar click: acepta body completo o solo nroContenido
    @PostMapping("/clicks")
    public ResponseEntity<?> registerClick(@RequestBody ClickRequest request) {
        try {
            if (request.getNroContenido() == null) {
                return ResponseEntity.badRequest().body("Debe enviar al menos nroContenido");
            }
            var result = (request.getNroRestaurante() == null || request.getNroIdioma() == null)
                    ? clickRepository.registerAnonymousClickByContenido(request.getNroContenido(), request.getFechaRegistro())
                    : clickRepository.registerAnonymousClick(
                        request.getNroRestaurante(),
                        request.getNroIdioma(),
                        request.getNroContenido(),
                        request.getFechaRegistro()
                    );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException iae) {
            log.warn("Registro de click rechazado: {}", iae.getMessage());
            return ResponseEntity.status(404).body(iae.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar click", e);
            return ResponseEntity.internalServerError().body("Error al registrar click: " + e.getMessage());
        }
    }

    // Endpoint alternativo que recibe solo el id del contenido por path (útil para frontend que llama con .id)
    @PostMapping("/clicks/{nroContenido}")
    public ResponseEntity<?> registerClickByContenido(@PathVariable Integer nroContenido) {
        try {
            var result = clickRepository.registerAnonymousClickByContenido(nroContenido, null);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException iae) {
            log.warn("Registro de click rechazado: {}", iae.getMessage());
            return ResponseEntity.status(404).body(iae.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar click", e);
            return ResponseEntity.internalServerError().body("Error al registrar click: " + e.getMessage());
        }
    }

    // NUEVO: endpoint compatible con la URL /api/promotions/{nroContenido}/click usada por el frontend
    @PostMapping("/{nroContenido}/click")
    public ResponseEntity<?> registerClickByContenidoAlt(@PathVariable Integer nroContenido) {
        try {
            var result = clickRepository.registerAnonymousClickByContenido(nroContenido, null);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException iae) {
            log.warn("Registro de click rechazado: {}", iae.getMessage());
            return ResponseEntity.status(404).body(iae.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar click", e);
            return ResponseEntity.internalServerError().body("Error al registrar click: " + e.getMessage());
        }
    }
}