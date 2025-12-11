package com.easifood.app.repository;

import com.easifood.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    Optional<Restaurante> findByNombreAndDireccion(String nombre, String direccion);
}
