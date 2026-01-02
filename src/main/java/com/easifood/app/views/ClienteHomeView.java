package com.easifood.app.views;

import com.easifood.app.model.Usuario;
import com.easifood.app.model.Restaurante;
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
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.*;

@PageTitle("Área Cliente")
@Route("home-cliente")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteHomeView extends VerticalLayout implements BeforeEnterObserver {

    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    private Dialog cartaDialog;
    private Long restauranteIdEnDialog;

    public ClienteHomeView(RestauranteService restauranteService,
                           ProductoService productoService,
                           UsuarioService usuarioService,
                           AuthenticationContext authenticationContext) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;

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
        tabsWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        tabsWrapper.setAlignItems(FlexComponent.Alignment.CENTER);

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
        content.removeAll();
        Component c = tabToContent.get(selected);
        if (c != null) content.add(c);
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

        com.vaadin.flow.component.html.Image img =
                new com.vaadin.flow.component.html.Image(url, "Foto " + safe(r.getNombre()));
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
                productoService
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
    // TABs placeholder
    // ==========================
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

    // ==========================
    // “stub” para centrar el título
    // ==========================
    private static class DivStub extends com.vaadin.flow.component.html.Div {
        DivStub(int px) {
            setWidth(px + "px");
            setHeight("1px");
        }
    }
}
