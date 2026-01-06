package com.easifood.app.views;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.PedidoService;
import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route("pedido/:id")
@RolesAllowed("ROLE_CLIENTE")
public class PedidoDetalleView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    private Long pedidoId;
    private Pedido pedido;

    private final Binder<FormData> binder = new Binder<>(FormData.class);

    // UI
    private final VerticalLayout page = new VerticalLayout();
    private final VerticalLayout infoCard = new VerticalLayout();

    private TextArea direccionEntrega;

    private Button guardar;
    private Button cancelarPedido;
    private Button volver;

    public PedidoDetalleView(PedidoService pedidoService, UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        page.setWidthFull();
        page.setMaxWidth("980px");
        page.getStyle().set("margin", "0 auto");
        page.setPadding(false);
        page.setSpacing(true);

        // Botones (se crean aquí para poder usarlos en el header)
        volver = buildBackButton();
        guardar = new Button(getTranslation("order.detail.actions.save"));
        cancelarPedido = new Button(getTranslation("order.detail.actions.cancelOrder"));

        page.add(buildHeader());

        // Card info
        infoCard.setPadding(true);
        infoCard.setSpacing(true);
        infoCard.setWidthFull();
        infoCard.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "18px")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        // Form
        direccionEntrega = new TextArea(getTranslation("order.detail.deliveryAddress"));
        direccionEntrega.setWidthFull();
        direccionEntrega.setMinHeight("120px");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.add(direccionEntrega);

        binder.setBean(new FormData());

        // ✅ Validación igual que en Checkout (mínimo 10)
        binder.forField(direccionEntrega)
                .asRequired(getTranslation("checkout.validation.address.required"))
                .withValidator(s -> s != null && s.trim().length() >= 10,
                        getTranslation("checkout.validation.address.tooShort"))
                .withValidator(s -> s == null || s.length() <= 300,
                        getTranslation("checkout.validation.address.max"))
                .withValidator(s -> {
                    if (s == null) return false;
                    String t = s.trim();
                    return t.chars().anyMatch(Character::isLetter);
                }, getTranslation("checkout.validation.address.mustHaveLetters"))
                .bind(FormData::getDireccionEntrega, FormData::setDireccionEntrega);

        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("font-weight", "700").set("cursor", "pointer").set("min-width", "180px");
        guardar.addClickListener(e -> onGuardar());

        cancelarPedido.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelarPedido.getStyle()
                .set("cursor", "pointer")
                .set("font-size", "0.9rem")
                .set("opacity", "0.85");
        cancelarPedido.addClickListener(e -> onCancelarPedido());

        // Acciones: izquierda
        VerticalLayout actions = new VerticalLayout(guardar, cancelarPedido);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setAlignItems(Alignment.START);
        actions.getStyle().set("margin-top", "0.75rem");

        page.add(infoCard, form, actions);
        add(page);
    }

    // ==========================
    // Page title dinámico
    // ==========================
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("order.detail.pageTitle"));
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

        title.addClickListener(e -> UI.getCurrent().navigate("home-cliente"));

        Span subtitle = new Span(getTranslation("order.detail.subtitle"));
        subtitle.getStyle().set("opacity", "0.7");

        center.add(title, subtitle);

        DivStub rightStub = new DivStub(44);

        header.add(volver, center, rightStub);
        header.expand(center);

        return header;
    }

    // ==========================
    // BACK BUTTON (estilo PagoView)
    // ==========================
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

        // ✅ Vuelve a Mis pedidos
        back.addClickListener(e -> UI.getCurrent().navigate("home-cliente?tab=pedidos"));

        return back;
    }

    // ==========================
    // LOAD
    // ==========================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        pedidoId = event.getRouteParameters().get("id").map(Long::valueOf).orElse(null);
        if (pedidoId == null) {
            event.rerouteTo("home-cliente?tab=pedidos");
            return;
        }

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) {
            event.rerouteTo("home-cliente");
            return;
        }

        pedido = pedidoService.pedidoDeCliente(pedidoId, cliente).orElse(null);
        if (pedido == null) {
            Notification.show(getTranslation("order.detail.notFound"), 2200, Notification.Position.TOP_CENTER);
            event.rerouteTo("home-cliente?tab=pedidos");
            return;
        }

        renderPedido(pedido);
    }

    private void renderPedido(Pedido p) {
        infoCard.removeAll();

        Locale locale = UI.getCurrent() != null && UI.getCurrent().getLocale() != null
                ? UI.getCurrent().getLocale()
                : Locale.getDefault();

        // Fecha: patrón distinto si es EN
        String datePattern = "en".equalsIgnoreCase(locale.getLanguage())
                ? "MM/dd/yyyy · HH:mm"
                : "dd/MM/yyyy · HH:mm";

        DateTimeFormatter fechaFormat = DateTimeFormatter.ofPattern(datePattern, locale);
        NumberFormat money = NumberFormat.getCurrencyInstance(locale);

        String rest = (p.getRestaurante() != null && p.getRestaurante().getNombre() != null)
                ? p.getRestaurante().getNombre()
                : "-";

        String fecha = (p.getFechaCreacion() != null)
                ? p.getFechaCreacion().format(fechaFormat)
                : "-";

        String estado = (p.getEstado() != null && !p.getEstado().isBlank())
                ? p.getEstado()
                : "-";

        H2 t = new H2(getTranslation("order.detail.titleWithId", String.valueOf(p.getId())));
        t.getStyle().set("margin", "0");

        Span sRest = new Span(getTranslation("order.detail.restaurant") + ": " + rest);
        Span sFecha = new Span(getTranslation("order.detail.date") + ": " + fecha);
        Span sEstado = new Span(getTranslation("order.detail.status") + ": " + estado);
        Span sTotal = new Span(getTranslation("order.detail.total") + ": " + money.format(p.getTotal()));
        sTotal.getStyle().set("font-weight", "800");

        infoCard.add(t, new Hr(), sRest, sFecha, sEstado, sTotal);

        // Form bean
        FormData data = new FormData();
        data.setDireccionEntrega(p.getDireccionEntrega());
        binder.setBean(data);

        boolean editable = "PENDIENTE".equalsIgnoreCase(estado);

        direccionEntrega.setReadOnly(!editable);
        guardar.setEnabled(editable);
        cancelarPedido.setEnabled(editable);

        if (!editable) {
            direccionEntrega.setHelperText(getTranslation("order.detail.notEditableHelper"));
        } else {
            direccionEntrega.setHelperText(null);
        }
    }

    private void onGuardar() {
        if (!binder.validate().isOk()) return;
        if (pedido == null) return;

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) return;

        try {
            Pedido actualizado = pedidoService.actualizarDireccionEntregaSiPendiente(
                    pedido.getId(),
                    cliente,
                    binder.getBean().getDireccionEntrega()
            );
            pedido = actualizado;
            renderPedido(pedido);

            Notification.show(getTranslation("order.detail.updated"), 1600, Notification.Position.BOTTOM_CENTER);

        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);
            guardar.setEnabled(false);
            cancelarPedido.setEnabled(false);
            direccionEntrega.setReadOnly(true);

        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);

        } catch (Exception ex) {
            Notification.show(getTranslation("order.detail.updateError") + ": " + ex.getMessage(),
                    2600, Notification.Position.TOP_CENTER);
        }
    }

    private void onCancelarPedido() {
        if (pedido == null) return;

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) return;

        try {
            pedidoService.cancelarPedidoSiPendiente(pedido.getId(), cliente);

            Notification.show(getTranslation("order.detail.cancelled"), 1800, Notification.Position.BOTTOM_CENTER);
            UI.getCurrent().navigate("home-cliente?tab=pedidos");

        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);
            guardar.setEnabled(false);
            cancelarPedido.setEnabled(false);
            direccionEntrega.setReadOnly(true);

        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("home-cliente?tab=pedidos");

        } catch (Exception ex) {
            Notification.show(getTranslation("order.detail.cancelError") + ": " + ex.getMessage(),
                    2600, Notification.Position.TOP_CENTER);
        }
    }

    // ==========================
    // FORM DATA
    // ==========================
    public static class FormData {
        private String direccionEntrega;
        public String getDireccionEntrega() { return direccionEntrega; }
        public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    }

    // Stub para mantener el título centrado
    private static class DivStub extends Div {
        DivStub(int px) {
            setWidth(px + "px");
            setHeight("1px");
        }
    }
}
