package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.repository.ClienteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class ClienteService implements UserDetailsService {

    private final ClienteRepository repo;
    private final PasswordEncoder encoder;

    public ClienteService(ClienteRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Cliente register(String nombre,
                            String apellidos,
                            String correo,
                            String contra,
                            String direccion1,
                            String direccion2) {

        if (repo.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con ese correo");
        }

        String contraCodificada = encoder.encode(contra);

        Cliente cliente = new Cliente(
                nombre,
                apellidos,
                correo,
                contraCodificada,
                direccion1,
                direccion2,
                "ROLE_USER"
        );

        return repo.save(cliente);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cliente cliente = repo.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un cliente con ese correo"));

        return User
                .withUsername(cliente.getCorreo())
                .password(cliente.getContra())
                .authorities(Collections.singleton(() -> cliente.getRole()))
                .build();
    }
}
