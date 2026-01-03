package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.ClienteRepository;
import com.easifood.app.repository.UsuarioRepository;
import com.vaadin.flow.component.UI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        String correo = auth.getName();
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

        Restaurante restaurante = new Restaurante(
                nombreRest,
                direccion,
                aforo,
                telefono,
                horario
        );
        restauranteService.save(restaurante);

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

    // =====================================================
    // PERFIL: CAMBIOS
    // =====================================================
    public Usuario guardarCambiosPerfil(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public boolean updateCorreo(Long userId, String nuevoCorreo) {
        if (userId == null) throw new IllegalArgumentException("Usuario inválido");
        if (nuevoCorreo == null || nuevoCorreo.isBlank()) throw new IllegalArgumentException("Correo inválido");

        Usuario u = usuarioRepository.findById(userId).orElse(null);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        String actual = u.getCorreo();
        if (nuevoCorreo.equalsIgnoreCase(actual)) return false;

        if (usuarioRepository.findByCorreo(nuevoCorreo).isPresent()) {
            throw new IllegalArgumentException("Ese correo ya está registrado");
        }

        u.setCorreo(nuevoCorreo);
        usuarioRepository.save(u);

        // ⚠️ IMPORTANTE: el "username" del login es el correo.
        // Tras cambiarlo, lo más correcto es forzar cerrar sesión para que se vuelva a autenticar.
        logout();

        return true;
    }

    @Transactional
    public void changePassword(Long userId, String actualPlain, String nuevaPlain) {
        if (userId == null) throw new IllegalArgumentException("Usuario inválido");

        Usuario u = usuarioRepository.findById(userId).orElse(null);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        if (actualPlain == null || actualPlain.isBlank()) {
            throw new IllegalArgumentException("La contraseña actual es obligatoria");
        }
        if (nuevaPlain == null || nuevaPlain.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña es obligatoria");
        }

        // ✅ En tu modelo la contraseña encriptada está en "contra"
        if (!passwordEncoder.matches(actualPlain, u.getContra())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        u.setContra(passwordEncoder.encode(nuevaPlain));
        usuarioRepository.save(u);

        // Opcional: forzar logout para que vuelva a iniciar sesión con la nueva contraseña
        logout();
    }

    // =====================================================
    // LOGOUT
    // =====================================================
    public void logout() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getSession().close();
        }
        SecurityContextHolder.clearContext();
        if (ui != null) {
            ui.getPage().setLocation("/login");
        }
    }
}
