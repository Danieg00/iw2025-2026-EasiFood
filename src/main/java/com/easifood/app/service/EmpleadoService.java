
package com.easifood.app.service;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado> empleadosDelRestaurante(Restaurante restaurante) {
        return empleadoRepository.findByRestaurante(restaurante);
    }

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    public Empleado guardar(Empleado empleado) {
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
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        // 🔴 MODO DESARROLLO
        /*if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return empleadoRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElse(null);
        }*/

        return empleadoRepository.findByCorreo(auth.getName()).orElse(null);
    }

}
