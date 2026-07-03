package com.appbit.backend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para todos los orígenes. El frontend nunca envía credenciales
 * (cookies/Authorization) al backend, así que no hay riesgo en permitir cualquier
 * origen. Sin esto, correr backend y frontend en puertos distintos en local
 * (ej. localhost:5173 -> localhost:8080) falla con "Failed to fetch": en
 * producción esto lo resolvía el proxy de Hugging Face Spaces, que no existe
 * al correr el backend directamente.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}