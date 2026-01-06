package com.easifood.app.views;

import com.easifood.app.model.Usuario;
import com.easifood.app.service.CarritoService;
import com.easifood.app.service.PedidoService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.YearMonth;

@PageTitle("Pago")
@Route("pago/:restauranteId")
@RolesAllowed("ROLE_CLIENTE")
public class PagoView extends VerticalLayout implements BeforeEnterObserver {

    private final PedidoService pedidoService;
    private final CarritoService carritoService;
    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;

    private Long restauranteId;
    private String direccionEntrega;

    private final Binder<PagoData> binder = new Binder<>(PagoData.class);

    public PagoView(PedidoService pedidoService,
                    CarritoService carritoService,
                    UsuarioService usuarioService,
                    AuthenticationContext authenticationContext) {
        this.pedidoService = pedidoService;
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());
        add(buildForm());
    }

    // ==========================
    // HEADER (mismo estilo que Perfil / Checkout)
    // ==========================
    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Button back = buildBackButton();

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
                .set("letter-spacing", "0.5px")
                .set("cursor", "pointer");

        title.addClickListener(e ->
                UI.getCurrent().navigate("home-cliente")
        );

        Span subtitle = new Span("Pago");
        subtitle.getStyle().set("opacity", "0.7");

        center.add(title, subtitle);

        Component userMenu = buildUserMenu();

        header.add(back, center, userMenu);
        header.expand(center);

        return header;
    }

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

        back.getElement().setProperty("title", "Volver");
        back.getElement().setAttribute("aria-label", "Volver");

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
                UI.getCurrent().navigate("checkout/" + restauranteId);
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
        menu.addItem("Mi perfil", e -> UI.getCurrent().navigate("perfil"));
        menu.addItem("Cerrar sesión", e -> authenticationContext.logout());

        return new HorizontalLayout(avatar);
    }

    private Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return usuarioService.findByCorreo(auth.getName());
    }

    // ==========================
    // FORMULARIO
    // ==========================
    private Component buildForm() {
        VerticalLayout wrap = new VerticalLayout();
        wrap.setMaxWidth("640px");
        wrap.setWidthFull();
        wrap.getStyle().set("margin", "0 auto");

        H2 h = new H2("Datos de la tarjeta");
        Paragraph p = new Paragraph("Introduce los datos para completar el pago.");
        p.getStyle().set("opacity", "0.8");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2)
        );

        TextField titular = new TextField("Titular");
        TextField numero = new TextField("Número de tarjeta");
        numero.setPlaceholder("XXXX XXXX XXXX XXXX");

        TextField caducidad = new TextField("Caducidad (MM/YY)");
        caducidad.setPlaceholder("MM/YY");

        TextField cvv = new TextField("CVV");
        cvv.setPlaceholder("***");

        form.add(titular, numero, caducidad, cvv);

        PagoData data = new PagoData();
        binder.setBean(data);

        binder.forField(titular)
                .asRequired("El titular es obligatorio")
                .withValidator(s -> s != null && s.trim().length() >= 3, "Titular inválido")
                .bind(PagoData::getTitular, PagoData::setTitular);

        binder.forField(numero)
                .asRequired("El número es obligatorio")
                .withValidator(PagoView::validarNumeroTarjeta, "Número inválido")
                .bind(PagoData::getNumeroTarjeta, PagoData::setNumeroTarjeta);

        binder.forField(caducidad)
                .asRequired("La caducidad es obligatoria")
                .withValidator(PagoView::validarCaducidad, "Caducidad inválida")
                .bind(PagoData::getCaducidad, PagoData::setCaducidad);

        binder.forField(cvv)
                .asRequired("El CVV es obligatorio")
                .withValidator(PagoView::validarCvv, "CVV inválido")
                .bind(PagoData::getCvv, PagoData::setCvv);

        // ==========================
        // ACCIONES (Pagar arriba, Cancelar abajo, izquierda)
        // ==========================
        Button pagar = new Button("Pagar", e -> pagar());
        pagar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pagar.getStyle()
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("min-width", "160px");

        Button cancelar = new Button("Cancelar", e -> cancelarPago());

        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.getStyle()
                .set("cursor", "pointer")
                .set("font-size", "0.9rem")
                .set("opacity", "0.8");

        VerticalLayout actions = new VerticalLayout(pagar, cancelar);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setAlignItems(Alignment.START);
        actions.getStyle().set("margin-top", "0.75rem");

        wrap.add(h, p, form, actions);
        return wrap;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        restauranteId = event.getRouteParameters().get("restauranteId").map(Long::valueOf).orElse(null);
        if (restauranteId == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        direccionEntrega = (String) VaadinSession.getCurrent().getAttribute("checkout_direccion");
        if (direccionEntrega == null || direccionEntrega.isBlank()) {
            Notification.show("Faltan datos de entrega. Vuelve al checkout.");
            event.rerouteTo("checkout/" + restauranteId);
        }
    }

    private void pagar() {
        if (!binder.validate().isOk()) return;

        try {
            pedidoService.crearPedido(restauranteId, direccionEntrega);
            VaadinSession.getCurrent().setAttribute("checkout_direccion", null);
            Notification.show("Pedido realizado", 2500, Notification.Position.BOTTOM_CENTER);
            UI.getCurrent().navigate("home-cliente?tab=pedidos");
        } catch (Exception ex) {
            Notification.show("No se pudo completar el pago");
        }
    }

    // ==========================
    // VALIDACIONES
    // ==========================
    private static boolean validarCvv(String s) {
        return s != null && s.matches("\\d{3,4}");
    }

    private static boolean validarCaducidad(String s) {
        if (s == null || !s.matches("\\d{2}/\\d{2}")) return false;
        int mm = Integer.parseInt(s.substring(0, 2));
        int yy = Integer.parseInt(s.substring(3, 5));
        YearMonth exp = YearMonth.of(2000 + yy, mm);
        return mm >= 1 && mm <= 12 && !exp.isBefore(YearMonth.now());
    }

    private static boolean validarNumeroTarjeta(String s) {
        if (s == null) return false;
        String digits = s.replaceAll("\\s+", "");
        return digits.matches("\\d{13,19}") && luhnOk(digits);
    }

    private static boolean luhnOk(String digits) {
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alt) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    public static class PagoData {
        private String titular;
        private String numeroTarjeta;
        private String caducidad;
        private String cvv;

        public String getTitular() { return titular; }
        public void setTitular(String titular) { this.titular = titular; }

        public String getNumeroTarjeta() { return numeroTarjeta; }
        public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }

        public String getCaducidad() { return caducidad; }
        public void setCaducidad(String caducidad) { this.caducidad = caducidad; }

        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
    }

    private void cancelarPago() {
        if (restauranteId == null) {
            UI.getCurrent().navigate("home-cliente");
            return;
        }

        // Vaciar carrito del restaurante actual
        carritoService.clear(restauranteId);

        // Limpiar dirección de checkout
        VaadinSession.getCurrent().setAttribute("checkout_direccion", null);

        // Volver al restaurante
        UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
    }

}
