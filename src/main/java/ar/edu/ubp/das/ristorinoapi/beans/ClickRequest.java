package ar.edu.ubp.das.ristorinoapi.beans;

import java.time.LocalDateTime;

public class ClickRequest {
    private Integer nroRestaurante; // opcional: si es null se intenta resolver por nroContenido
    private Integer nroIdioma;      // opcional: si es null se intenta resolver por nroContenido
    private Integer nroContenido;   // requerido
    private LocalDateTime fechaRegistro; // opcional, null -> lo define el SP

    public Integer getNroRestaurante() { return nroRestaurante; }
    public void setNroRestaurante(Integer nroRestaurante) { this.nroRestaurante = nroRestaurante; }

    public Integer getNroIdioma() { return nroIdioma; }
    public void setNroIdioma(Integer nroIdioma) { this.nroIdioma = nroIdioma; }

    public Integer getNroContenido() { return nroContenido; }
    public void setNroContenido(Integer nroContenido) { this.nroContenido = nroContenido; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

