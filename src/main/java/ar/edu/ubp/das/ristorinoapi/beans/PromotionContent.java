package ar.edu.ubp.das.ristorinoapi.beans;

public class PromotionContent {
    private Integer nro_contenido;
    private Integer nro_sucursal;
    private String nom_sucursal;
    private Integer nro_idioma;
    private String cod_idioma;
    private String nom_idioma;
    private String contenido_promocional;
    private String contenido_a_publicar;
    private String fecha_ini_vigencia;
    private String fecha_fin_vigencia; // Nuevo campo
    private String imagen_promocional; // Nuevo campo
    private Double costo_click;
    private String cod_contenido_restaurante;
    private Boolean vigente;

    // Constructor vacío
    public PromotionContent() {}

    // Getters y Setters
    public Integer getNro_contenido() { return nro_contenido; }
    public void setNro_contenido(Integer nro_contenido) { this.nro_contenido = nro_contenido; }

    public Integer getNro_sucursal() { return nro_sucursal; }
    public void setNro_sucursal(Integer nro_sucursal) { this.nro_sucursal = nro_sucursal; }

    public String getNom_sucursal() { return nom_sucursal; }
    public void setNom_sucursal(String nom_sucursal) { this.nom_sucursal = nom_sucursal; }

    public Integer getNro_idioma() { return nro_idioma; }
    public void setNro_idioma(Integer nro_idioma) { this.nro_idioma = nro_idioma; }

    public String getCod_idioma() { return cod_idioma; }
    public void setCod_idioma(String cod_idioma) { this.cod_idioma = cod_idioma; }

    public String getNom_idioma() { return nom_idioma; }
    public void setNom_idioma(String nom_idioma) { this.nom_idioma = nom_idioma; }

    public String getContenido_promocional() { return contenido_promocional; }
    public void setContenido_promocional(String contenido_promocional) { this.contenido_promocional = contenido_promocional; }

    public String getContenido_a_publicar() { return contenido_a_publicar; }
    public void setContenido_a_publicar(String contenido_a_publicar) { this.contenido_a_publicar = contenido_a_publicar; }

    public String getFecha_ini_vigencia() { return fecha_ini_vigencia; }
    public void setFecha_ini_vigencia(String fecha_ini_vigencia) { this.fecha_ini_vigencia = fecha_ini_vigencia; }

    public String getFecha_fin_vigencia() { return fecha_fin_vigencia; }
    public void setFecha_fin_vigencia(String fecha_fin_vigencia) { this.fecha_fin_vigencia = fecha_fin_vigencia; }

    public String getImagen_promocional() { return imagen_promocional; }
    public void setImagen_promocional(String imagen_promocional) { this.imagen_promocional = imagen_promocional; }

    public Double getCosto_click() { return costo_click; }
    public void setCosto_click(Double costo_click) { this.costo_click = costo_click; }

    public String getCod_contenido_restaurante() { return cod_contenido_restaurante; }
    public void setCod_contenido_restaurante(String cod_contenido_restaurante) { this.cod_contenido_restaurante = cod_contenido_restaurante; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }
}