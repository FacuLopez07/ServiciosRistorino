package ar.edu.ubp.das.ristorinoapi.resources;

// CAMBIO: Agregar import de RestaurantResponse
import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.beans.PromotionContent; // Import necesario para lista directa
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:4200")
public class PromotionResource {

    @Autowired
    private PromotionRepository promotionRepository;

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
}