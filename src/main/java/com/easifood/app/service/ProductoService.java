package com.easifood.app.service;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> productosDelRestaurante(Restaurante restaurante) {
        return productoRepository.findByRestaurante(restaurante);
    }
    
    public List<Producto> findByRestauranteId(Long restauranteId) {
        return productoRepository.findByRestauranteId(restauranteId);
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }
}
