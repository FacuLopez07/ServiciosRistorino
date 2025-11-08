package ar.edu.ubp.das.ristorinoapi.resources;

// CAMBIO: Agregar import de RestaurantResponse
import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.beans.PromotionContent;
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:4200")
public class PromotionResource {

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping
    public ResponseEntity<?> getPromotions() {
        try {
            System.out.println("=== SOLICITUD RECIBIDA PARA OBTENER PROMOCIONES ===");

            // CAMBIO: Obtener el objeto completo RestaurantResponse
            RestaurantResponse restaurantData = promotionRepository.getPromotionsWithRestaurant();

            System.out.println("Restaurante: " + restaurantData.getRazon_social());
            System.out.println("Promociones enviadas al cliente: " + restaurantData.getContenidos().size());

            // CAMBIO: Devolver el objeto completo en lugar de solo la lista
            return ResponseEntity.ok(restaurantData);

        } catch (Exception e) {
            System.err.println("=== ERROR EN CONTROLLER ===");
            System.err.println("Error: " + e.getMessage());

            return ResponseEntity.internalServerError()
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
}