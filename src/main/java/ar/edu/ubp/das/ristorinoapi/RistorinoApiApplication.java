package ar.edu.ubp.das.ristorinoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal de Ristorino API.
 * <p>
 * Punto de entrada de Spring Boot que arranca el contexto y expone los controladores REST.
 * </p>
 */
@SpringBootApplication
public class RistorinoApiApplication {

    /**
     * Método main que inicializa la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(RistorinoApiApplication.class, args);
    }

}
