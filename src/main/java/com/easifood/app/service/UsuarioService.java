package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.ClienteRepository;
import com.easifood.app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestauranteService restauranteService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder passwordEncoder,
            RestauranteService restauranteService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.restauranteService = restauranteService;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================
    public Usuario findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    // =====================================================
    // REGISTRO CLIENTE
    // =====================================================
    public void registrarCliente(
            String nombre,
            String apellidos,
            String correo,
            String contra,
            String direccion1,
            String direccion2,
            String imagenUrl
    ) {

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String passwordEncriptada = passwordEncoder.encode(contra);

        Cliente cliente = new Cliente(
                nombre,
                apellidos,
                correo,
                imagenUrl,
                passwordEncriptada,
                direccion1,
                direccion2
        );

        clienteRepository.save(cliente);
    }

    // =====================================================
    // REGISTRO GERENTE
    // =====================================================
    public void registrarGerente(
            String nombre,
            String apellidos,
            String correo,
            String contra,
            String nombreRest,
            String direccion,
            Integer aforo,
            String telefono,
            String horario,
            String imagenUrl
    ) {

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        String passwordEncriptada = passwordEncoder.encode(contra);

        // Crear restaurante
        Restaurante restaurante = new Restaurante(
                nombreRest,
                direccion,
                aforo,
                telefono,
                horario
        );
        restauranteService.save(restaurante);

        // Crear gerente
        Gerente gerente = new Gerente(
                nombre,
                apellidos,
                correo,
                imagenUrl,
                passwordEncriptada,
                restaurante
        );

        usuarioRepository.save(gerente);
    }

    public Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    public void logout() {
        UI.getCurrent().getSession().close();
        SecurityContextHolder.clearContext();
        UI.getCurrent().getPage().setLocation("/login");
    }
}
