package ar.edu.ubp.das.ristorinoapi.resources;

import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import ar.edu.ubp.das.ristorinoapi.utils.B64uDecoder;
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
public class PromotionResource {

    private static final Logger log = LoggerFactory.getLogger(PromotionResource.class);

    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ClickRepository clickRepository;


    /**
     * Permite filtrar por vigencia actual y por sucursal.
     * Los parámetros son opcionales; si no se indican, el SP devuelve todo.
     */
    @GetMapping("/{nroRestaurante}")
    public ResponseEntity<?> getPromotionsForRestaurant(
            @PathVariable String nroRestaurante,
            @RequestParam(required = false) Boolean soloVigentes,
            @RequestParam(required = false) Integer nroSucursal
    ) {
        try {
            Integer restauranteId = B64uDecoder.decodeToInt(nroRestaurante);
            RestaurantResponse restaurantData = promotionRepository.getPromotionsWithRestaurant(restauranteId, soloVigentes, nroSucursal);
            return ResponseEntity.ok(restaurantData);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Registra un click anónimo sobre un contenido específico indicando explícitamente restaurante e idioma.
     * Si alguno de los path variables llega null (binding fallido) retorna 400.
     */
    @PostMapping("/{nroRestaurante}/{nroIdioma}/{nroContenido}/click")
    public ResponseEntity<?> registerClickByContenidoAlt(
            @PathVariable String nroRestaurante,
            @PathVariable String nroIdioma,
            @PathVariable String nroContenido) {
        try {
            Integer rest = B64uDecoder.decodeToInt(nroRestaurante);
            Integer idioma = B64uDecoder.decodeToInt(nroIdioma);
            Integer contenido = B64uDecoder.decodeToInt(nroContenido);
            var result = clickRepository.registerAnonymousClick(rest, idioma, contenido, null);
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