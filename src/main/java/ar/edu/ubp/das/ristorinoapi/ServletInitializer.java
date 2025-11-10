package ar.edu.ubp.das.ristorinoapi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador de servlet para despliegues en contenedores externos (WAR).
 * Permite que la aplicación Spring Boot se configure cuando se empaqueta como WAR
 * y se despliega en servidores como Tomcat tradicional.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * Configura la aplicación indicando la clase fuente principal.
     * @param application builder provisto por el contenedor
     * @return builder configurado
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RistorinoApiApplication.class);
    }

}
