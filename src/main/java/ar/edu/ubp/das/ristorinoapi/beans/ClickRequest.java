package ar.edu.ubp.das.ristorinoapi.beans;

import java.time.LocalDateTime;

/**
 * DTO para registrar un click sobre un contenido promocional.
 * Se permite omitir restaurante e idioma si se provee {@code nroContenido}, ya que
 * el backend intentará resolverlos con una consulta auxiliar.
 */
public class ClickRequest {
    private Integer nroRestaurante; // Opcional: si es null se intenta resolver por nroContenido
    private Integer nroIdioma;      // Opcional: si es null se intenta resolver por nroContenido
    private Integer nroContenido;   // Requerido para registrar el click
    private LocalDateTime fechaRegistro; // Opcional, null -> lo define el SP (GETDATE())

    /** @return número de restaurante asociado al contenido */
    public Integer getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(Integer nroRestaurante) { this.nroRestaurante = nroRestaurante; }

    /** @return número de idioma asociado al contenido */
    public Integer getNroIdioma() { return nroIdioma; }
    public void setNroIdioma(Integer nroIdioma) { this.nroIdioma = nroIdioma; }

    /** @return identificador del contenido sobre el que se registra el click */
    public Integer getNroContenido() { return nroContenido; }
    public void setNroContenido(Integer nroContenido) { this.nroContenido = nroContenido; }

    /** @return fecha/hora del click (opcional) */
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
