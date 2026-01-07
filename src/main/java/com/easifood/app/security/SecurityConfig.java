package com.easifood.app.security;

import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(0)
    SecurityFilterChain publicResourcesFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/images/**", "/uploads/**", "/imagenes/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain vaadinFilterChain(HttpSecurity http) throws Exception {

        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class, "/login");
        });

        // ✅ REDIRECCIÓN POR ROL AL HACER LOGIN
        http.formLogin(form -> form
                .successHandler(roleBasedSuccessHandler())
        );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String ctx = request.getContextPath();

            boolean isEmpleado = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_REPARTIDOR") || a.getAuthority().equals("ROLE_EMPLEADO"));

            boolean isGerente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (isEmpleado) {
                response.sendRedirect(ctx + "/home-empleado");
            } else if (isGerente) {
                response.sendRedirect(ctx + "/home-gerente");
            } else if (isCliente) {
                response.sendRedirect(ctx + "/home-cliente");
            } else {
                // fallback
                response.sendRedirect(ctx + "/home-cliente");
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> {
            String correo = (username == null) ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
            return usuarioRepository.findByCorreo(correo)
                    .map(u -> User.withUsername(u.getCorreo())
                            .password(u.getContra())
                            .authorities(u.getRole())
                            .build()
                    )
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}