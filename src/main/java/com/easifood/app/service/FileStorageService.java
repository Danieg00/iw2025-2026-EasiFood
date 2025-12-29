package com.easifood.app.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    public String saveUserImage(InputStream inputStream, String originalFilename) {
        try {
            String ext = getExt(originalFilename);
            String filename = UUID.randomUUID() + ext;

            Path dir = Path.of("uploads", "usuarios");
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/usuarios/" + filename;
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
