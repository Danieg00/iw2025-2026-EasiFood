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

@PageTitle("Detalle pedido")
@Route("pedido/:id")
@RolesAllowed("ROLE_CLIENTE")
public class PedidoDetalleView extends VerticalLayout implements BeforeEnterObserver {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    private Long pedidoId;
    private Pedido pedido;

    private final Binder<FormData> binder = new Binder<>(FormData.class);

    private static final DateTimeFormatter FECHA_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
    private static final NumberFormat EUR =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    // UI
    private final VerticalLayout page = new VerticalLayout();
    private final VerticalLayout infoCard = new VerticalLayout();
    private final TextArea direccionEntrega = new TextArea("Dirección de entrega");

    private final Button guardar = new Button("Guardar cambios");
    private final Button volver = buildBackButton(); // mismo estilo

    private final Button cancelarPedido = new Button("Cancelar pedido");

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
        direccionEntrega.setWidthFull();
        direccionEntrega.setMinHeight("120px");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.add(direccionEntrega);

        binder.setBean(new FormData());
        // ✅ Validación igual que en Checkout (mínimo 10)
        binder.forField(direccionEntrega)
                .asRequired("La dirección es obligatoria")
                .withValidator(s -> s != null && s.trim().length() >= 10,
                        "Dirección demasiado corta (mínimo 10 caracteres)")
                .withValidator(s -> s == null || s.length() <= 300,
                        "Máximo 300 caracteres")
                .withValidator(s -> {
                    if (s == null) return false;
                    String t = s.trim();
                    return t.chars().anyMatch(Character::isLetter);
                }, "La dirección debe contener letras")
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

        // Acciones: izquierda, como venís haciendo
        VerticalLayout actions = new VerticalLayout(guardar, cancelarPedido);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setAlignItems(Alignment.START);
        actions.getStyle().set("margin-top", "0.75rem");

        page.add(infoCard, form, actions);
        add(page);
    }

    // ==========================
    // HEADER (mismo estilo general: back + título clicable)
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

        Span subtitle = new Span("Detalle pedido");
        subtitle.getStyle().set("opacity", "0.7");

        center.add(title, subtitle);

        DivStub rightStub = new DivStub(44);

        header.add(volver, center, rightStub);
        header.expand(center);

        return header;
    }

    // ==========================
    // BACK BUTTON (copiado 1:1 de PagoView)
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
            Notification.show("Pedido no encontrado", 2200, Notification.Position.TOP_CENTER);
            event.rerouteTo("home-cliente?tab=pedidos");
            return;
        }

        renderPedido(pedido);
    }

    private void renderPedido(Pedido p) {
        infoCard.removeAll();

        String rest = (p.getRestaurante() != null && p.getRestaurante().getNombre() != null)
                ? p.getRestaurante().getNombre()
                : "-";

        String fecha = (p.getFechaCreacion() != null)
                ? p.getFechaCreacion().format(FECHA_FORMAT)
                : "-";

        String estado = (p.getEstado() != null && !p.getEstado().isBlank())
                ? p.getEstado()
                : "-";

        H2 t = new H2("Pedido #" + p.getId());
        t.getStyle().set("margin", "0");

        Span sRest = new Span("Restaurante: " + rest);
        Span sFecha = new Span("Fecha: " + fecha);
        Span sEstado = new Span("Estado: " + estado);
        Span sTotal = new Span("Total: " + EUR.format(p.getTotal()));
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
            direccionEntrega.setHelperText("Este pedido ya no se puede modificar ni cancelar porque no está en estado PENDIENTE.");
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

            Notification.show("Pedido actualizado", 1600, Notification.Position.BOTTOM_CENTER);

        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);
            guardar.setEnabled(false);
            cancelarPedido.setEnabled(false);
            direccionEntrega.setReadOnly(true);

        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);

        } catch (Exception ex) {
            Notification.show("Error al actualizar: " + ex.getMessage(), 2600, Notification.Position.TOP_CENTER);
        }
    }

    private void onCancelarPedido() {
        if (pedido == null) return;

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) return;

        try {
            // ✅ elimina de BD (solo si PENDIENTE) -> implementar en PedidoService
            pedidoService.cancelarPedidoSiPendiente(pedido.getId(), cliente);

            Notification.show("Pedido cancelado", 1800, Notification.Position.BOTTOM_CENTER);
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
            Notification.show("No se pudo cancelar el pedido: " + ex.getMessage(),
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

    // Stub para mantener el título centrado (como en ClienteHomeView)
    private static class DivStub extends Div {
        DivStub(int px) {
            setWidth(px + "px");
            setHeight("1px");
        }
    }
}
