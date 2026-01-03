package com.easifood.app.views;

import com.easifood.app.model.Producto;
import com.easifood.app.service.CarritoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("Checkout")
@Route("checkout/:restauranteId")
@RolesAllowed("ROLE_CLIENTE")
public class CheckoutView extends VerticalLayout implements BeforeEnterObserver {

    private final CarritoService carritoService;

    private Long restauranteId;

    private final Binder<CheckoutData> binder = new Binder<>(CheckoutData.class);

    private final VerticalLayout resumenBox = new VerticalLayout();
    private final Span totalSpan = new Span();

    public CheckoutView(CarritoService carritoService) {
        this.carritoService = carritoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildTopBar());
        add(buildLayout());
    }

    private Component buildTopBar() {
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setAlignItems(Alignment.CENTER);
        top.getStyle().set("gap", "14px");

        Button back = new Button("⬅ Volver", e -> {
            if (restauranteId != null) {
                UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
            } else {
                UI.getCurrent().navigate("home-cliente");
            }
        });

        H2 title = new H2("Checkout");
        title.getStyle().set("margin", "0");

        top.add(back, title);
        return top;
    }

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

        H3 h = new H3("Datos de entrega");
        h.getStyle().set("margin", "0");

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("720px", 2)
        );

        TextField nombre = new TextField("Nombre");
        nombre.setWidthFull();

        TextField telefono = new TextField("Teléfono");
        telefono.setWidthFull();

        TextField direccion = new TextField("Dirección");
        direccion.setWidthFull();

        TextField ciudad = new TextField("Ciudad");
        ciudad.setWidthFull();

        TextField codigoPostal = new TextField("Código postal");
        codigoPostal.setWidthFull();

        EmailField email = new EmailField("Email (opcional)");
        email.setWidthFull();

        TextArea notas = new TextArea("Instrucciones (opcional)");
        notas.setWidthFull();
        notas.setMinHeight("110px");
        notas.setPlaceholder("Ej: Portal 2, 3ºB · Llamar al llegar · Sin cebolla...");

        form.add(nombre, telefono);
        form.add(direccion, ciudad);
        form.add(codigoPostal, email);
        form.add(notas);
        form.setColspan(notas, 2);

        CheckoutData data = new CheckoutData();
        binder.setBean(data);

        binder.forField(nombre)
                .asRequired("El nombre es obligatorio")
                .bind(CheckoutData::getNombre, CheckoutData::setNombre);

        binder.forField(telefono)
                .asRequired("El teléfono es obligatorio")
                .bind(CheckoutData::getTelefono, CheckoutData::setTelefono);

        binder.forField(direccion)
                .asRequired("La dirección es obligatoria")
                .bind(CheckoutData::getDireccion, CheckoutData::setDireccion);

        binder.forField(ciudad)
                .asRequired("La ciudad es obligatoria")
                .bind(CheckoutData::getCiudad, CheckoutData::setCiudad);

        binder.forField(codigoPostal)
                .asRequired("El código postal es obligatorio")
                .bind(CheckoutData::getCodigoPostal, CheckoutData::setCodigoPostal);

        binder.forField(email)
                .bind(CheckoutData::getEmail, CheckoutData::setEmail);

        binder.forField(notas)
                .bind(CheckoutData::getNotas, CheckoutData::setNotas);

        Button confirmar = new Button("Confirmar pedido", e -> confirmarPedido());
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> {
            if (restauranteId != null) UI.getCurrent().navigate("cliente-restaurante/" + restauranteId);
            else UI.getCurrent().navigate("home-cliente");
        });

        HorizontalLayout actions = new HorizontalLayout(cancelar, confirmar);
        actions.setSpacing(true);

        wrap.add(h, form, actions);
        return wrap;
    }

    private Component buildResumen() {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(true);
        box.setSpacing(true);
        box.setWidthFull();

        box.getStyle()
                .set("border-radius", "14px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 title = new H3("Resumen");
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

        NumberFormat eur = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        List<CarritoService.Item> items = carritoService.items(restauranteId);

        if (items.isEmpty()) {
            Span empty = new Span("Tu carrito está vacío.");
            empty.getStyle().set("opacity", "0.7");
            resumenBox.add(empty);
            totalSpan.setText("Total: " + eur.format(BigDecimal.ZERO));
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

            Span right = new Span(eur.format(line));
            right.getStyle().set("font-weight", "700");

            row.add(left, right);
            resumenBox.add(row);
        }

        totalSpan.setText("Total: " + eur.format(carritoService.totalPrecio(restauranteId)));
    }

    private void confirmarPedido() {
        if (!binder.validate().isOk()) return;

        Notification.show("Pedido confirmado (pendiente guardar en BD)", 2200,
                Notification.Position.BOTTOM_CENTER);

        // Normalmente: crear Pedido + líneas y guardarlo
        // Si quieres, vaciamos el carrito tras confirmar:
        carritoService.clear(restauranteId);

        UI.getCurrent().navigate("home-cliente");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        restauranteId = event.getRouteParameters().get("restauranteId").map(Long::valueOf).orElse(null);
        if (restauranteId == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        // Si carrito vacío => volver a la carta
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
        private String nombre;
        private String telefono;
        private String direccion;
        private String ciudad;
        private String codigoPostal;
        private String email;
        private String notas;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }

        public String getCiudad() { return ciudad; }
        public void setCiudad(String ciudad) { this.ciudad = ciudad; }

        public String getCodigoPostal() { return codigoPostal; }
        public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
    }
}
