package ar.edu.ubp.das.ristorinoapi.resources;

import ar.edu.ubp.das.ristorinoapi.repositories.RestaurantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para exponer el detalle completo de un restaurante.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantResource {

    @Autowired
    private RestaurantRepository restaurantRepository;

    /**
     * Devuelve el detalle anidado del restaurante como JSON.
     * Ej: GET /api/restaurants/1?nroIdioma=1
     */
    @GetMapping(value = "/{nroRestaurante}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRestaurantDetails(
            @PathVariable Integer nroRestaurante,
            @RequestParam(required = false) Integer nroIdioma
    ) {
        try {
            JsonNode node = restaurantRepository.getRestaurantDetails(nroRestaurante, nroIdioma);
            if (node == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(node);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor: " + e.getMessage());
        }
    }
}

