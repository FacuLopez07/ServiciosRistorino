package ar.edu.ubp.das.ristorinoapi.resources;

import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para exponer promociones y registrar clicks.
 *
 * Endpoints expuestos:
 * - GET /api/promotions: devuelve directamente la lista de contenidos promocionales (PromotionContent[])
 *   para el restaurante por defecto.
 * - GET /api/promotions/{nroRestaurante}?soloVigentes&nroSucursal: versión parametrizada que devuelve el objeto
 *   completo del restaurante con su lista de contenidos.
 * - POST /api/promotions/{nroContenido}/click: registra un click anónimo resolviendo restaurante/idioma por nroContenido.
 */
@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS })
public class PromotionResource {

    private static final Logger log = LoggerFactory.getLogger(PromotionResource.class);

    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ClickRepository clickRepository;

    /**
     * Devuelve únicamente la lista de contenidos (promociones) para simplificar el consumo en Angular.
     * Si no hay datos o contenidos es null, retorna una lista vacía (HTTP 200).
     * @return ResponseEntity con arreglo de contenidos o lista vacía.
     */
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
            // Devolvemos directamente la lista para que el frontend tipado a Promotion[] consuma sin wrapper.
            return ResponseEntity.ok(restaurantData.getContenidos());

        } catch (Exception e) {
            System.err.println("=== ERROR EN CONTROLLER ===");
            System.err.println("Error: " + e.getMessage());

            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Versión parametrizada: permite filtrar por vigencia actual y por sucursal.
     * Los parámetros son opcionales; si no se indican, el SP devuelve todo.
     */
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

    /**
     * Registra un click anónimo sobre un contenido específico.
     * Resuelve restaurante e idioma en base al nroContenido.
     * @param nroContenido id del contenido
     */
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