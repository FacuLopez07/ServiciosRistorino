package ar.edu.ubp.das.ristorinoapi.beans;

import java.util.List;

public class RestaurantResponse {
    private Integer nro_restaurante;
    private String razon_social;
    private List<PromotionContent> contenidos;

    // Constructor vacío
    public RestaurantResponse() {}

    // Getters y Setters
    public Integer getNro_restaurante() { return nro_restaurante; }
    public void setNro_restaurante(Integer nro_restaurante) { this.nro_restaurante = nro_restaurante; }

    public String getRazon_social() { return razon_social; }
    public void setRazon_social(String razon_social) { this.razon_social = razon_social; }

    public List<PromotionContent> getContenidos() { return contenidos; }
    public void setContenidos(List<PromotionContent> contenidos) { this.contenidos = contenidos; }
}