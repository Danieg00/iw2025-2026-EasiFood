package com.easifood.app.views;

import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.model.Cliente;
import com.easifood.app.model.Pedido;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.easifood.app.service.UsuarioService;
import com.easifood.app.service.PedidoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route("home-cliente")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteHomeView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final RestauranteService restauranteService;
    private final ProductoService productoService; // (si no lo usas aquí, puedes quitarlo)
    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;
    private final PedidoService pedidoService;

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    private Tabs tabs;
    private Tab tabExplorar;
    private Tab tabPedidos;

    private static final DateTimeFormatter FECHA_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    public ClienteHomeView(RestauranteService restauranteService,
                           ProductoService productoService,
                           UsuarioService usuarioService,
                           AuthenticationContext authenticationContext,
                           PedidoService pedidoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;
        this.pedidoService = pedidoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        // ==========================
        // TABS
        // ==========================
        tabExplorar = new Tab(getTranslation("cliente.home.tab.explore"));
        tabPedidos = new Tab(getTranslation("cliente.home.tab.orders"));
        tabExplorar.getStyle().set("cursor", "pointer");
        tabPedidos.getStyle().set("cursor", "pointer");

        tabs = new Tabs(tabExplorar, tabPedidos);
        tabs.setWidth("auto");
        tabs.getStyle().set("margin-top", "0.5rem");

        HorizontalLayout tabsWrapper = new HorizontalLayout(tabs);
        tabsWrapper.setWidthFull();
        tabsWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        tabsWrapper.setAlignItems(FlexComponent.Alignment.CENTER);

        tabToContent.put(tabExplorar, buildExplorarContent());
        tabToContent.put(tabPedidos, buildPedidosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabsWrapper, content);
        expand(content);

        // Default (si no viene query param)
        showContent(tabExplorar);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    // ==========================
    // Title dinámico (porque @PageTitle es estático)
    // ==========================
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("cliente.home.pageTitle"));
    }

    // ==========================
    // QUERY PARAM: ?tab=pedidos
    // ==========================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String tab = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("tab", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        if ("pedidos".equalsIgnoreCase(tab)) {
            tabs.setSelectedTab(tabPedidos);
            showContent(tabPedidos);
        } else {
            tabs.setSelectedTab(tabExplorar);
            showContent(tabExplorar);
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

        H1 title = new H1(getTranslation("app.title"));
        title.getStyle()
                .set("margin", "0.5rem 0 0.25rem 0")
                .set("font-weight", "800")
                .set("font-size", "2.4rem")
                .set("letter-spacing", "0.5px");

        Span subtitle = new Span(getTranslation("cliente.home.subtitle"));
        subtitle.getStyle()
                .set("opacity", "0.7")
                .set("font-size", "0.95rem");

        center.add(title, subtitle);

        Component right = buildRightHeader();

        header.add(leftStub, center, right);
        header.expand(center);

        return header;
    }

    private Component buildRightHeader() {
        HorizontalLayout right = new HorizontalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setAlignItems(FlexComponent.Alignment.CENTER);

        ComboBox<Locale> lang = buildLanguageSelector();
        Component userMenu = buildUserMenu();

        right.add(lang, userMenu);
        return right;
    }

    private ComboBox<Locale> buildLanguageSelector() {
        ComboBox<Locale> lang = new ComboBox<>();
        Locale es = new Locale("es");
        Locale en = Locale.ENGLISH;

        lang.setItems(es, en);
        lang.setItemLabelGenerator(l -> l.getLanguage().equals("es")
                ? getTranslation("lang.es")
                : getTranslation("lang.en"));

        Locale current = UI.getCurrent().getLocale();
        if (current == null) current = es;

        // Normaliza a es/en para que no se quede “es_ES” vs “es”
        Locale normalized = current.getLanguage().equalsIgnoreCase("en") ? en : es;
        lang.setValue(normalized);

        lang.setWidth("140px");

        lang.addValueChangeListener(e -> {
            Locale newLocale = e.getValue();
            if (newLocale != null) {
                UI.getCurrent().setLocale(newLocale);
                UI.getCurrent().getPage().reload();
            }
        });

        return lang;
    }

    private Component buildUserMenu() {
        Usuario u = getUsuarioActual();

        String nombre = (u != null && u.getNombre() != null && !u.getNombre().isBlank())
                ? u.getNombre()
                : getTranslation("user.defaultName");

        Avatar avatar = new Avatar(nombre);
        avatar.setWidth("44px");
        avatar.setHeight("44px");
        avatar.getStyle().set("cursor", "pointer");

        if (u != null && u.getImagen() != null && !u.getImagen().isBlank()) {
            avatar.setImage(u.getImagen());
        }

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);

        menu.addItem(getTranslation("user.menu.profile"), e -> UI.getCurrent().navigate("perfil"));
        menu.addItem(getTranslation("user.menu.logout"), e -> authenticationContext.logout());

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
        search.setPlaceholder(getTranslation("cliente.home.search.placeholder"));
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
            Span empty = new Span(getTranslation("cliente.home.restaurants.empty"));
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
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        Image img = new Image(restauranteImageSrc(r),
                getTranslation("cliente.home.restaurant.photoAlt", safe(r.getNombre())));
        img.setWidthFull();
        img.setHeight("160px");
        img.getStyle().set("object-fit", "cover");

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);

        H2 name = new H2(safe(r.getNombre()));
        name.getStyle().set("margin", "0");

        Paragraph direccion = new Paragraph(getTranslation("cliente.home.restaurant.addressPrefix") + " " + safe(r.getDireccion()));
        direccion.getStyle().set("margin", "0.25rem 0").set("opacity", "0.85");

        Paragraph meta = new Paragraph(
                getTranslation("cliente.home.restaurant.phonePrefix") + " " + safe(r.getTelefono())
                        + " · " + getTranslation("cliente.home.restaurant.hoursPrefix") + " " + safe(r.getHorario())
        );
        meta.getStyle().set("margin", "0").set("opacity", "0.75").set("font-size", "0.9rem");

        Button ver = new Button(getTranslation("cliente.home.restaurant.viewMenu"),
                e -> UI.getCurrent().navigate("cliente-restaurante/" + r.getId()));
        ver.setWidthFull();
        ver.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ver.getStyle().set("margin-top", "0.75rem").set("font-weight", "600");

        body.add(name, direccion, meta, ver);
        card.add(img, body);

        return card;
    }

    private String restauranteImageSrc(Restaurante r) {
        String fallback = "/images/restaurantes/default.jpg";

        if (r == null || r.getImagenUrl() == null || r.getImagenUrl().isBlank()) {
            return fallback;
        }

        String raw = r.getImagenUrl().trim().replace("\\", "/");

        if (raw.startsWith("http://") || raw.startsWith("https://")
                || raw.startsWith("/imagenes/") || raw.startsWith("/uploads/")) {
            return raw;
        }

        int idx = raw.indexOf("/uploads/");
        if (idx >= 0) {
            String afterUploads = raw.substring(idx + "/uploads/".length());
            return "/imagenes/" + stripLeadingSlash(afterUploads);
        }

        if (raw.startsWith("uploads/")) {
            String afterUploads = raw.substring("uploads/".length());
            return "/imagenes/" + stripLeadingSlash(afterUploads);
        }

        return "/imagenes/" + stripLeadingSlash(raw);
    }

    private String stripLeadingSlash(String s) {
        if (s == null) return "";
        return s.startsWith("/") ? s.substring(1) : s;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ==========================
    // TAB PEDIDOS
    // ==========================
    private Component buildPedidosContent() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        FlexLayout gallery = new FlexLayout();
        gallery.setWidthFull();
        gallery.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "16px")
                .set("justify-content", "center")
                .set("align-items", "flex-start");

        Usuario u = getUsuarioActual();
        if (!(u instanceof Cliente cliente)) {
            Span s = new Span(getTranslation("cliente.home.orders.notAuthenticated"));
            s.getStyle().set("opacity", "0.7");
            gallery.add(s);
            layout.add(gallery);
            layout.expand(gallery);
            return layout;
        }

        List<Pedido> pedidos = pedidoService.pedidosDeCliente(cliente);

        if (pedidos.isEmpty()) {
            Span empty = new Span(getTranslation("cliente.home.orders.empty"));
            empty.getStyle().set("opacity", "0.7");
            gallery.add(empty);
            layout.add(gallery);
            layout.expand(gallery);
            return layout;
        }

        renderPedidosGallery(gallery, pedidos);

        layout.add(gallery);
        layout.expand(gallery);
        return layout;
    }

    private void renderPedidosGallery(FlexLayout gallery, List<Pedido> pedidos) {
        gallery.removeAll();
        for (Pedido p : pedidos) {
            gallery.add(createPedidoCard(p));
        }
    }

    private Component createPedidoCard(Pedido p) {

        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);

        card.getStyle()
                .set("flex", "1 1 calc(33.333% - 16px)")
                .set("max-width", "calc(33.333% - 16px)")
                .set("min-width", "260px)")
                .set("border-radius", "18px")
                .set("overflow", "hidden")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);
        body.setHeightFull();

        String rest = (p.getRestaurante() != null && p.getRestaurante().getNombre() != null)
                ? p.getRestaurante().getNombre()
                : "-";

        String fechaTxt = (p.getFechaCreacion() != null)
                ? p.getFechaCreacion().format(FECHA_FORMAT)
                : "-";

        String estadoTxt = (p.getEstado() != null && !p.getEstado().isBlank())
                ? p.getEstado()
                : "-";

        // Formato moneda según locale actual
        Locale locale = UI.getCurrent().getLocale();
        if (locale == null) locale = new Locale("es", "ES");
        NumberFormat eur = NumberFormat.getCurrencyInstance(locale);

        H2 title = new H2(getTranslation("cliente.home.orders.cardTitle", p.getId()));
        title.getStyle().set("margin", "0");

        Paragraph restP = new Paragraph(getTranslation("cliente.home.orders.restaurantPrefix") + " " + rest);
        restP.getStyle().set("margin", "0.25rem 0").set("opacity", "0.85");

        Paragraph fechaP = new Paragraph(getTranslation("cliente.home.orders.datePrefix") + " " + fechaTxt);
        fechaP.getStyle().set("margin", "0").set("opacity", "0.75").set("font-size", "0.9rem");

        Paragraph estadoP = new Paragraph(getTranslation("cliente.home.orders.statusPrefix") + " " + estadoTxt);
        estadoP.getStyle().set("margin", "0.25rem 0 0 0").set("opacity", "0.85");

        Paragraph totalP = new Paragraph(getTranslation("cliente.home.orders.totalPrefix") + " " + eur.format(p.getTotal()));
        totalP.getStyle().set("margin", "0.25rem 0 0 0").set("font-weight", "800");

        Button verDetalle = new Button(getTranslation("cliente.home.orders.viewDetail"),
                e -> UI.getCurrent().navigate("pedido/" + p.getId()));
        verDetalle.setWidthFull();
        verDetalle.getStyle()
                .set("margin-top", "0.75rem")
                .set("font-weight", "600")
                .set("cursor", "pointer");
        verDetalle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        body.add(title, restP, fechaP, estadoP, totalP, verDetalle);
        card.add(body);

        return card;
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
