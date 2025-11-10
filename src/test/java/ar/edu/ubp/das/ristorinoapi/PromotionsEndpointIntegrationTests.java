package ar.edu.ubp.das.ristorinoapi;

import ar.edu.ubp.das.ristorinoapi.beans.PromotionContent;
import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import ar.edu.ubp.das.ristorinoapi.repositories.ClickRepository;
import ar.edu.ubp.das.ristorinoapi.repositories.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Pruebas de integración del endpoint GET /api/promotions.
 * Se mockea el repositorio para evitar dependencias de BD y controlar el payload.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PromotionsEndpointIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionRepository promotionRepository;

    // Necesario para inyección en el controlador aunque no se use en estos tests
    @MockBean
    private ClickRepository clickRepository;

    @Test
    @DisplayName("GET /api/promotions devuelve lista de contenidos con formato correcto")
    void getPromotions_shouldReturnListOfPromotionContents() throws Exception {
        // Arrange: construir RestaurantResponse con 2 contenidos
        PromotionContent c1 = new PromotionContent();
        c1.setNro_contenido(1);
        c1.setNro_sucursal(null);
        c1.setNom_sucursal(null);
        c1.setNro_idioma(1);
        c1.setCod_idioma("es");
        c1.setNom_idioma("Español");
        c1.setContenido_promocional("Promo 1");
        c1.setImagen_promocional("/img1.png");
        c1.setContenido_a_publicar("Texto 1");
        c1.setFecha_ini_vigencia("2025-01-01");
        c1.setFecha_fin_vigencia(null);
        c1.setCosto_click(1.5);
        c1.setCod_contenido_restaurante("ABCD");
        c1.setVigente(true);

        PromotionContent c2 = new PromotionContent();
        c2.setNro_contenido(2);
        c2.setNro_sucursal(3);
        c2.setNom_sucursal("Centro");
        c2.setNro_idioma(1);
        c2.setCod_idioma("es");
        c2.setNom_idioma("Español");
        c2.setContenido_promocional("Promo 2");
        c2.setImagen_promocional("/img2.png");
        c2.setContenido_a_publicar("Texto 2");
        c2.setFecha_ini_vigencia("2025-02-01");
        c2.setFecha_fin_vigencia("2025-12-31");
        c2.setCosto_click(2.5);
        c2.setCod_contenido_restaurante("EFGH");
        c2.setVigente(false);

        RestaurantResponse rr = new RestaurantResponse();
        rr.setNro_restaurante(1);
        rr.setRazon_social("Test");
        rr.setContenidos(List.of(c1, c2));

        when(promotionRepository.getPromotionsWithRestaurant()).thenReturn(rr);

        // Act + Assert
        mockMvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Respuesta es un array de tamaño 2
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(2)))
                // Validar primeros campos del primer elemento
                .andExpect(jsonPath("$[0].nro_contenido", is(1)))
                .andExpect(jsonPath("$[0].cod_idioma", is("es")))
                .andExpect(jsonPath("$[0].vigente", is(true)))
                // Validar algunos campos del segundo elemento
                .andExpect(jsonPath("$[1].nro_contenido", is(2)))
                .andExpect(jsonPath("$[1].nom_sucursal", is("Centro")))
                .andExpect(jsonPath("$[1].fecha_fin_vigencia", is("2025-12-31")));
    }

    @Test
    @DisplayName("GET /api/promotions devuelve lista vacía cuando contenidos es null")
    void getPromotions_shouldReturnEmptyListWhenNoData() throws Exception {
        RestaurantResponse rr = new RestaurantResponse();
        rr.setNro_restaurante(1);
        rr.setRazon_social("Test");
        rr.setContenidos(null); // Simular SP que devuelve contenidos nulo

        when(promotionRepository.getPromotionsWithRestaurant()).thenReturn(rr);

        mockMvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/promotions devuelve 500 si ocurre un error en el repositorio")
    void getPromotions_shouldReturn500OnRepositoryError() throws Exception {
        when(promotionRepository.getPromotionsWithRestaurant())
                .thenThrow(new RuntimeException("Falla al obtener promociones"));

        mockMvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error interno del servidor: Falla al obtener promociones")));
    }
}
