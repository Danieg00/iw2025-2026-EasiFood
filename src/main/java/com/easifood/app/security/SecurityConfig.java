package com.easifood.app.security;

import com.easifood.app.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Permitir imágenes estáticas
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/images/**").permitAll()
        );

        super.configure(http);

        // Login personalizado → tras iniciar sesión va a /home
        setLoginView(http, LoginView.class, "/home");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
