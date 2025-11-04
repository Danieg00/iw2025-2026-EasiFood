package com.easifood.app.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

// This is the root path: http://localhost:8080
@Route("")
@PermitAll
public class MainView extends VerticalLayout {

    public MainView() {
        add(new Text("Bienvenido a EasiFood!"));
    }
}
