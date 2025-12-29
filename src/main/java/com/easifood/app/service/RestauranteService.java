package com.easifood.app.service;

import com.easifood.app.model.Restaurante;
import com.easifood.app.repository.RestauranteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    public List<Restaurante> findAll() {
        return restauranteRepository.findAll();
    }

    public Restaurante findById(Long id) {
        return restauranteRepository.findById(id).orElse(null);
    }

    public Restaurante save(Restaurante restaurante) {
        return restauranteRepository.save(restaurante);
    }
}