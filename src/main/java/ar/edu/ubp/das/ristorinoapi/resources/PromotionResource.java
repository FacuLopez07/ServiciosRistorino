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
 * Controlador REST para exponer promociones y registrar clicks sobre contenidos.
 * Endpoints principales:
 * <ul>
 *   <li>GET /api/promotions/{nroRestaurante}?soloVigentes&nroSucursal<br>
 *       Devuelve estructura {@link RestaurantResponse} con lista de contenidos filtrados opcionalmente.</li>
 *   <li>POST /api/promotions/{nroRestaurante}/{nroIdioma}/{nroContenido}/click<br>
 *       Registra un click anónimo sobre un contenido específico.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionResource {

    private static final Logger log = LoggerFactory.getLogger(PromotionResource.class);

    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ClickRepository clickRepository;

    /**
     * Obtiene las promociones de un restaurante en formato compuesto (incluye metadatos del restaurante).
     * @param nroRestaurante id del restaurante
     * @param soloVigentes filtro opcional de vigencia (null = no filtra)
     * @param nroSucursal sucursal específica (null = todas/globales)
     * @return {@link RestaurantResponse} serializado a JSON
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
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Registra un click anónimo sobre un contenido específico.
     * @param nroRestaurante id restaurante
     * @param nroIdioma id idioma
     * @param nroContenido id contenido
     * @return resultado del procedimiento almacenado (JSON deserializado a Map)
     */
    @PostMapping("/{nroRestaurante}/{nroIdioma}/{nroContenido}/click")
    public ResponseEntity<?> registerClickByContenidoAlt(
            @PathVariable Integer nroRestaurante,
            @PathVariable Integer nroIdioma,
            @PathVariable Integer nroContenido) {
        try {
            var result = clickRepository.registerAnonymousClick(nroRestaurante, nroIdioma, nroContenido, null);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException iae) {
            log.warn("Registro de click rechazado: {}", iae.getMessage());
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            log.error("Error al registrar click", e);
            return ResponseEntity.internalServerError().body("Error al registrar click: " + e.getMessage());
        }
    }

}