package com.easifood.app.views.components;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClienteRestauranteContent extends VerticalLayout {

    public ClienteRestauranteContent(Long restauranteId,
                                     RestauranteService restauranteService,
                                     ProductoService productoService) {

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Restaurante r = restauranteService.findById(restauranteId);
        if (r == null) {
            add(new Paragraph("Restaurante no encontrado."));
            return;
        }

        // ✅ 1) HEADER SIEMPRE
        add(buildRestauranteHeader(r));

        // ✅ 2) PRODUCTOS (si hay)
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

        for (Producto p : productos) {
            add(buildProductoCard(p, eur));
        }
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

        card.getStyle()
                .set("border-radius", "14px")
                .set("padding", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        H3 nombre = new H3(safe(p.getNombre()));
        nombre.getStyle().set("margin", "0");

        Span precio = new Span(p.getPrecio() != null ? eur.format(p.getPrecio()) : "-");
        precio.getStyle().set("font-weight", "700");

        Paragraph desc = new Paragraph(safe(p.getDescripcion()));
        desc.getStyle().set("margin", "0.35rem 0 0 0").set("opacity", "0.8");

        card.add(nombre, precio, desc);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
