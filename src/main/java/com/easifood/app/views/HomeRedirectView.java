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
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // ✅ No logueado (ojo: auth puede existir pero ser anónimo)
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            UI.getCurrent().navigate("login");
            return;
        }

        boolean isEmpleado = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_EMPLEADO".equals(a.getAuthority()) || "ROLE_REPARTIDOR".equals(a.getAuthority()));

        boolean isGerente = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GERENTE".equals(a.getAuthority()));

        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

        if (isEmpleado) {
            UI.getCurrent().navigate("home-empleado");
        } else if (isGerente) {
            UI.getCurrent().navigate("home-gerente");
        } else if (isCliente) {
            UI.getCurrent().navigate("home-cliente");
        } else {
            UI.getCurrent().navigate("login");
        }
    }
}
