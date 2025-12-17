package com.easifood.app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@PermitAll
public class HomeRedirectView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        boolean isGerente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

        if (isCliente) {
            UI.getCurrent().navigate("home-cliente");
        } else if (isGerente) {
            UI.getCurrent().navigate("home-gerente");
        } else {
            UI.getCurrent().navigate("login");
        }
    }
}
