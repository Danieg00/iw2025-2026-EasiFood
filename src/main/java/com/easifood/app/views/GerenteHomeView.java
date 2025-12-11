package com.easifood.app.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Área Gerente")
@Route("home-gerente")
@RolesAllowed("ROLE_GERENTE")
public class GerenteHomeView extends VerticalLayout {

    public GerenteHomeView() {
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(new H1("Panel del Gerente — Gestión del Restaurante"));
    }
}
