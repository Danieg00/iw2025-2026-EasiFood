
package com.easifood.app.service;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado> empleadosDelRestaurante(Restaurante restaurante) {
        return empleadoRepository.findByRestaurante(restaurante);
    }

    public Empleado guardar(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }
}
