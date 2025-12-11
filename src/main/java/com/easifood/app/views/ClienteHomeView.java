package com.easifood.app.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Área Cliente")
@Route("home-cliente")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteHomeView extends VerticalLayout {

    public ClienteHomeView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(new H1("Bienvenido al área del Cliente"));
    }
}
