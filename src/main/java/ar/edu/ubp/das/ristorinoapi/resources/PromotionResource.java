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
     * Registra un click anónimo sobre un contenido específico indicando explícitamente restaurante e idioma.
     * Útil cuando el frontend conoce las tres claves.
     * Si alguno de los path variables llega null (binding fallido) retorna 400.
     */
    @PostMapping("/{nroRestaurante}/{nroIdioma}/{nroContenido}/click")
    public ResponseEntity<?> registerClickByContenidoAlt(
            @PathVariable Integer nroRestaurante,
            @PathVariable Integer nroIdioma,
            @PathVariable Integer nroContenido) {
        try {
            if (nroRestaurante == null || nroIdioma == null || nroContenido == null) {
                return ResponseEntity.badRequest().body("Debe indicar nroRestaurante, nroIdioma y nroContenido en la URL");
            }
            var result = clickRepository.registerAnonymousClick(nroRestaurante, nroIdioma, nroContenido, null);
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