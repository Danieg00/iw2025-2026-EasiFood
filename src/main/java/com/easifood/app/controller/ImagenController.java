package com.easifood.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class ImagenController {

    @GetMapping("/imagenes/**")
    @ResponseBody
    public ResponseEntity<Resource> verImagen(HttpServletRequest request) throws IOException {

        // Quita el prefijo /imagenes/
        String path = request.getRequestURI().replace("/imagenes/", "");

        // Apunta a uploads/<path>
        Path file = Path.of("uploads").resolve(path);

        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file))
                .body(resource);
    }
}
