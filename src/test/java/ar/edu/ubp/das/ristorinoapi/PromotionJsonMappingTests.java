package ar.edu.ubp.das.ristorinoapi;

import ar.edu.ubp.das.ristorinoapi.beans.RestaurantResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PromotionJsonMappingTests {

    private final Gson gson = new Gson();

    @Test
    void parseWithEmbeddedContenidosString_shouldNormalizeAndMap() {
        String contenidosStr = "[{\"nro_contenido\":1,\"nro_sucursal\":null,\"nom_sucursal\":null,\"nro_idioma\":1,\"cod_idioma\":\"es\",\"nom_idioma\":\"Español\",\"contenido_promocional\":\"Promo\",\"imagen_promocional\":\"/img.png\",\"contenido_a_publicar\":\"Texto\",\"fecha_ini_vigencia\":\"2025-01-01\",\"fecha_fin_vigencia\":null,\"costo_click\":1.5,\"cod_contenido_restaurante\":\"ABCD\",\"vigente\":true}]";
        String json = "{" +
                "\"nro_restaurante\":1,\"razon_social\":\"Test\",\"contenidos\":" +
                "\"" + contenidosStr.replace("\"", "\\\"") + "\"" +
                "}";

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonElement contenidos = root.get("contenidos");
        Assertions.assertTrue(contenidos.isJsonPrimitive());
        JsonElement parsed = JsonParser.parseString(contenidos.getAsString());
        root.add("contenidos", parsed);

        RestaurantResponse resp = gson.fromJson(root, RestaurantResponse.class);
        Assertions.assertNotNull(resp);
        Assertions.assertEquals(1, resp.getNro_restaurante());
        Assertions.assertEquals("Test", resp.getRazon_social());
        Assertions.assertNotNull(resp.getContenidos());
        Assertions.assertEquals(1, resp.getContenidos().size());
        Assertions.assertEquals(1, resp.getContenidos().get(0).getNro_contenido());
    }

    @Test
    void parseWithContenidosArray_shouldMap() {
        String json = "{" +
                "\"nro_restaurante\":1,\"razon_social\":\"Test\",\"contenidos\":[{" +
                "\"nro_contenido\":2,\"nro_sucursal\":1,\"nom_sucursal\":\"Centro\",\"nro_idioma\":1,\"cod_idioma\":\"es\",\"nom_idioma\":\"Español\",\"contenido_promocional\":\"Promo2\",\"imagen_promocional\":\"/img2.png\",\"contenido_a_publicar\":\"Texto2\",\"fecha_ini_vigencia\":\"2025-02-01\",\"fecha_fin_vigencia\":\"2025-12-31\",\"costo_click\":2.5,\"cod_contenido_restaurante\":\"EFGH\",\"vigente\":false}]" +
                "}";

        RestaurantResponse resp = gson.fromJson(json, RestaurantResponse.class);
        Assertions.assertNotNull(resp);
        Assertions.assertEquals(1, resp.getNro_restaurante());
        Assertions.assertEquals("Test", resp.getRazon_social());
        Assertions.assertNotNull(resp.getContenidos());
        Assertions.assertEquals(1, resp.getContenidos().size());
        Assertions.assertEquals(2, resp.getContenidos().get(0).getNro_contenido());
        Assertions.assertEquals("Centro", resp.getContenidos().get(0).getNom_sucursal());
    }
}

