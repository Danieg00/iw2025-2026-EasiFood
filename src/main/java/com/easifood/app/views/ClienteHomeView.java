package com.easifood.app.views;

import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.button.ButtonVariant;



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


        tabs.setWidth("auto");
        tabs.getStyle().set("margin-top", "0.5rem");

        HorizontalLayout tabsWrapper = new HorizontalLayout(tabs);
        tabsWrapper.setWidthFull();
        tabsWrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        tabsWrapper.setAlignItems(Alignment.CENTER);

        tabToContent.put(tabExplorar, buildExplorarContent());
        tabToContent.put(tabCarrito, buildCarritoContent());
        tabToContent.put(tabPedidos, buildPedidosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabsWrapper, content);
        expand(content);

        showContent(tabExplorar);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    private Component buildHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("EasiFood");
        title.getStyle()
                .set("margin", "0.5rem 0 0.25rem 0")
                .set("font-weight", "800")
                .set("font-size", "2.4rem")
                .set("letter-spacing", "0.5px");

        Span subtitle = new Span("Tu comida, fácil y rápido");
        subtitle.getStyle()
                .set("opacity", "0.7")
                .set("font-size", "0.95rem");

        header.add(title, subtitle);
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
        layout.setPadding(false);
        layout.setSpacing(true);

        TextField search = new TextField();
        search.setPlaceholder("Buscar por nombre...");
        search.setClearButtonVisible(true);
        search.setWidth("360px");

        HorizontalLayout searchWrapper = new HorizontalLayout(search);
        searchWrapper.setWidthFull();
        searchWrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        // Contenedor tipo galería
        FlexLayout gallery = new FlexLayout();
        gallery.setWidthFull();
        gallery.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("justify-content", "center"); // ✅ centra las cards si hay hueco

        // Datos
        List<Restaurante> all = restauranteService.findAll();
        List<Restaurante> filtered = new ArrayList<>(all);

        renderGallery(gallery, filtered);

        // Filtro
        search.addValueChangeListener(e -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            filtered.clear();

            if (term.isBlank()) {
                filtered.addAll(all);
            } else {
                for (Restaurante r : all) {
                    if (r.getNombre() != null && r.getNombre().toLowerCase().contains(term)) {
                        filtered.add(r);
                    }
                }
            }
            renderGallery(gallery, filtered);
        });

        layout.add(searchWrapper, gallery);
        layout.expand(gallery);
        return layout;
    }

    private void renderGallery(FlexLayout gallery, List<Restaurante> restaurantes) {
        gallery.removeAll();

        if (restaurantes.isEmpty()) {
            Span empty = new Span("No hay restaurantes que coincidan con la búsqueda.");
            empty.getStyle().set("opacity", "0.7");
            gallery.add(empty);
            return;
        }

        for (Restaurante r : restaurantes) {
            gallery.add(createRestauranteCard(r));
        }
    }

    private Component createRestauranteCard(Restaurante r) {

        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);

        // Responsive width
        card.getStyle()
                .set("flex", "1 1 calc(33.333% - 16px)") // desktop
                .set("max-width", "calc(33.333% - 16px)")
                .set("min-width", "260px")
                .set("border-radius", "18px")
                .set("overflow", "hidden")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("transition", "transform 0.12s ease-in-out");

        // Imagen
        String url = (r.getImagenUrl() != null && !r.getImagenUrl().isBlank())
                ? r.getImagenUrl()
                : "/images/restaurantes/default.jpg";

        Image img = new Image(url, "Foto " + safe(r.getNombre()));
        img.setWidthFull();
        img.setHeight("160px");
        img.getStyle().set("object-fit", "cover");

        // Contenido
        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);

        H2 name = new H2(safe(r.getNombre()));
        name.getStyle().set("margin", "0");

        Paragraph direccion = new Paragraph("📍 " + safe(r.getDireccion()));
        direccion.getStyle().set("margin", "0.25rem 0");
        direccion.getStyle().set("opacity", "0.85");

        Paragraph meta = new Paragraph(
                "📞 " + safe(r.getTelefono()) + " · ⏰ " + safe(r.getHorario())
        );
        meta.getStyle().set("margin", "0");
        meta.getStyle().set("opacity", "0.75");
        meta.getStyle().set("font-size", "0.9rem");

        Button ver = new Button("Ver carta", e -> UI.getCurrent().navigate("cliente-restaurante/" + r.getId()));
        ver.setWidthFull();
        ver.getStyle()
                .set("margin-top", "0.75rem")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        ver.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        body.add(name, direccion, meta, ver);
        card.add(img, body);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
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