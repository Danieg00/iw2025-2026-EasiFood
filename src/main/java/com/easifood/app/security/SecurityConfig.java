package com.easifood.app.security;

import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * ✅ 1) Cadena SOLO para recursos públicos.
     * Importante: debe ir ANTES que la de Vaadin.
     */
    @Bean
    @Order(0)
    SecurityFilterChain publicResourcesFilterChain(HttpSecurity http) throws Exception {
        return http
                // Solo se aplica a estas rutas:
                .securityMatcher("/images/**", "/uploads/**", "/imagenes/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Para recursos estáticos no necesitamos CSRF
                .csrf(csrf -> csrf.disable())
                .build();
    }

    /**
     * ✅ 2) Cadena principal de Vaadin (views + login).
     */
    @Bean
    @Order(1)
    SecurityFilterChain vaadinFilterChain(HttpSecurity http) throws Exception {

        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class, "/login");
        });

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> usuarioRepository.findByCorreo(username)
                .map(u -> User.withUsername(u.getCorreo())
                        .password(u.getContra())       // bcrypt en BD
                        .authorities(u.getRole())      // ROLE_CLIENTE, ROLE_GERENTE...
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
