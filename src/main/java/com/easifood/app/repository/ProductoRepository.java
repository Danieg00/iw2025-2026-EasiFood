package com.easifood.app.repository;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByRestaurante(Restaurante restaurante);
    List<Producto> findByRestauranteId(Long restauranteId);
}
