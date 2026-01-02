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
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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

        // ==========================
        // HEADER RESTAURANTE (IGUAL)
        // ==========================
        add(buildRestauranteHeader(r));

        // ==========================
        // PRODUCTOS
        // ==========================
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

        // 👉 GRID FLEXIBLE (CLAVE)
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

        // 👉 CLAVE: ancho fijo y no estirarse
        card.setWidth("300px");

        card.getStyle()
                .set("border-radius", "14px")
                .set("padding", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("transition", "transform 0.15s ease");

        // Hover suave
        card.getElement().addEventListener("mouseenter",
                e -> card.getStyle().set("transform", "translateY(-3px)"));
        card.getElement().addEventListener("mouseleave",
                e -> card.getStyle().set("transform", "none"));

        // ==========================
        // FOTO PRODUCTO
        // ==========================
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

        card.add(prodImg, nombre, precio, desc, ing);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
