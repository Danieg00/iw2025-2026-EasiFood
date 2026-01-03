package com.easifood.app.views;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.CarritoService;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("Restaurante")
@Route("cliente-restaurante/:id")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteRestauranteView extends VerticalLayout implements BeforeEnterObserver {

    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private final CarritoService carritoService;

    private Long restauranteId;
    private Restaurante restaurante;

    private final NumberFormat eur = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    // ✅ Área scrolleable de la carta
    private VerticalLayout scrollArea;

    // ✅ Barra inferior (oculta hasta que haya items)
    private HorizontalLayout carritoBar;
    private Button btnCarrito;

    // ✅ Bottom-sheet dentro de esta vista (no global)
    private Div backdrop;
    private VerticalLayout sheet;
    private VerticalLayout sheetList;
    private Span sheetTotal;
    private boolean sheetOpen = false;

    // 🔧 “ancho maestro” para barra + sheet
    private static final String FLOAT_WIDTH = "min(720px, calc(100% - 24px))";
    private static final String FLOAT_BOTTOM = "12px";

    public ClienteRestauranteView(RestauranteService restauranteService,
                                  ProductoService productoService,
                                  CarritoService carritoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.carritoService = carritoService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // ✅ Para que overlays (backdrop/sheet/bar) sean “de esta vista”
        getStyle().set("position", "relative");
        getStyle().set("overflow", "hidden");

        add(buildTopBar());

        // ✅ Contenido scrolleable real
        scrollArea = new VerticalLayout();
        scrollArea.setSizeFull();
        scrollArea.setPadding(true);
        scrollArea.setSpacing(true);
        scrollArea.getStyle().set("overflow", "auto");
        scrollArea.getStyle().set("scrollbar-gutter", "stable");
        scrollArea.getStyle().set("padding-bottom", "140px"); // deja hueco real para barra

        add(scrollArea);
        expand(scrollArea);

        // ✅ overlays + barra (se renderizan una vez)
        backdrop = buildBackdrop();
        sheet = buildBottomSheet();
        carritoBar = buildCarritoBar();

        add(backdrop, sheet, carritoBar);
    }

    // ✅ helper para “manita” en todos los botones
    private void makePointer(Button b) {
        if (b != null) b.getStyle().set("cursor", "pointer");
    }

    private HorizontalLayout buildTopBar() {
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setPadding(true);
        top.setAlignItems(Alignment.CENTER);
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button back = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        makePointer(back);

        back.getStyle()
                .set("border-radius", "999px")
                .set("width", "42px")
                .set("height", "42px")
                .set("padding", "0")
                .set("transition", "background-color 160ms ease, box-shadow 160ms ease");

        back.getElement().setProperty("title", "Volver");
        back.getElement().setAttribute("aria-label", "Volver");

        // Hover visual (halo circular)
        back.getElement().addEventListener("mouseenter", e ->
                back.getStyle()
                        .set("background", "var(--lumo-contrast-10pct)")
                        .set("box-shadow", "0 0 0 6px var(--lumo-contrast-10pct)")
        );

        back.getElement().addEventListener("mouseleave", e ->
                back.getStyle()
                        .set("background", "transparent")
                        .set("box-shadow", "none")
        );

        back.addClickListener(e -> UI.getCurrent().navigate("home-cliente"));

        top.add(back);
        return top;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        restauranteId = event.getRouteParameters().get("id").map(Long::valueOf).orElse(null);
        if (restauranteId == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        restaurante = restauranteService.findById(restauranteId);
        if (restaurante == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        renderCarta();
        updateCarritoUI();
    }

    // ==========================
    // Render carta (header + productos)
    // ==========================
    private void renderCarta() {
        scrollArea.removeAll();

        scrollArea.add(buildRestauranteHeader(restaurante));

        List<Producto> productos = productoService.productosDelRestaurante(restaurante);

        H3 subtitulo = new H3("Productos");
        subtitulo.getStyle().set("margin", "0.75rem 0 0 0");
        scrollArea.add(subtitulo);

        if (productos.isEmpty()) {
            Span empty = new Span("Este restaurante todavía no tiene productos.");
            empty.getStyle().set("opacity", "0.7");
            scrollArea.add(empty);
            return;
        }

        FlexLayout grid = new FlexLayout();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("margin-top", "0.5rem");

        for (Producto p : productos) {
            grid.add(buildProductoCard(p));
        }

        scrollArea.add(grid);
    }

    private Component buildRestauranteHeader(Restaurante r) {
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

    private Component buildProductoCard(Producto p) {
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
        desc.getStyle().set("margin", "0.35rem 0 0 0").set("opacity", "0.8");

        Paragraph ing = new Paragraph("🧾 Ingredientes: " + safe(p.getIngredientes()));
        ing.getStyle().set("margin", "0.25rem 0 0 0")
                .set("opacity", "0.7")
                .set("font-size", "0.9rem");

        Button add = new Button("Añadir", new Icon(VaadinIcon.CART_O));
        add.setWidthFull();
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.getStyle().set("margin-top", "0.7rem");
        makePointer(add);

        add.addClickListener(e -> {
            carritoService.add(restauranteId, p);
            Notification.show("Añadido: " + safe(p.getNombre()), 1100,
                    Notification.Position.BOTTOM_CENTER);
            updateCarritoUI();
        });

        card.add(prodImg, nombre, precio, desc, ing, add);
        return card;
    }

    // ==========================
    // Barra inferior (aparece al añadir algo)
    // ==========================
    private HorizontalLayout buildCarritoBar() {
        HorizontalLayout bar = new HorizontalLayout();
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        bar.setPadding(true);
        bar.setSpacing(true);

        bar.getStyle()
                .set("position", "absolute")
                .set("left", "50%")
                .set("bottom", FLOAT_BOTTOM)
                .set("width", FLOAT_WIDTH)
                .set("transform", "translate(-50%, 110%)") // oculto
                .set("z-index", "6")
                .set("border-radius", "14px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 -6px 18px rgba(0,0,0,0.12)")
                .set("backdrop-filter", "blur(6px)")
                .set("opacity", "0")
                .set("pointer-events", "none")
                .set("transition", "transform 220ms ease, opacity 220ms ease");

        Span resumen = new Span("");
        resumen.getStyle().set("font-weight", "800");

        btnCarrito = new Button("Ver carrito", new Icon(VaadinIcon.CHEVRON_UP));
        btnCarrito.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCarrito.getStyle().set("font-weight", "700");
        makePointer(btnCarrito);
        btnCarrito.addClickListener(e -> openSheet());

        bar.add(resumen, btnCarrito);
        return bar;
    }

    // ==========================
    // Backdrop + BottomSheet (dentro de esta vista)
    // ==========================
    private Div buildBackdrop() {
        Div b = new Div();
        b.getStyle()
                .set("position", "absolute")
                .set("inset", "0")
                .set("background", "rgba(0,0,0,0.45)")
                .set("z-index", "9")
                .set("opacity", "0")
                .set("pointer-events", "none")
                .set("transition", "opacity 180ms ease");
        b.addClickListener(e -> closeSheet());
        return b;
    }

    private VerticalLayout buildBottomSheet() {
        VerticalLayout s = new VerticalLayout();
        s.setPadding(false);
        s.setSpacing(false);

        // 🔧 MISMO ancho y centrado que la barra
        s.getStyle()
                .set("position", "absolute")
                .set("left", "50%")
                .set("bottom", FLOAT_BOTTOM) // nace desde la misma cota
                .set("width", FLOAT_WIDTH)
                .set("transform", "translate(-50%, 110%)") // oculto
                .set("z-index", "10")
                .set("border-radius", "18px 18px 0 0")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "0 -18px 40px rgba(0,0,0,0.22)")
                .set("max-height", "70vh")
                .set("overflow", "hidden")
                .set("opacity", "0")
                .set("pointer-events", "none")
                .set("transition", "transform 240ms ease, opacity 240ms ease");

        Div handle = new Div();
        handle.getStyle()
                .set("height", "5px")
                .set("width", "56px")
                .set("border-radius", "999px")
                .set("background", "var(--lumo-contrast-20pct)")
                .set("margin", "10px auto 6px auto");

        Button close = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> closeSheet());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        close.getStyle()
                .set("margin-left", "auto")
                .set("border-radius", "999px")
                .set("width", "42px")
                .set("height", "42px")
                .set("padding", "0")
                .set("cursor", "pointer")
                .set("transition", "background-color 160ms ease, box-shadow 160ms ease");

        close.getElement().setProperty("title", "Cerrar");
        close.getElement().setAttribute("aria-label", "Cerrar");

        // Hover visual (halo circular)
        close.getElement().addEventListener("mouseenter", e ->
                close.getStyle()
                        .set("background", "var(--lumo-contrast-10pct)")
                        .set("box-shadow", "0 0 0 6px var(--lumo-contrast-10pct)")
        );

        close.getElement().addEventListener("mouseleave", e ->
                close.getStyle()
                        .set("background", "transparent")
                        .set("box-shadow", "none")
        );
        makePointer(close);

        H3 title = new H3("Tu carrito");
        title.getStyle().set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(title, close);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("padding", "8px 14px 10px 14px");

        sheetList = new VerticalLayout();
        sheetList.setPadding(false);
        sheetList.setSpacing(true);
        sheetList.getStyle()
                .set("padding", "0 14px 12px 14px")
                .set("overflow", "auto")
                .set("max-height", "calc(70vh - 130px)"); // deja espacio a header+footer

        sheetTotal = new Span("");
        sheetTotal.getStyle().set("font-weight", "900").set("font-size", "1.05rem");

        Button vaciar = new Button("Vaciar", e -> {
            carritoService.clear(restauranteId);
            updateCarritoUI();
            closeSheet();
        });
        vaciar.addThemeVariants(ButtonVariant.LUMO_ERROR);
        makePointer(vaciar);

        Button continuar = new Button("Continuar", e ->
                UI.getCurrent().navigate("checkout/" + restauranteId)
        );
        continuar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        makePointer(continuar);

        HorizontalLayout footerActions = new HorizontalLayout(vaciar, continuar);
        footerActions.setSpacing(true);

        HorizontalLayout footer = new HorizontalLayout(sheetTotal, footerActions);
        footer.setWidthFull();
        footer.setAlignItems(Alignment.CENTER);
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.getStyle().set("padding", "12px 14px 14px 14px");

        s.add(handle, header, new Hr(), sheetList, new Hr(), footer);
        return s;
    }

    private void openSheet() {
        sheetOpen = true;
        renderSheetItems();

        backdrop.getStyle().set("opacity", "1").set("pointer-events", "auto");
        sheet.getStyle()
                .set("transform", "translate(-50%, 0)")
                .set("opacity", "1")
                .set("pointer-events", "auto");
    }

    private void closeSheet() {
        sheetOpen = false;

        backdrop.getStyle().set("opacity", "0").set("pointer-events", "none");
        sheet.getStyle()
                .set("transform", "translate(-50%, 110%)")
                .set("opacity", "0")
                .set("pointer-events", "none");
    }

    // ==========================
    // Actualización UI carrito (barra + sheet)
    // ==========================
    private void updateCarritoUI() {
        int n = carritoService.totalUnidades(restauranteId);
        BigDecimal total = carritoService.totalPrecio(restauranteId);

        Span resumen = (Span) carritoBar.getComponentAt(0);

        if (n <= 0) {
            carritoBar.getStyle()
                    .set("opacity", "0")
                    .set("transform", "translate(-50%, 110%)")
                    .set("pointer-events", "none");
            resumen.setText("");

            if (sheetOpen) closeSheet();
            return;
        }

        resumen.setText(n + " item" + (n == 1 ? "" : "s") + " · " + eur.format(total));

        carritoBar.getStyle()
                .set("opacity", "1")
                .set("transform", "translate(-50%, 0)")
                .set("pointer-events", "auto");

        if (sheetOpen) renderSheetItems();
    }

    private void renderSheetItems() {
        sheetList.removeAll();

        List<CarritoService.Item> items = carritoService.items(restauranteId);
        if (items.isEmpty()) {
            Span empty = new Span("Tu carrito está vacío.");
            empty.getStyle().set("opacity", "0.7");
            sheetList.add(empty);
            sheetTotal.setText("Total: " + eur.format(BigDecimal.ZERO));
            return;
        }

        for (CarritoService.Item it : items) {
            Producto p = it.getProducto();

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);
            row.getStyle()
                    .set("padding", "10px")
                    .set("border-radius", "12px")
                    .set("background", "var(--lumo-contrast-5pct)");

            Span name = new Span(safe(p.getNombre()));
            name.getStyle().set("font-weight", "700");

            Button minus = new Button(new Icon(VaadinIcon.MINUS));
            Button plus = new Button(new Icon(VaadinIcon.PLUS));
            Button remove = new Button(new Icon(VaadinIcon.TRASH));

            makePointer(minus);
            makePointer(plus);
            makePointer(remove);

            Span qty = new Span(String.valueOf(it.getCantidad()));
            qty.getStyle().set("min-width", "28px").set("text-align", "center").set("font-weight", "800");

            HorizontalLayout qtyBox = new HorizontalLayout(minus, qty, plus);
            qtyBox.setAlignItems(Alignment.CENTER);

            BigDecimal precio = p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO;
            Span subtotal = new Span(eur.format(precio.multiply(BigDecimal.valueOf(it.getCantidad()))));
            subtotal.getStyle().set("font-weight", "800");

            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);

            minus.addClickListener(e -> {
                carritoService.dec(restauranteId, p);
                updateCarritoUI();
            });

            plus.addClickListener(e -> {
                carritoService.add(restauranteId, p);
                updateCarritoUI();
            });

            remove.addClickListener(e -> {
                carritoService.remove(restauranteId, p.getId());
                updateCarritoUI();
            });

            row.add(name);
            row.expand(name);
            row.add(qtyBox, subtotal, remove);

            sheetList.add(row);
        }

        sheetTotal.setText("Total: " + eur.format(carritoService.totalPrecio(restauranteId)));
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
