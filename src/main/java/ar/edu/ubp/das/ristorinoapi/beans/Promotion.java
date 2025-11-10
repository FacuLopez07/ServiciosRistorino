package ar.edu.ubp.das.ristorinoapi.beans;

/**
 * DTO histórico de promoción simple.
 * Actualmente no es el modelo principal expuesto por /api/promotions (se usa {@link PromotionContent}).
 * Se mantiene por compatibilidad/ejemplos.
 */
public class Promotion {
    private Long id;
    private String titulo;
    private String subtitulo;
    private String descripcion;
    private String imagen_url;
    private String ruta;
    private Boolean activa; // Se agrego para probar la nueva conexion a la base

    /** Constructor vacío para frameworks de deserialización */
    public Promotion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return titulo; }
    public void setTitle(String titulo) { this.titulo = titulo; }

    public String getSubtitle() { return subtitulo; }
    public void setSubtitle(String subtitulo) { this.subtitulo = subtitulo; }

    public String getDescription() { return descripcion; }
    public void setDescription(String descripcion) { this.descripcion = descripcion; }

    public String getImageUrl() { return imagen_url; }
    public void setImageUrl(String imagen_url) { this.imagen_url = imagen_url; }

    public String getRoute() { return ruta; }
    public void setRoute(String ruta) { this.ruta = ruta; }

    public Boolean getActive() { return activa; }
    public void setActive(Boolean activa) { this.activa = activa; }
}
