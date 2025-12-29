package com.easifood.app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@PermitAll
public class HomeRedirectView extends VerticalLayout {

    public HomeRedirectView() {
        setVisible(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // OJO: cuando no estás logueado, muchas veces auth existe pero es "anonymousUser"
        if (auth == null || auth.getAuthorities() == null || auth.getAuthorities().isEmpty()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
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
