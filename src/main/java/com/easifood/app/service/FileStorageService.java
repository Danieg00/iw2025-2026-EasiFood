package com.easifood.app.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // ==========================
    // USUARIOS
    // ==========================
    public String saveUserImage(InputStream inputStream, String originalFilename) {
        return saveImage(inputStream, originalFilename, "usuarios");
    }

    // ==========================
    // PRODUCTOS
    // ==========================
    public String saveProductImage(InputStream inputStream, String originalFilename) {
        return saveImage(inputStream, originalFilename, "productos");
    }

    // ==========================
    // MÉTODO COMÚN
    // ==========================
    private String saveImage(InputStream inputStream,
                             String originalFilename,
                             String folder) {
        try {
            String ext = getExt(originalFilename);
            String filename = UUID.randomUUID() + ext;

            // 📁 uploads/<folder>/
            Path dir = Path.of("uploads", folder);
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

            // 🌐 URL pública (NO ruta física)
            return "/imagenes/" + folder + "/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }
    }

    private String getExt(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx < 0) return "";
        String ext = name.substring(idx).toLowerCase();
        return ext.matches("\\.[a-z0-9]{1,6}") ? ext : "";
    }
}
