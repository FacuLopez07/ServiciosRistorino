package ar.edu.ubp.das.ristorinoapi.repositories;

import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.beans.PromotionContent;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PromotionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Gson gson = new Gson();

    public RestaurantResponse getPromotionsWithRestaurant() {
        try {
            String sql = "EXEC dbo.usp_get_promociones_restaurante @nro_restaurante = 1";

            System.out.println("=== EJECUTANDO PROCEDIMIENTO ALMACENADO ===");

            // CAMBIO: Usar query() en lugar de queryForObject()
            List<String> jsonResults = jdbcTemplate.query(sql, (rs, rowNum) -> {
                return rs.getString(1); // Obtener la primera columna como string
            });

            System.out.println("Número de filas obtenidas: " + jsonResults.size());

            if (jsonResults.isEmpty()) {
                throw new RuntimeException("No se obtuvieron resultados del procedimiento");
            }

            // Tomar la primera fila (o decidir qué hacer con múltiples filas)
            String jsonResult = jsonResults.get(0);
            System.out.println("JSON obtenido (primera fila): " + jsonResult);

            // Si hay múltiples filas, mostrar warning
            if (jsonResults.size() > 1) {
                System.out.println("⚠️  ADVERTENCIA: El procedimiento devolvió " + jsonResults.size() + " filas. Usando la primera.");
                for (int i = 0; i < jsonResults.size(); i++) {
                    System.out.println("Fila " + i + ": " + jsonResults.get(i));
                }
            }

            // Convertir JSON a objeto RestaurantResponse
            RestaurantResponse restaurantResponse = gson.fromJson(jsonResult, RestaurantResponse.class);

            System.out.println("Restaurante: " + restaurantResponse.getRazon_social());
            System.out.println("Número de contenidos: " + restaurantResponse.getContenidos().size());

            return restaurantResponse;

        } catch (Exception e) {
            System.err.println("=== ERROR EN REPOSITORY ===");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener promociones desde la base de datos", e);
        }
    }
}