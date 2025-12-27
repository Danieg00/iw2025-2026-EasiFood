package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;

import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.repository.RestauranteRepository;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final PasswordEncoder encoder;

    // ✅ ÚNICO CONSTRUCTOR VÁLIDO
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RestauranteRepository restauranteRepository,
                          PasswordEncoder encoder) {

        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.encoder = encoder;
    }

    // ============================================================
    // REGISTRO DE CLIENTE
    // ============================================================
    public Cliente registrarCliente(String nombre,
                                    String apellidos,
                                    String correo,
                                    String contra,
                                    String direccion1,
                                    String direccion2) {

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ese correo ya está registrado.");
        }

        String contraCodificada = encoder.encode(contra);

        Cliente cliente = new Cliente(
                nombre,
                apellidos,
                correo,
                null,
                contraCodificada,
                direccion1,
                direccion2
        );

        return usuarioRepository.save(cliente);
    }

    // ============================================================
    // LOGIN SPRING SECURITY
    // ============================================================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese correo."));

        return User
                .withUsername(usuario.getCorreo())
                .password(usuario.getContra())
                .authorities(usuario.getRole())
                .build();
    }

    // ============================================================
    // REGISTRO DE GERENTE Y RESTAURANTE
    // ============================================================
    public Gerente registrarGerente(String nombre,
                                    String apellidos,
                                    String correo,
                                    String contra,
                                    String nombreRest,
                                    String direccion,
                                    Integer aforo,
                                    String telefono,
                                    String horario) {

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ese correo ya está registrado.");
        }

        String contraCodificada = encoder.encode(contra);

        // buscar restaurante por nombre + direccion
        Restaurante restaurante = restauranteRepository
                .findByNombreAndDireccion(nombreRest, direccion)
                .orElse(null);

        if (restaurante == null) {
            // crear restaurante nuevo
            restaurante = new Restaurante(nombreRest, direccion, aforo, telefono, horario);
            restaurante = restauranteRepository.save(restaurante);
        } else {
            // SI el restaurante ya existe y tiene gerente → ERROR
            if (restaurante.getGerente() != null) {
                throw new IllegalArgumentException("Ese restaurante ya tiene un gerente asignado.");
            }
        }

        // crear gerente
        Gerente gerente = new Gerente(
                nombre,
                apellidos,
                correo,
                null,
                contraCodificada,
                restaurante
        );

        // RELACIÓN BIDIRECCIONAL
        restaurante.setGerente(gerente);

        // guardar gerente
        return (Gerente) usuarioRepository.save(gerente);
    }

}