package com.easifood.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Path.of("uploads").toAbsolutePath().normalize();

        // /imagenes/<folder>/<file> -> uploads/<folder>/<file>
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(0);

        // (opcional) mantener también /uploads/** si ya lo usas en otros sitios
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(0);
    }
}
