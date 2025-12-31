package com.easifood.app.views;

import com.easifood.app.model.Empleado;
import com.easifood.app.service.EmpleadoService;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;

import com.vaadin.flow.component.html.Span;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@PageTitle("Área Repartidor Test")
@Route("home-empleado")
@PermitAll
//@RolesAllowed("ROLE_REPARTIDOR")

public class EmpleadoView extends VerticalLayout {

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    private final RestauranteService restauranteService;
    private final EmpleadoService empleadoService;

    public EmpleadoView(RestauranteService restauranteService, EmpleadoService empleadoService) {
        this.restauranteService = restauranteService;
        this.empleadoService = empleadoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        Tab tabTurno = new Tab("Mi turno");
        Tab tabEnvios = new Tab("Mis envíos");

        Tabs tabs = new Tabs(tabTurno, tabEnvios);
        tabs.setWidthFull();

        tabToContent.put(tabTurno, buildTurnoContent());
        tabToContent.put(tabEnvios, buildEnviosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabs, content);
        expand(content);

        showContent(tabTurno);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Repartidor");
        title.getStyle().set("margin", "0");

        Empleado empleado = empleadoService.empleadoActual();

        Span userInfo;

        if (empleado == null) {
            userInfo = new Span("Empleado no autenticado");
        } else if (empleado.getRestaurante() == null) {
            userInfo = new Span("Sin restaurante asignado");
        } else {
            userInfo = new Span(
                    empleado.getNombre() + " — " +
                            empleado.getRestaurante().getNombre()
            );
        }

        userInfo.getStyle().set("opacity", "0.7");

        header.add(title, userInfo);
        return header;
    }

    /*private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Repartidor");
        title.getStyle().set("margin", "0");

        Empleado empleado = empleadoService.findFirst();

        Span userInfo;

        if (empleado == null) {
            userInfo = new Span("No hay empleados en la BD");
        } else if (empleado.getRestaurante() == null) {
            userInfo = new Span("Empleado sin restaurante asignado");
        } else {
            userInfo = new Span(
                    "Restaurante: " + empleado.getRestaurante().getNombre()
            );
        }

        userInfo.getStyle().set("opacity", "0.7");

        header.add(title, userInfo);
        return header;
    }*/

    /*    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Repartidor");
        title.getStyle().set("margin", "0");

        header.add(title);
        return header;
    }
    */

    /*private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Cliente v2");
        title.getStyle().set("margin", "0");

        //Span userInfo = new Span("HEADER NUEVO CARGADO");
        //userInfo.getStyle().set("opacity", "0.7");

        /*
        List<Restaurante> restaurantes = restauranteService.findAll();

        Span userInfo = new Span(
                "Restaurantes encontrados: " + restaurantes.size()
        );
        userInfo.getStyle().set("opacity", "0.7");
        */
/*
        Restaurante restaurante = restauranteService.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        Span userInfo = new Span(
                restaurante != null
                        ? "Restaurante: " + restaurante.getNombre()
                        : "No hay restaurantes en la BD"
        );
        userInfo.getStyle().set("opacity", "0.7");

        header.add(title, userInfo);
        return header;
    }*/

    private void showContent(Tab selected) {
        content.removeAll();
        Component c = tabToContent.get(selected);
        if (c != null) content.add(c);
    }

    private Component buildTurnoContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mi turno");
        title.getStyle().set("margin", "0");

        TextField turno = new TextField("Horario del turno");
        turno.setReadOnly(true);
        turno.setValue("Turno actual: 9:00 AM - 5:00 PM"); // Valor hardcodeado

        layout.add(title, turno);
        return layout;
    }

    private Component buildEnviosContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mis envíos");
        title.getStyle().set("margin", "0");

        TextField search = new TextField();
        search.setPlaceholder("Buscar por cliente...");
        search.setClearButtonVisible(true);
        search.setWidth("320px");

        // Lista de envíos hardcodeada
        List<Envio> envios = new ArrayList<>();
        envios.add(new Envio(1, "Juan Pérez", "Calle Falsa 123", "En espera", "2023-12-28"));
        envios.add(new Envio(2, "María López", "Av. Siempre Viva 742", "En camino", "2023-12-27"));
        envios.add(new Envio(3, "Carlos García", "Plaza Mayor 456", "Entregado", "2023-12-29"));

        ListDataProvider<Envio> provider = new ListDataProvider<>(envios);

        Grid<Envio> grid = new Grid<>(Envio.class, false);
        grid.setSizeFull();

        grid.addColumn(Envio::getClienteNombre)
                .setHeader("Cliente")
                .setAutoWidth(true);

        grid.addColumn(Envio::getDireccion)
                .setHeader("Dirección")
                .setAutoWidth(true);

        grid.addColumn(Envio::getEstado)
                .setHeader("Estado")
                .setAutoWidth(true);

        grid.addColumn(Envio::getFechaAsignacion)
                .setHeader("Fecha de asignación")
                .setAutoWidth(true);

        // Columna de acciones con el botón para cambiar estado
        grid.addComponentColumn(envio ->
                new Button("Cambiar estado", e -> cambiarEstado(envio))
        ).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        grid.setDataProvider(provider);

        search.addValueChangeListener(e -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            provider.setFilter(envio ->
                    envio.getClienteNombre() != null && envio.getClienteNombre().toLowerCase().contains(term)
            );
        });

        layout.add(title, search, grid);
        layout.expand(grid);
        return layout;
    }

    private void cambiarEstado(Envio envio) {
        // Cambia el estado cíclicamente entre "En espera", "En camino", "Entregado"
        switch (envio.getEstado()) {
            case "En espera":
                envio.setEstado("En camino");
                break;
            case "En camino":
                envio.setEstado("Entregado");
                break;
            case "Entregado":
                envio.setEstado("En espera");
                break;
        }
        // Actualiza la vista después de cambiar el estado
        UI.getCurrent().getPage().reload();
    }

    // Clase Envio incluida en el mismo archivo
    public static class Envio {
        private int id;
        private String clienteNombre;
        private String direccion;
        private String estado;
        private String fechaAsignacion;

        public Envio(int id, String clienteNombre, String direccion, String estado, String fechaAsignacion) {
            this.id = id;
            this.clienteNombre = clienteNombre;
            this.direccion = direccion;
            this.estado = estado;
            this.fechaAsignacion = fechaAsignacion;
        }

        public int getId() {
            return id;
        }

        public String getClienteNombre() {
            return clienteNombre;
        }

        public String getDireccion() {
            return direccion;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getFechaAsignacion() {
            return fechaAsignacion;
        }
    }
}
