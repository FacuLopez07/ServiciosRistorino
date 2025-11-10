package ar.edu.ubp.das.ristorinoapi.beans;

import java.util.List;

/**
 * DTO que representa la respuesta compuesta del restaurante y sus contenidos promocionales.
 * Se corresponde con la estructura JSON devuelta por el SP dbo.usp_get_promociones_restaurante.
 */
public class RestaurantResponse {
    private Integer nro_restaurante;
    private String razon_social;
    private List<PromotionContent> contenidos;

    /** Constructor vacío requerido para deserialización */
    public RestaurantResponse() {}

    /** @return identificador del restaurante */
    public Integer getNro_restaurante() { return nro_restaurante; }
    public void setNro_restaurante(Integer nro_restaurante) { this.nro_restaurante = nro_restaurante; }

    /** @return razón social (nombre legal/comercial) */
    public String getRazon_social() { return razon_social; }
    public void setRazon_social(String razon_social) { this.razon_social = razon_social; }

    /** @return lista de contenidos promocionales (puede ser null) */
    public List<PromotionContent> getContenidos() { return contenidos; }
    public void setContenidos(List<PromotionContent> contenidos) { this.contenidos = contenidos; }
}