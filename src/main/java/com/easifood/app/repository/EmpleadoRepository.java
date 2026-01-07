package com.easifood.app.repository;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByRestaurante(Restaurante restaurante);
    Optional<Empleado> findByCorreo(String correo);
}
