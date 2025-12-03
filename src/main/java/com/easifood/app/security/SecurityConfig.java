package com.easifood.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Vaadin hace POST internos a '/', si dejamos CSRF por defecto nos casca 403
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos de Vaadin (JS, etc.)
                .requestMatchers("/VAADIN/**", "/favicon.ico", "/images/**", "/icons/**",
                                 "/manifest.webmanifest", "/sw.js", "/offline.html")
                    .permitAll()
                // El resto de rutas requieren estar logueado
                .anyRequest().authenticated()
            )
            // Formulario de login por defecto de Spring
            .formLogin(form -> form
                .loginPage("/login")       // usa la página de login por defecto en /login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
            );

        return http.build();
    }

    // Usuario en memoria: root / root
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User
            .withUsername("root")
            .password("{noop}root") // {noop} = sin codificar, texto plano
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }
}
