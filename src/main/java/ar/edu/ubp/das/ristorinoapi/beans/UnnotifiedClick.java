package ar.edu.ubp.das.ristorinoapi.beans;

import java.time.LocalDateTime;

/**
 * DTO plano que combina los datos del click y parte del contenido para notificación externa.
 */
public class UnnotifiedClick {
    private Integer nroRestaurante;
    private Integer nroIdioma;
    private Integer nroContenido;
    private Integer nroClick;
    private LocalDateTime fechaHoraRegistro;
    private Integer nroCliente;
    private Double costoClick;
    private Integer notificado; // 0/1
    private String codContenidoRestaurante;
    private String contenidoPromocional;
    private String imagenPromocional;
    private String contenidoAPublicar;

    public Integer getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(Integer nroRestaurante) { this.nroRestaurante = nroRestaurante; }
    public Integer getNroIdioma() { return nroIdioma; }
    public void setNroIdioma(Integer nroIdioma) { this.nroIdioma = nroIdioma; }
    public Integer getNroContenido() { return nroContenido; }
    public void setNroContenido(Integer nroContenido) { this.nroContenido = nroContenido; }
    public Integer getNroClick() { return nroClick; }
    public void setNroClick(Integer nroClick) { this.nroClick = nroClick; }
    public LocalDateTime getFechaHoraRegistro() { return fechaHoraRegistro; }
    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) { this.fechaHoraRegistro = fechaHoraRegistro; }
    public Integer getNroCliente() { return nroCliente; }
    public void setNroCliente(Integer nroCliente) { this.nroCliente = nroCliente; }
    public Double getCostoClick() { return costoClick; }
    public void setCostoClick(Double costoClick) { this.costoClick = costoClick; }
    public Integer getNotificado() { return notificado; }
    public void setNotificado(Integer notificado) { this.notificado = notificado; }
    public String getCodContenidoRestaurante() { return codContenidoRestaurante; }
    public void setCodContenidoRestaurante(String codContenidoRestaurante) { this.codContenidoRestaurante = codContenidoRestaurante; }
    public String getContenidoPromocional() { return contenidoPromocional; }
    public void setContenidoPromocional(String contenidoPromocional) { this.contenidoPromocional = contenidoPromocional; }
    public String getImagenPromocional() { return imagenPromocional; }
    public void setImagenPromocional(String imagenPromocional) { this.imagenPromocional = imagenPromocional; }
    public String getContenidoAPublicar() { return contenidoAPublicar; }
    public void setContenidoAPublicar(String contenidoAPublicar) { this.contenidoAPublicar = contenidoAPublicar; }
}

