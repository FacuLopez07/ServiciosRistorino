package ar.edu.ubp.das.ristorinoapi.beans;

/**
 * DTO de cada contenido/promoción del restaurante.
 * Representa la estructura generada por el SP y permite ser serializado hacia el frontend.
 */
public class PromotionContent {
    private Integer nro_contenido;            // Identificador interno del contenido
    private Integer nro_sucursal;             // Sucursal a la que aplica o null si es global
    private String nom_sucursal;              // Nombre de la sucursal (LEFT JOIN)
    private Integer nro_idioma;               // Identificador del idioma
    private String cod_idioma;                // Código corto (ej. "es", "en")
    private String nom_idioma;                // Nombre descriptivo del idioma
    private String contenido_promocional;     // Texto o payload promocional original
    private String contenido_a_publicar;      // Versión final a publicar (puede diferir del original)
    private String fecha_ini_vigencia;        // Fecha inicio vigencia (YYYY-MM-DD) o null
    private String fecha_fin_vigencia;        // Fecha fin vigencia (YYYY-MM-DD) o null
    private String imagen_promocional;        // URL/base64 de la imagen asociada
    private Double costo_click;               // Costo por click (metadata)
    private String cod_contenido_restaurante; // Código externo o público del contenido
    private Boolean vigente;                  // Flag calculado en el SP según fechas

    /** Constructor vacío para frameworks de deserialización */
    public PromotionContent() {}

    /**
     * Obtiene el número de contenido.
     * @return El número de contenido.
     */
    public Integer getNro_contenido() { return nro_contenido; }
    /**
     * Establece el número de contenido.
     * @param nro_contenido El número de contenido a establecer.
     */
    public void setNro_contenido(Integer nro_contenido) { this.nro_contenido = nro_contenido; }

    /**
     * Obtiene el número de sucursal.
     * @return El número de sucursal.
     */
    public Integer getNro_sucursal() { return nro_sucursal; }
    /**
     * Establece el número de sucursal.
     * @param nro_sucursal El número de sucursal a establecer.
     */
    public void setNro_sucursal(Integer nro_sucursal) { this.nro_sucursal = nro_sucursal; }

    /**
     * Obtiene el nombre de la sucursal.
     * @return El nombre de la sucursal.
     */
    public String getNom_sucursal() { return nom_sucursal; }
    /**
     * Establece el nombre de la sucursal.
     * @param nom_sucursal El nombre de la sucursal a establecer.
     */
    public void setNom_sucursal(String nom_sucursal) { this.nom_sucursal = nom_sucursal; }

    /**
     * Obtiene el número de idioma.
     * @return El número de idioma.
     */
    public Integer getNro_idioma() { return nro_idioma; }
    /**
     * Establece el número de idioma.
     * @param nro_idioma El número de idioma a establecer.
     */
    public void setNro_idioma(Integer nro_idioma) { this.nro_idioma = nro_idioma; }

    /**
     * Obtiene el código de idioma.
     * @return El código de idioma.
     */
    public String getCod_idioma() { return cod_idioma; }
    /**
     * Establece el código de idioma.
     * @param cod_idioma El código de idioma a establecer.
     */
    public void setCod_idioma(String cod_idioma) { this.cod_idioma = cod_idioma; }

    /**
     * Obtiene el nombre de idioma.
     * @return El nombre de idioma.
     */
    public String getNom_idioma() { return nom_idioma; }
    /**
     * Establece el nombre de idioma.
     * @param nom_idioma El nombre de idioma a establecer.
     */
    public void setNom_idioma(String nom_idioma) { this.nom_idioma = nom_idioma; }

    /**
     * Obtiene el contenido promocional.
     * @return El contenido promocional.
     */
    public String getContenido_promocional() { return contenido_promocional; }
    /**
     * Establece el contenido promocional.
     * @param contenido_promocional El contenido promocional a establecer.
     */
    public void setContenido_promocional(String contenido_promocional) { this.contenido_promocional = contenido_promocional; }

    /**
     * Obtiene el contenido a publicar.
     * @return El contenido a publicar.
     */
    public String getContenido_a_publicar() { return contenido_a_publicar; }
    /**
     * Establece el contenido a publicar.
     * @param contenido_a_publicar El contenido a publicar a establecer.
     */
    public void setContenido_a_publicar(String contenido_a_publicar) { this.contenido_a_publicar = contenido_a_publicar; }

    /**
     * Obtiene la fecha de inicio de vigencia.
     * @return La fecha de inicio de vigencia.
     */
    public String getFecha_ini_vigencia() { return fecha_ini_vigencia; }
    /**
     * Establece la fecha de inicio de vigencia.
     * @param fecha_ini_vigencia La fecha de inicio de vigencia a establecer.
     */
    public void setFecha_ini_vigencia(String fecha_ini_vigencia) { this.fecha_ini_vigencia = fecha_ini_vigencia; }

    /**
     * Obtiene la fecha de fin de vigencia.
     * @return La fecha de fin de vigencia.
     */
    public String getFecha_fin_vigencia() { return fecha_fin_vigencia; }
    /**
     * Establece la fecha de fin de vigencia.
     * @param fecha_fin_vigencia La fecha de fin de vigencia a establecer.
     */
    public void setFecha_fin_vigencia(String fecha_fin_vigencia) { this.fecha_fin_vigencia = fecha_fin_vigencia; }

    /**
     * Obtiene la imagen promocional.
     * @return La imagen promocional.
     */
    public String getImagen_promocional() { return imagen_promocional; }
    /**
     * Establece la imagen promocional.
     * @param imagen_promocional La imagen promocional a establecer.
     */
    public void setImagen_promocional(String imagen_promocional) { this.imagen_promocional = imagen_promocional; }

    /**
     * Obtiene el costo por click.
     * @return El costo por click.
     */
    public Double getCosto_click() { return costo_click; }
    /**
     * Establece el costo por click.
     * @param costo_click El costo por click a establecer.
     */
    public void setCosto_click(Double costo_click) { this.costo_click = costo_click; }

    /**
     * Obtiene el código de contenido del restaurante.
     * @return El código de contenido del restaurante.
     */
    public String getCod_contenido_restaurante() { return cod_contenido_restaurante; }
    /**
     * Establece el código de contenido del restaurante.
     * @param cod_contenido_restaurante El código de contenido del restaurante a establecer.
     */
    public void setCod_contenido_restaurante(String cod_contenido_restaurante) { this.cod_contenido_restaurante = cod_contenido_restaurante; }

    /**
     * Obtiene el estado de vigencia.
     * @return Verdadero si está vigente, falso en caso contrario.
     */
    public Boolean getVigente() { return vigente; }
    /**
     * Establece el estado de vigencia.
     * @param vigente Verdadero si debe estar vigente, falso en caso contrario.
     */
    public void setVigente(Boolean vigente) { this.vigente = vigente; }
}