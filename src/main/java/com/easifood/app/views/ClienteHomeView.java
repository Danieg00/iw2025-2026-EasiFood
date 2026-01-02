package com.easifood.app.views;

import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.CarritoService;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.easifood.app.service.UsuarioService;
import com.easifood.app.views.components.ClienteRestauranteContent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

@PageTitle("Área Cliente")
@Route("home-cliente")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteHomeView extends VerticalLayout implements BeforeEnterObserver {

    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;
    private final CarritoService carritoService;

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    private Dialog cartaDialog;
    private Long restauranteIdEnDialog;

    // Tabs / UI carrito
    private Tab tabCarrito;
    private Span carritoBadge;
    private Component carritoContent;

    public ClienteHomeView(RestauranteService restauranteService,
                           ProductoService productoService,
                           UsuarioService usuarioService,
                           AuthenticationContext authenticationContext,
                           CarritoService carritoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;
        this.carritoService = carritoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        // ==========================
        // TABS
        // ==========================
        Tab tabExplorar = new Tab("Explorar");

        carritoBadge = new Span();
        carritoBadge.getStyle()
                .set("margin-left", "8px")
                .set("padding", "2px 8px")
                .set("border-radius", "999px")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("font-size", "0.85rem")
                .set("font-weight", "700");

        tabCarrito = new Tab(new Span("Carrito"), carritoBadge);

        Tab tabPedidos = new Tab("Mis pedidos");

        Tabs tabs = new Tabs(tabExplorar, tabCarrito, tabPedidos);
        tabs.setWidth("auto");
        tabs.getStyle().set("margin-top", "0.5rem");

        HorizontalLayout tabsWrapper = new HorizontalLayout(tabs);
        tabsWrapper.setWidthFull();
        tabsWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        tabsWrapper.setAlignItems(FlexComponent.Alignment.CENTER);

        tabToContent.put(tabExplorar, buildExplorarContent());

        carritoContent = buildCarritoContent();
        tabToContent.put(tabCarrito, carritoContent);

        tabToContent.put(tabPedidos, buildPedidosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabsWrapper, content);
        expand(content);

        updateCarritoBadge();
        showContent(tabExplorar);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String value = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("restaurante", Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(null);

        if (value == null) return;

        try {
            Long id = Long.valueOf(value);
            if (cartaDialog == null || !cartaDialog.isOpened()
                    || restauranteIdEnDialog == null || !restauranteIdEnDialog.equals(id)) {
                openCartaDialog(id, false);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    // ==========================
    // HEADER
    // ==========================
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        DivStub leftStub = new DivStub(48);

        VerticalLayout center = new VerticalLayout();
        center.setPadding(false);
        center.setSpacing(false);
        center.setAlignItems(FlexComponent.Alignment.CENTER);
        center.setWidthFull();

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

        center.add(title, subtitle);

        Component userMenu = buildUserMenu();

        header.add(leftStub, center, userMenu);
        header.expand(center);

        return header;
    }

    private Component buildUserMenu() {
        Usuario u = getUsuarioActual();

        String nombre = (u != null && u.getNombre() != null && !u.getNombre().isBlank())
                ? u.getNombre()
                : "Usuario";

        Avatar avatar = new Avatar(nombre);
        avatar.setWidth("44px");
        avatar.setHeight("44px");
        avatar.getStyle().set("cursor", "pointer");

        if (u != null && u.getImagen() != null && !u.getImagen().isBlank()) {
            avatar.setImage(u.getImagen());
        }

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);

        menu.addItem("Mi perfil", e -> {
            closeDialogIfOpen();
            clearRestauranteQueryParam();
            UI.getCurrent().navigate("perfil");
        });

        menu.addItem("Cerrar sesión", e -> {
            closeDialogIfOpen();
            clearRestauranteQueryParam();
            authenticationContext.logout();
        });

        HorizontalLayout wrap = new HorizontalLayout(avatar);
        wrap.setPadding(false);
        wrap.setSpacing(false);
        wrap.setAlignItems(FlexComponent.Alignment.CENTER);

        return wrap;
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return usuarioService.findByCorreo(auth.getName());
    }

    private void closeDialogIfOpen() {
        if (cartaDialog != null && cartaDialog.isOpened()) {
            cartaDialog.close();
        }
    }

    private void showContent(Tab selected) {
        // ✅ Si entro al Carrito, SIEMPRE reconstruyo el contenido (para no usar el viejo “vacío”)
        if (selected == tabCarrito) {
            carritoContent = buildCarritoContent();
            tabToContent.put(tabCarrito, carritoContent);
        }

        content.removeAll();
        Component c = tabToContent.get(selected);
        if (c != null) content.add(c);
    }

    // ==========================
    // BADGE CARRITO
    // ==========================
    private void updateCarritoBadge() {
        int n = carritoService.totalUnidades();
        carritoBadge.setText(String.valueOf(n));
        carritoBadge.getStyle().set("opacity", n > 0 ? "1" : "0.6");
    }

    private void refreshCarritoViewIfVisible() {
        Component current = content.getChildren().findFirst().orElse(null);
        if (current == carritoContent) {
            carritoContent = buildCarritoContent();
            tabToContent.put(tabCarrito, carritoContent);
            showContent(tabCarrito);
        }
    }

    private void refreshCarritoCache() {
        carritoContent = buildCarritoContent();
        tabToContent.put(tabCarrito, carritoContent);
    }


    // ==========================
    // EXPLORAR
    // ==========================
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
        searchWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        FlexLayout gallery = new FlexLayout();
        gallery.setWidthFull();
        gallery.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("justify-content", "center");

        List<Restaurante> all = restauranteService.findAll();
        List<Restaurante> filtered = new ArrayList<>(all);

        renderGallery(gallery, filtered);

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

        card.getStyle()
                .set("flex", "1 1 calc(33.333% - 16px)")
                .set("max-width", "calc(33.333% - 16px)")
                .set("min-width", "260px")
                .set("border-radius", "18px")
                .set("overflow", "hidden")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("transition", "transform 0.12s ease-in-out");

        String url = (r.getImagenUrl() != null && !r.getImagenUrl().isBlank())
                ? r.getImagenUrl()
                : "/images/restaurantes/default.jpg";

        Image img = new Image(url, "Foto " + safe(r.getNombre()));
        img.setWidthFull();
        img.setHeight("160px");
        img.getStyle().set("object-fit", "cover");

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);

        H2 name = new H2(safe(r.getNombre()));
        name.getStyle().set("margin", "0");

        Paragraph direccion = new Paragraph("📍 " + safe(r.getDireccion()));
        direccion.getStyle().set("margin", "0.25rem 0");
        direccion.getStyle().set("opacity", "0.85");

        Paragraph meta = new Paragraph("📞 " + safe(r.getTelefono()) + " · ⏰ " + safe(r.getHorario()));
        meta.getStyle().set("margin", "0");
        meta.getStyle().set("opacity", "0.75");
        meta.getStyle().set("font-size", "0.9rem");

        Button ver = new Button("Ver carta", e -> openCartaDialog(r.getId(), true));
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

    // ==========================
    // DIALOG CARTA
    // ==========================
    private void openCartaDialog(Long restauranteId, boolean updateUrl) {

        if (cartaDialog != null && cartaDialog.isOpened()
                && restauranteIdEnDialog != null
                && restauranteIdEnDialog.equals(restauranteId)) {
            return;
        }

        if (cartaDialog != null && cartaDialog.isOpened()) {
            cartaDialog.close();
        }

        restauranteIdEnDialog = restauranteId;

        cartaDialog = new Dialog();
        cartaDialog.setModal(true);
        cartaDialog.setDraggable(false);
        cartaDialog.setResizable(false);

        cartaDialog.setWidth("min(1000px, 92vw)");
        cartaDialog.setHeight("min(720px, 90vh)");

        Button close = new Button(new Icon(VaadinIcon.CLOSE), e -> cartaDialog.close());

        HorizontalLayout header = new HorizontalLayout(close);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Component carta = new ClienteRestauranteContent(
                restauranteId,
                restauranteService,
                productoService,
                carritoService,
                () -> {
                    updateCarritoBadge();
                    refreshCarritoCache();
                }

        );

        VerticalLayout wrapper = new VerticalLayout(header, carta);
        wrapper.setSizeFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);

        cartaDialog.add(wrapper);

        cartaDialog.addDialogCloseActionListener(e -> clearRestauranteQueryParam());

        if (updateUrl) {
            UI.getCurrent().navigate(
                    "home-cliente",
                    QueryParameters.simple(Map.of("restaurante", String.valueOf(restauranteId)))
            );
        }

        cartaDialog.open();
    }

    private void clearRestauranteQueryParam() {
        restauranteIdEnDialog = null;

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.navigate("home-cliente");
        }
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ==========================
    // TAB CARRITO (REAL)
    // ==========================
    private Component buildCarritoContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        Long restId = carritoService.getRestauranteId();
        if (restId != null) {
            Span rest = new Span("Restaurante del carrito: " + restId);
            rest.getStyle().set("opacity", "0.7");
            layout.add(rest);
        }
        H1 title = new H1("Carrito");
        title.getStyle().set("margin", "0.25rem 0 0.5rem 0");
        layout.add(title);

        List<CarritoService.Item> items = carritoService.items();

        if (items.isEmpty()) {
            Span empty = new Span("Tu carrito está vacío.");
            empty.getStyle().set("opacity", "0.7");
            layout.add(empty);
            return layout;
        }

        NumberFormat eur = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        VerticalLayout list = new VerticalLayout();
        list.setWidthFull();
        list.setPadding(false);
        list.setSpacing(false);

        for (CarritoService.Item it : items) {
            var p = it.getProducto();

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.getStyle()
                    .set("padding", "10px")
                    .set("border-radius", "12px")
                    .set("background", "var(--lumo-base-color)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("margin-bottom", "10px");

            VerticalLayout info = new VerticalLayout();
            info.setPadding(false);
            info.setSpacing(false);

            H3 name = new H3(safe(p.getNombre()));
            name.getStyle().set("margin", "0");

            Span unit = new Span("Unidad: " + (p.getPrecio() != null ? eur.format(p.getPrecio()) : "-"));
            unit.getStyle().set("opacity", "0.75");

            info.add(name, unit);

            Button minus = new Button(new Icon(VaadinIcon.MINUS));
            Button plus = new Button(new Icon(VaadinIcon.PLUS));

            Span qty = new Span(String.valueOf(it.getCantidad()));
            qty.getStyle()
                    .set("min-width", "28px")
                    .set("text-align", "center")
                    .set("font-weight", "700");

            HorizontalLayout qtyBox = new HorizontalLayout(minus, qty, plus);
            qtyBox.setAlignItems(FlexComponent.Alignment.CENTER);
            qtyBox.setSpacing(true);

            BigDecimal precio = p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO;
            Span subtotal = new Span("Subtotal: " + eur.format(precio.multiply(BigDecimal.valueOf(it.getCantidad()))));
            subtotal.getStyle().set("font-weight", "700");

            Button remove = new Button(new Icon(VaadinIcon.TRASH));
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR);

            minus.addClickListener(e -> {
                carritoService.dec(p);
                updateCarritoBadge();
                refreshCarritoViewIfVisible();
            });

            plus.addClickListener(e -> {
                carritoService.add(p);
                updateCarritoBadge();
                refreshCarritoViewIfVisible();
            });

            remove.addClickListener(e -> {
                carritoService.remove(p.getId());
                updateCarritoBadge();
                refreshCarritoViewIfVisible();
            });

            row.add(info);
            row.expand(info);
            row.add(qtyBox, subtotal, remove);

            list.add(row);
        }

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span total = new Span("Total: " + eur.format(carritoService.totalPrecio()));
        total.getStyle().set("font-weight", "800").set("font-size", "1.1rem");

        Button clear = new Button("Vaciar carrito", new Icon(VaadinIcon.CLOSE_SMALL));
        clear.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button pagar = new Button("Continuar (pendiente pago)");
        pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        clear.addClickListener(e -> {
            carritoService.clear();
            updateCarritoBadge();
            refreshCarritoViewIfVisible();
        });

        footer.add(total, new HorizontalLayout(clear, pagar));

        layout.add(list, footer);
        return layout;
    }

    // ==========================
    // TAB PEDIDOS (placeholder)
    // ==========================
    private Component buildPedidosContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.add(new H1("Mis pedidos"));
        layout.add(new Span("Pendiente: listar pedidos del cliente, modificar/cancelar y generar ticket."));
        return layout;
    }

    // ==========================
    // “stub” para centrar el título
    // ==========================
    private static class DivStub extends Div {
        DivStub(int px) {
            setWidth(px + "px");
            setHeight("1px");
        }
    }
}
