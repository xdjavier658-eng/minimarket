package com.minimarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuración de recursos estáticos.
 *
 * Registra los assets del frontend React (JS, CSS, fuentes, imágenes)
 * con máxima prioridad, de modo que el SpaController no los intercepte.
 *
 * Spring Boot sirve /resources/static/** automáticamente, pero al tener
 * el SpaController con @RequestMapping generales, es necesario dejar
 * explícito que /assets/** apunta a los archivos físicos.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Assets del build de React (JS, CSS, fuentes, imágenes)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        // Imágenes de productos subidas
        // Usar Path.toUri() para garantizar formato correcto en Windows
        Path uploadsPath = Paths.get(System.getProperty("user.dir"), "uploads", "img").toAbsolutePath();
        String imageLocation = uploadsPath.toUri().toString();
        logger.info("📁 Sirviendo imágenes desde: {}", imageLocation);

        registry.addResourceHandler("/img/**")
                .addResourceLocations(imageLocation);

        // Raíz de archivos estáticos
        registry.addResourceHandler("/*.js", "/*.css", "/*.ico",
                "/*.png", "/*.svg", "/*.webp",
                "/*.woff", "/*.woff2", "/*.ttf")
                .addResourceLocations("classpath:/static/");
    }
}
