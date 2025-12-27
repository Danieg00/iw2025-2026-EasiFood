package com.easifood.app.views;

import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashMap;
import java.util.Map;

@PageTitle("Área Cliente")
@Route("home-cliente")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteHomeView extends VerticalLayout {

    private final RestauranteService restauranteService;

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    public ClienteHomeView(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        Tab tabExplorar = new Tab("Explorar");
        Tab tabCarrito = new Tab("Carrito");
        Tab tabPedidos = new Tab("Mis pedidos");

        Tabs tabs = new Tabs(tabExplorar, tabCarrito, tabPedidos);
        tabs.setWidthFull();

        tabToContent.put(tabExplorar, buildExplorarContent());
        tabToContent.put(tabCarrito, buildCarritoContent());
        tabToContent.put(tabPedidos, buildPedidosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabs, content);
        expand(content);

        showContent(tabExplorar);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Cliente");
        title.getStyle().set("margin", "0");

        Span userInfo = new Span("Cliente");
        userInfo.getStyle().set("opacity", "0.7");

        header.add(title, userInfo);
        return header;
    }

    private void showContent(Tab selected) {
        content.removeAll();
        Component c = tabToContent.get(selected);
        if (c != null) content.add(c);
    }

    private Component buildExplorarContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Explorar restaurantes");
        title.getStyle().set("margin", "0");

        TextField search = new TextField();
        search.setPlaceholder("Buscar por nombre...");
        search.setClearButtonVisible(true);
        search.setWidth("320px");

        ListDataProvider<Restaurante> provider =
                new ListDataProvider<>(restauranteService.findAll());

        Grid<Restaurante> grid = new Grid<>(Restaurante.class, false);
        grid.setSizeFull();
        grid.getStyle().set("--vaadin-grid-row-height", "60px");

        // Columna imagen (opción B: URL guardada en BD)
        grid.addComponentColumn(r -> {
            String url = (r.getImagenUrl() != null && !r.getImagenUrl().isBlank())
                    ? r.getImagenUrl()
                    : "/images/restaurantes/default.jpg";

            Image img = new Image(url, "Foto " + r.getNombre());
            img.setWidth("72px");
            img.setHeight("48px");
            img.getStyle().set("object-fit", "cover");
            img.getStyle().set("border-radius", "8px");
            return img;
        }).setHeader("Foto").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(Restaurante::getNombre)
                .setHeader("Nombre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Restaurante::getDireccion)
                .setHeader("Dirección")
                .setAutoWidth(true);

        grid.addColumn(Restaurante::getTelefono)
                .setHeader("Teléfono")
                .setAutoWidth(true);

        grid.addColumn(Restaurante::getHorario)
                .setHeader("Horario")
                .setAutoWidth(true);

        grid.addComponentColumn(r ->
                new Button("Ver", e -> UI.getCurrent().navigate("cliente-restaurante/" + r.getId()))
        ).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        grid.setDataProvider(provider);

        search.addValueChangeListener(e -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            provider.setFilter(r ->
                    r.getNombre() != null && r.getNombre().toLowerCase().contains(term)
            );
        });

        layout.add(title, search, grid);
        layout.expand(grid);
        return layout;
    }

    private Component buildCarritoContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H1("Carrito"));
        layout.add(new Span("Pendiente: añadir/quitar productos, elegir tipo de pedido (domicilio / recoger / mesa) y pagar."));
        return layout;
    }

    private Component buildPedidosContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H1("Mis pedidos"));
        layout.add(new Span("Pendiente: listar pedidos del cliente, modificar/cancelar y generar ticket."));
        return layout;
    }
}