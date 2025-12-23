package com.easifood.app.security;

import com.easifood.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Recursos estáticos públicos (ajusta si lo necesitas)
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/images/**").permitAll()
        );

        // Configuración Vaadin + Spring Security (reemplazo de VaadinWebSecurity)
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class); // registra tu LoginView (Flow)
        });

        // Opcional: si quieres forzar que tras login siempre vaya a /home:
        // http.formLogin(form -> form.defaultSuccessUrl("/home", true));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
