package com.easifood.app.security;

import com.easifood.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configuración por defecto de Vaadin (maneja rutas, /VAADIN/**, etc.)
        super.configure(http);

        // Esta es la vista de login de Vaadin que ya tienes (@Route("login"))
        setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ⛔ IMPORTANTE:
    // NO declares aquí ningún @Bean UserDetailsService.
    // Spring detecta automáticamente tu ClienteService porque implementa UserDetailsService.
}
