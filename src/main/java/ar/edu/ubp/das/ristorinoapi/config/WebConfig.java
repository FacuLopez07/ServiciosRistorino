package ar.edu.ubp.das.ristorinoapi.config;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuración global de CORS.
 * <p>Registra un filtro con la máxima precedencia para que las peticiones desde
 * el frontend Angular (http://localhost:4200) puedan acceder a los endpoints
 * de esta API (http://localhost:8080) incluyendo peticiones preflight (OPTIONS).
 * Sin esta configuración, el navegador bloqueará las llamadas por la política
 * de mismo origen (Same-Origin Policy).</p>
 * <p>Si luego se agregan otros orígenes (deploy en otra URL), se deben agregar
 * aquí o usar patrones con {@code setAllowedOriginPatterns}.</p>
 */
@Configuration
public class WebConfig {

    /**
     * Registra el filtro CORS para toda la aplicación.
     *
     * Ajustes:
     * - allowCredentials: permite incluir cookies / auth headers si se necesitaran.
     * - allowedOrigins: origen autorizado (Angular local).
     * - allowedMethods: métodos HTTP permitidos.
     * - allowedHeaders: encabezados que el cliente puede enviar.
     *
     * Nota: Se usa Content-Type (con guión) y no "ContentType".
     * Si se necesitan exponer headers de respuesta se puede usar
     * {@code config.addExposedHeader("X-Custom-Header")}. Para permitir todos
     * se podría usar un comodín en headers, pero aquí se fija la lista mínima.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList(
                "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"
        ));
        // Si se necesitaran todos los headers: config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // asegura que se ejecute antes que otros filtros
        return bean;
    }
}

