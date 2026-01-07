package com.easifood.app.views;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.model.Producto;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.PedidoService;
import com.easifood.app.service.TicketPdfService;
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
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@PageTitle("Detalle pedido")
@Route("pedido/:id")
@RolesAllowed("ROLE_CLIENTE")
public class PedidoDetalleView extends VerticalLayout implements BeforeEnterObserver {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final TicketPdfService ticketPdfService;

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
    private final Button volver = buildBackButton();
    private final Button cancelarPedido = new Button("Cancelar pedido");

    // ✅ Ticket PDF (Vaadin 24.8+)
    private Anchor descargarTicket;

    // ✅ Líneas (UI)
    private final VerticalLayout lineasCard = new VerticalLayout();
    private final VerticalLayout lineasList = new VerticalLayout();
    private final Span totalSpan = new Span();

    // ✅ Draft: productoId -> (nombre, cantidad, precioUnitario)
    private final Map<Long, LineaDraft> draft = new LinkedHashMap<>();

    private static class LineaDraft {
        Long productoId;
        String nombre;
        int cantidad;
        BigDecimal precioUnitario;

        LineaDraft(Long productoId, String nombre, int cantidad, BigDecimal precioUnitario) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnitario = (precioUnitario != null) ? precioUnitario : BigDecimal.ZERO;
        }

        BigDecimal subtotal() {
            return precioUnitario.multiply(BigDecimal.valueOf(Math.max(0, cantidad)));
        }
    }

    public PedidoDetalleView(PedidoService pedidoService,
                             UsuarioService usuarioService,
                             TicketPdfService ticketPdfService) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.ticketPdfService = ticketPdfService;

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

        // Form dirección
        direccionEntrega.setWidthFull();
        direccionEntrega.setMinHeight("120px");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.add(direccionEntrega);

        binder.setBean(new FormData());
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

        // Botones
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("font-weight", "700").set("cursor", "pointer").set("min-width", "180px");
        guardar.addClickListener(e -> onGuardar());

        cancelarPedido.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelarPedido.getStyle()
                .set("cursor", "pointer")
                .set("font-size", "0.9rem")
                .set("opacity", "0.85");
        cancelarPedido.addClickListener(e -> onCancelarPedido());

        // ✅ Botón descarga (Anchor + DownloadHandler)
        descargarTicket = buildTicketDownload();

        VerticalLayout actions = new VerticalLayout(guardar, cancelarPedido);
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.setAlignItems(Alignment.START);
        actions.getStyle().set("margin-top", "0.75rem");

        // ✅ Card líneas
        lineasCard.setPadding(true);
        lineasCard.setSpacing(true);
        lineasCard.setWidthFull();
        lineasCard.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "18px")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 lineasTitle = new H3("Productos del pedido");
        lineasTitle.getStyle().set("margin", "0");

        lineasList.setPadding(false);
        lineasList.setSpacing(true);

        totalSpan.getStyle().set("font-weight", "900").set("font-size", "1.05rem");

        lineasCard.add(lineasTitle, new Hr(), lineasList, new Hr(), totalSpan);

        page.add(infoCard, lineasCard, form, actions);
        add(page);

        // hasta que se cargue el pedido
        descargarTicket.setEnabled(false);
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

        Span subtitle = new Span("Detalle pedido");
        subtitle.getStyle().set("opacity", "0.7");

        center.add(title, subtitle);

        DivStub rightStub = new DivStub(44);

        header.add(volver, center, rightStub);
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

        back.addClickListener(e -> UI.getCurrent().navigate("home-cliente?tab=pedidos"));
        return back;
    }

    // ==========================
    // DOWNLOAD (Vaadin 24.8+)
    // ==========================
    private Anchor buildTicketDownload() {
        Button btn = new Button("Descargar ticket (PDF)");
        btn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btn.getStyle().set("font-weight", "700").set("cursor", "pointer");

        // Genera en el momento de descargar, validando usuario/pedido
        DownloadHandler handler = DownloadHandler.fromInputStream(event -> {
            try {
                Usuario u = usuarioService.obtenerUsuarioActual();
                if (!(u instanceof Cliente cliente)) {
                    return DownloadResponse.error(403);
                }
                if (pedidoId == null) {
                    return DownloadResponse.error(400);
                }

                Pedido p = pedidoService.pedidoDeCliente(pedidoId, cliente).orElse(null);
                if (p == null) {
                    return DownloadResponse.error(404);
                }

                byte[] pdf = ticketPdfService.generarTicket(p);
                String filename = "ticket-pedido-" + p.getId() + ".pdf";

                return new DownloadResponse(
                        new ByteArrayInputStream(pdf),
                        filename,
                        "application/pdf",
                        (long) pdf.length
                );
            } catch (Exception ex) {
                return DownloadResponse.error(500);
            }
        });

        Anchor a = new Anchor(handler, "");
        a.setTarget("_blank"); // opcional
        a.add(btn);
        return a;
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

        loadDraftFromPedido(pedido);

        renderPedido(pedido);
        renderLineasFromDraft();

        // ya hay pedido válido
        descargarTicket.setEnabled(true);
    }

    private void loadDraftFromPedido(Pedido p) {
        draft.clear();
        if (p.getLineas() == null) return;

        for (PedidoProducto l : p.getLineas()) {
            Producto prod = l.getProducto();
            if (prod == null || prod.getId() == null) continue;

            Long pid = prod.getId();
            String nombre = (prod.getNombre() != null) ? prod.getNombre() : "Producto";
            int qty = l.getCantidad();
            BigDecimal pu = l.getPrecioUnitario();

            draft.put(pid, new LineaDraft(pid, nombre, qty, pu));
        }
    }

    // ==========================
    // RENDER
    // ==========================
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

        descargarTicket.getStyle()
                .set("margin-left", "auto"); // empuja a la derecha

        HorizontalLayout titleRow = new HorizontalLayout(t, descargarTicket);
        titleRow.setWidthFull();
        titleRow.setAlignItems(Alignment.CENTER);

        Span sRest = new Span("Restaurante: " + rest);
        Span sFecha = new Span("Fecha: " + fecha);
        Span sEstado = new Span("Estado: " + estado);

        infoCard.add(titleRow, new Hr(), sRest, sFecha, sEstado);

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
            direccionEntrega.setHelperText("Puedes cambiar dirección y productos. Los cambios se guardan solo al pulsar Guardar cambios.");
        }
    }

    private void renderLineasFromDraft() {
        lineasList.removeAll();

        boolean editable = (pedido != null && "PENDIENTE".equalsIgnoreCase(pedido.getEstado()));

        if (draft.isEmpty()) {
            Span empty = new Span("No hay productos en el pedido.");
            empty.getStyle().set("opacity", "0.7");
            lineasList.add(empty);
            totalSpan.setText("Total: " + EUR.format(BigDecimal.ZERO));
            return;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (LineaDraft d : draft.values()) {
            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);
            row.getStyle()
                    .set("padding", "10px")
                    .set("border-radius", "12px")
                    .set("background", "var(--lumo-contrast-5pct)");

            Span name = new Span(d.nombre);
            name.getStyle().set("font-weight", "700");

            Button minus = new Button(new Icon(VaadinIcon.MINUS));
            Button plus = new Button(new Icon(VaadinIcon.PLUS));
            Button remove = new Button(new Icon(VaadinIcon.TRASH));

            minus.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            plus.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            remove.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);

            minus.getStyle().set("cursor", "pointer");
            plus.getStyle().set("cursor", "pointer");
            remove.getStyle().set("cursor", "pointer");

            Span qty = new Span(String.valueOf(d.cantidad));
            qty.getStyle().set("min-width", "28px").set("text-align", "center").set("font-weight", "800");

            HorizontalLayout qtyBox = new HorizontalLayout(minus, qty, plus);
            qtyBox.setAlignItems(Alignment.CENTER);

            Span subtotal = new Span(EUR.format(d.subtotal()));
            subtotal.getStyle().set("font-weight", "800");

            minus.setEnabled(editable);
            plus.setEnabled(editable);
            remove.setEnabled(editable);

            minus.addClickListener(e -> {
                if (!editable) return;
                d.cantidad -= 1;
                if (d.cantidad <= 0) {
                    draft.remove(d.productoId);
                    Notification.show("Producto quitado (pendiente de guardar)", 1600, Notification.Position.BOTTOM_CENTER);
                }
                renderLineasFromDraft();
            });

            plus.addClickListener(e -> {
                if (!editable) return;
                d.cantidad += 1;
                renderLineasFromDraft();
            });

            remove.addClickListener(e -> {
                if (!editable) return;
                draft.remove(d.productoId);
                Notification.show("Producto eliminado (pendiente de guardar)", 1600, Notification.Position.BOTTOM_CENTER);
                renderLineasFromDraft();
            });

            row.add(name);
            row.expand(name);
            row.add(qtyBox, subtotal, remove);

            lineasList.add(row);
            total = total.add(d.subtotal());
        }

        totalSpan.setText("Total: " + EUR.format(total));
        if (!editable) {
            Span info = new Span("Los productos no se pueden modificar porque el pedido no está en estado PENDIENTE.");
            info.getStyle().set("opacity", "0.7").set("font-size", "0.9rem");
            lineasList.add(info);
        }
    }

    // ==========================
    // GUARDAR
    // ==========================
    private void onGuardar() {
        if (pedido == null) return;
        if (!binder.validate().isOk()) return;

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) return;

        try {
            pedido = pedidoService.actualizarDireccionEntregaSiPendiente(
                    pedido.getId(),
                    cliente,
                    binder.getBean().getDireccionEntrega()
            );

            List<PedidoService.LineaUpdate> updates = draft.values().stream()
                    .map(d -> new PedidoService.LineaUpdate(d.productoId, d.cantidad))
                    .toList();

            pedido = pedidoService.actualizarLineasSiPendiente(pedido.getId(), cliente, updates);

            loadDraftFromPedido(pedido);

            renderPedido(pedido);
            renderLineasFromDraft();

            Notification.show("Pedido actualizado", 1600, Notification.Position.BOTTOM_CENTER);

        } catch (IllegalStateException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);
            guardar.setEnabled(false);
            cancelarPedido.setEnabled(false);
            direccionEntrega.setReadOnly(true);

        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 2400, Notification.Position.TOP_CENTER);

        } catch (Exception ex) {
            Notification.show("Error al guardar: " + ex.getMessage(), 2600, Notification.Position.TOP_CENTER);
        }
    }

    private void onCancelarPedido() {
        if (pedido == null) return;

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) return;

        try {
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

    private static class DivStub extends Div {
        DivStub(int px) {
            setWidth(px + "px");
            setHeight("1px");
        }
    }
}
