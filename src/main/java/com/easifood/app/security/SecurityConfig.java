package com.easifood.app.security;

import com.easifood.app.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Vaadin internal requests y CSRF
                .csrf(csrf -> csrf.disable())

                // Permitir recursos estáticos
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**",
                                "/VAADIN/**",
                                "/login",
                                "/favicon.ico",
                                "/robots.txt",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline.html",
                                "/icons/**").permitAll()
                        .anyRequest().authenticated()
                )

                //Este login no es necesario al usar Spring Security en lugar de Vaadin
//                // Login personalizado con LoginView
//                .formLogin(form -> form
//                        .loginPage("/login")  // ruta de login, puede ser LoginView
//                        .defaultSuccessUrl("/home", true)
//                        .permitAll()
//                )
                .requestCache(cache -> cache.disable())
                .httpBasic(httpBasic -> {})
                // Logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
