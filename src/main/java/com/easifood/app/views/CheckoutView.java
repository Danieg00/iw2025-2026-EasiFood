package com.easifood.app.views;

import com.easifood.app.model.Producto;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.CarritoService;
import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Route("checkout/:restauranteId")
@RolesAllowed("ROLE_CLIENTE")
public class CheckoutView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;

    private Long restauranteId;

    private final Binder<CheckoutData> binder = new Binder<>(CheckoutData.class);

    private final VerticalLayout resumenBox = new VerticalLayout();
    private final Span totalSpan = new Span();

    public CheckoutView(CarritoService carritoService,
                        UsuarioService usuarioService,
                        AuthenticationContext authenticationContext) {
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());
        add(buildLayout());
    }

    // Título dinámico (i18n)
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("checkout.pageTitle"));
    }

    private Locale uiLocale() {
        Locale l = UI.getCurrent().getLocale();
        return (l != null) ? l : new Locale("es", "ES");
    }

    private NumberFormat money() {
        return NumberFormat.getCurrencyInstance(uiLocale());
    }

    // ==========================
    // HEADER (con botón volver estilo PerfilView)
    // ==========================
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        // Izquierda: botón volver (mismo estilo que PerfilView)
        Button back = buildBackButton();

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
                .set("letter-spacing", "0.5px")
                .set("cursor", "pointer");

        title.addClickListener(e -> UI.getCurrent().navigate("home-cliente"));

        Span subtitle = new Span(getTranslation("checkout.subtitle"));
        subtitle.getStyle()
                .set("opacity", "0.7")
                .set("font-size", "0.95rem");

        center.add(title, subtitle);

        Component userMenu = buildUserMenu();

        header.add(back, center, userMenu);
        header.expand(center);

        return header;
    }

    /**
     * Botón volver idéntico al de PerfilView:
     */
    private Button buildBackButton() {
        Button back = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        back.getStyle()
                .set("border-radius", "999px")
                .set("width", "42px")
                .set("height", "42px")
                .set("padding", "0")
                .set("cursor", "pointer")
                .set("transition", "background-color 160ms ease, box-shadow 160ms ease");

        back.getElement().setProperty("title", getTranslation("common.back"));
        back.getElement().setAttribute("aria-label", getTranslation("common.back"));

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

        back.addClickListener(e -> {
            if (restauranteId != null) {
                UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
            } else {
                UI.getCurrent().navigate("home-cliente");
            }
        });

        return back;
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

    // ==========================
    // LAYOUT PRINCIPAL
    // ==========================
    private Component buildLayout() {
        HorizontalLayout root = new HorizontalLayout();
        root.setWidthFull();
        root.setSpacing(true);
        root.getStyle().set("flex-wrap", "wrap");

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setWidth("min(760px, 100%)");

        VerticalLayout right = new VerticalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setWidth("360px");
        right.getStyle().set("min-width", "280px");

        left.add(buildForm());
        right.add(buildResumen());

        root.add(left, right);
        root.expand(left);

        return root;
    }

    private Component buildForm() {
        VerticalLayout wrap = new VerticalLayout();
        wrap.setPadding(false);
        wrap.setSpacing(true);

        H3 h = new H3(getTranslation("checkout.delivery.title"));
        h.getStyle().set("margin", "0");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextArea direccionEntrega = new TextArea(getTranslation("checkout.delivery.address.label"));
        direccionEntrega.setWidthFull();
        direccionEntrega.setMinHeight("120px");
        direccionEntrega.setPlaceholder(getTranslation("checkout.delivery.address.placeholder"));

        form.add(direccionEntrega);

        CheckoutData data = new CheckoutData();
        binder.setBean(data);

        binder.forField(direccionEntrega)
                .asRequired(getTranslation("checkout.validation.address.required"))
                .withValidator(s -> s != null && s.trim().length() >= 10,
                        getTranslation("checkout.validation.address.tooShort"))
                .withValidator(s -> s == null || s.length() <= 300,
                        getTranslation("checkout.validation.address.maxLength"))
                .withValidator(s -> {
                    if (s == null) return false;
                    String t = s.trim();
                    return t.chars().anyMatch(Character::isLetter);
                }, getTranslation("checkout.validation.address.mustContainLetters"))
                .bind(CheckoutData::getDireccionEntrega, CheckoutData::setDireccionEntrega);

        // ==========================
        // ACCIONES
        // ==========================
        Button continuar = new Button(getTranslation("common.next"), e -> continuarPago());
        continuar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        continuar.getStyle()
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("min-width", "160px");

        Button cancelar = new Button(getTranslation("common.cancel"), e -> cancelarCheckout());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.getStyle()
                .set("cursor", "pointer")
                .set("font-size", "0.9rem")
                .set("opacity", "0.8");

        VerticalLayout actions = new VerticalLayout(continuar, cancelar);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setAlignItems(Alignment.START);
        actions.getStyle().set("margin-top", "0.5rem");

        wrap.add(h, form, actions);
        return wrap;
    }

    private Component buildResumen() {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(true);
        box.setSpacing(true);
        box.setWidthFull();

        box.getStyle()
                .set("border-radius", "18px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 title = new H3(getTranslation("checkout.summary.title"));
        title.getStyle().set("margin", "0");

        resumenBox.setPadding(false);
        resumenBox.setSpacing(false);
        resumenBox.setWidthFull();

        totalSpan.getStyle().set("font-weight", "900").set("font-size", "1.1rem");

        box.add(title, resumenBox, new Hr(), totalSpan);
        return box;
    }

    private void renderResumen() {
        resumenBox.removeAll();

        List<CarritoService.Item> items = carritoService.items(restauranteId);

        if (items.isEmpty()) {
            Span empty = new Span(getTranslation("checkout.cart.empty"));
            empty.getStyle().set("opacity", "0.7");
            resumenBox.add(empty);
            totalSpan.setText(getTranslation("common.total") + ": " + money().format(BigDecimal.ZERO));
            return;
        }

        for (CarritoService.Item it : items) {
            Producto p = it.getProducto();

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span left = new Span(it.getCantidad() + " × " + safe(p.getNombre()));
            left.getStyle().set("opacity", "0.9");

            BigDecimal precio = (p.getPrecio() != null) ? p.getPrecio() : BigDecimal.ZERO;
            BigDecimal line = precio.multiply(BigDecimal.valueOf(it.getCantidad()));

            Span right = new Span(money().format(line));
            right.getStyle().set("font-weight", "700");

            row.add(left, right);
            resumenBox.add(row);
        }

        totalSpan.setText(getTranslation("common.total") + ": " + money().format(carritoService.totalPrecio(restauranteId)));
    }

    private void continuarPago() {
        if (!binder.validate().isOk()) return;

        if (carritoService.items(restauranteId).isEmpty()) {
            Notification.show(getTranslation("checkout.cart.empty"));
            UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
            return;
        }

        VaadinSession.getCurrent().setAttribute("checkout_direccion", binder.getBean().getDireccionEntrega());
        UI.getCurrent().navigate("pago/" + restauranteId);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        restauranteId = event.getRouteParameters().get("restauranteId").map(Long::valueOf).orElse(null);
        if (restauranteId == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        if (carritoService.items(restauranteId).isEmpty()) {
            event.rerouteTo("cliente-restaurante/" + restauranteId);
            return;
        }

        renderResumen();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    public static class CheckoutData {
        private String direccionEntrega;

        public String getDireccionEntrega() { return direccionEntrega; }
        public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    }

    private void cancelarCheckout() {
        if (restauranteId == null) {
            UI.getCurrent().navigate("home-cliente");
            return;
        }

        carritoService.clear(restauranteId);
        VaadinSession.getCurrent().setAttribute("checkout_direccion", null);
        UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
    }
}
