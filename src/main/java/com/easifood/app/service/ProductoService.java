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

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Producto findById(Long id) {
        if (id == null) return null;
        return productoRepository.findById(id).orElse(null);
    }

    public List<Producto> productosDelRestaurante(Restaurante restaurante) {
        if (restaurante == null) return List.of();
        return productoRepository.findByRestaurante(restaurante);
    }

    public Producto save(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("Producto inválido");
        return productoRepository.save(producto);
    }
    public Producto guardar(Producto producto) {
        return save(producto);
    }

    public void deleteById(Long id) {
        if (id == null) return;
        productoRepository.deleteById(id);
    }
}
