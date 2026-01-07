package com.easifood.app.service;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Locale;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Empleado> empleadosDelRestaurante(Restaurante restaurante) {
        return empleadoRepository.findByRestaurante(restaurante);
    }

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    /**
     * ✅ Guarda empleado asegurando:
     * - correo normalizado
     * - contraseña en BCrypt (si viene en texto plano)
     */
    public Empleado guardar(Empleado empleado) {

        if (empleado == null) return null;

        // Normalizar correo
        if (empleado.getCorreo() != null) {
            empleado.setCorreo(empleado.getCorreo().trim().toLowerCase(Locale.ROOT));
        }

        // Encriptar contraseña si NO parece BCrypt ya
        if (empleado.getContra() != null && !empleado.getContra().isBlank()) {
            String c = empleado.getContra().trim();
            boolean yaEsBCrypt = c.startsWith("$2a$") || c.startsWith("$2b$") || c.startsWith("$2y$");
            if (!yaEsBCrypt) {
                empleado.setContra(passwordEncoder.encode(c));
            }
        }

        return empleadoRepository.save(empleado);
    }

    public Empleado findFirst() {
        return empleadoRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Empleado empleadoActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }

        // auth.getName() es el username => en tu caso es el correo
        String correo = auth.getName().trim().toLowerCase(Locale.ROOT);
        return empleadoRepository.findByCorreo(correo).orElse(null);
    }

    public Empleado registrarEmpleado(
            String nombre,
            String apellidos,
            String correo,
            String contraPlain,
            String puesto,
            BigDecimal salario,
            Restaurante restaurante,
            String imagenUrl // si no usas imagen en empleado, ponlo a null y listo
    ) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        String correoNorm = correo.trim().toLowerCase(Locale.ROOT);

        // Evitar duplicados (mismo email)
        Optional<Empleado> existente = empleadoRepository.findByCorreo(correoNorm);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (contraPlain == null || contraPlain.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        String passwordEncriptada = passwordEncoder.encode(contraPlain);

        Empleado e = new Empleado(
                nombre,
                apellidos,
                correoNorm,
                imagenUrl,
                passwordEncriptada,
                "ROLE_REPARTIDOR",   // o ROLE_EMPLEADO (pon el que uses de verdad)
                puesto,
                salario,
                restaurante
        );

        return empleadoRepository.save(e);
    }
}
