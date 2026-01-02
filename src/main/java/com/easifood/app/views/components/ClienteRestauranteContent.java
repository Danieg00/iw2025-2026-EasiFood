package com.easifood.app.views.components;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.CarritoService;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClienteRestauranteContent extends VerticalLayout {

    private final CarritoService carritoService;
    private final Runnable onCartChange;
    private final Long restauranteId;

    public ClienteRestauranteContent(Long restauranteId,
                                     RestauranteService restauranteService,
                                     ProductoService productoService,
                                     CarritoService carritoService,
                                     Runnable onCartChange) {

        this.carritoService = carritoService;
        this.onCartChange = onCartChange;
        this.restauranteId = restauranteId;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Restaurante r = restauranteService.findById(restauranteId);
        if (r == null) {
            add(new Paragraph("Restaurante no encontrado."));
            return;
        }

        add(buildRestauranteHeader(r));

        List<Producto> productos = productoService.productosDelRestaurante(r);

        H3 subtitulo = new H3("Productos");
        subtitulo.getStyle().set("margin", "0.75rem 0 0 0");
        add(subtitulo);

        if (productos.isEmpty()) {
            Span empty = new Span("Este restaurante todavía no tiene productos.");
            empty.getStyle().set("opacity", "0.7");
            add(empty);
            return;
        }

        NumberFormat eur = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        FlexLayout productosGrid = new FlexLayout();
        productosGrid.setWidthFull();
        productosGrid.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("margin-top", "0.5rem");

        for (Producto p : productos) {
            productosGrid.add(buildProductoCard(p, eur));
        }

        add(productosGrid);
    }

    private VerticalLayout buildRestauranteHeader(Restaurante r) {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        String url = (r.getImagenUrl() != null && !r.getImagenUrl().isBlank())
                ? r.getImagenUrl()
                : "/images/restaurantes/default.jpg";

        Image img = new Image(url, "Foto " + safe(r.getNombre()));
        img.setWidthFull();
        img.setHeight("220px");
        img.getStyle().set("border-radius", "12px");
        img.getStyle().set("object-fit", "cover");

        H2 title = new H2("Carta de " + safe(r.getNombre()));
        title.getStyle().set("margin", "0.6rem 0 0 0");

        Paragraph d1 = new Paragraph("📍 Dirección: " + safe(r.getDireccion()));
        d1.getStyle().set("margin", "0.25rem 0 0 0").set("opacity", "0.85");

        Paragraph d2 = new Paragraph("📞 Teléfono: " + safe(r.getTelefono()) + " · ⏰ " + safe(r.getHorario()));
        d2.getStyle().set("margin", "0.1rem 0 0 0").set("opacity", "0.75");

        header.add(img, title, d1, d2);
        return header;
    }

    private VerticalLayout buildProductoCard(Producto p, NumberFormat eur) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.setWidth("300px");

        card.getStyle()
                .set("border-radius", "14px")
                .set("padding", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("transition", "transform 0.15s ease");

        card.getElement().addEventListener("mouseenter",
                e -> card.getStyle().set("transform", "translateY(-3px)"));
        card.getElement().addEventListener("mouseleave",
                e -> card.getStyle().set("transform", "none"));

        String imgUrl = (p.getImagenUrl() != null && !p.getImagenUrl().isBlank())
                ? p.getImagenUrl()
                : "/images/productos/default.jpg";

        Image prodImg = new Image(imgUrl, "Foto " + safe(p.getNombre()));
        prodImg.setWidthFull();
        prodImg.setHeight("160px");
        prodImg.getStyle().set("border-radius", "12px");
        prodImg.getStyle().set("object-fit", "cover");
        prodImg.getStyle().set("margin-bottom", "0.6rem");

        H3 nombre = new H3(safe(p.getNombre()));
        nombre.getStyle().set("margin", "0");

        Span precio = new Span(p.getPrecio() != null ? eur.format(p.getPrecio()) : "-");
        precio.getStyle().set("font-weight", "700");

        Paragraph desc = new Paragraph(safe(p.getDescripcion()));
        desc.getStyle()
                .set("margin", "0.35rem 0 0 0")
                .set("opacity", "0.8");

        Paragraph ing = new Paragraph("🧾 Ingredientes: " + safe(p.getIngredientes()));
        ing.getStyle()
                .set("margin", "0.25rem 0 0 0")
                .set("opacity", "0.7")
                .set("font-size", "0.9rem");

        Button add = new Button("Añadir", new Icon(VaadinIcon.CART_O));
        add.setWidthFull();
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.getStyle().set("margin-top", "0.7rem");

        add.addClickListener(e -> {
            try {
                carritoService.add(p, restauranteId);
                Notification.show("Añadido al carrito: " + safe(p.getNombre()), 1500,
                        Notification.Position.BOTTOM_CENTER);
                if (onCartChange != null) onCartChange.run();

            } catch (IllegalStateException ex) {
                if ("CARRITO_OTRO_RESTAURANTE".equals(ex.getMessage())) {

                    Dialog d = new Dialog();
                    d.setHeaderTitle("Carrito de otro restaurante");

                    Span msg = new Span("Tu carrito ya tiene productos de otro restaurante. ¿Quieres vaciarlo y cambiar?");
                    msg.getStyle().set("opacity", "0.85");

                    Button cancelar = new Button("Cancelar", ev -> d.close());

                    Button vaciarYAgregar = new Button("Vaciar y añadir", ev -> {
                        carritoService.clear();
                        carritoService.add(p, restauranteId);
                        if (onCartChange != null) onCartChange.run();
                        d.close();
                        Notification.show("Carrito cambiado y producto añadido", 1500,
                                Notification.Position.BOTTOM_CENTER);
                    });
                    vaciarYAgregar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                    HorizontalLayout actions = new HorizontalLayout(cancelar, vaciarYAgregar);
                    actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                    actions.setWidthFull();

                    VerticalLayout body = new VerticalLayout(msg, actions);
                    body.setPadding(false);
                    body.setSpacing(true);

                    d.add(body);
                    d.open();
                }
            }
        });

        card.add(prodImg, nombre, precio, desc, ing, add);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
