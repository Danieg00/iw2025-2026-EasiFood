package com.easifood.app.views;

import com.easifood.app.model.Pedido;
import com.easifood.app.service.PedidoService;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.service.PedidoProductoService;
import com.easifood.app.model.Empleado;
import com.easifood.app.service.EmpleadoService;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Área Empleado")
@Route("home-empleado")
@RolesAllowed("ROLE_EMPLEADO")

public class EmpleadoView extends VerticalLayout {

    private final VerticalLayout contentContainer = new VerticalLayout();
    private final EmpleadoService empleadoService;
    private final PedidoService pedidoService;
    private final PedidoProductoService pedidoProductoService;

    private FlexLayout cardsContainer;
    private Empleado empleadoActual;

    public EmpleadoView(RestauranteService restauranteService,
                        EmpleadoService empleadoService,
                        PedidoService pedidoService,
                        PedidoProductoService pedidoProductoService) {

        this.empleadoService = empleadoService;
        this.pedidoService = pedidoService;
        this.pedidoProductoService = pedidoProductoService;
        this.empleadoActual = empleadoService.empleadoActual();

        setWidthFull();
        getStyle().set("min-height", "100vh");
        setPadding(true);
        setSpacing(true);

        getStyle().set("background-image", "linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1920&q=80')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        getStyle().set("background-attachment", "fixed");

        add(buildHeader());

        Tab tabTurno = new Tab("Mi turno");
        Tab tabEnvios = new Tab("Pedidos y Envíos");
        Tabs tabs = new Tabs(tabTurno, tabEnvios);
        styleTabs(tabs);

        contentContainer.setWidthFull();
        contentContainer.setPadding(false);

        tabs.addSelectedChangeListener(e -> {
            if (e.getSelectedTab().equals(tabTurno)) {
                contentContainer.removeAll();
                contentContainer.add(buildTurnoContent());
            } else {
                renderizarVistaPedidos();
            }
        });

        add(tabs, contentContainer);

        tabs.setSelectedTab(tabEnvios);
        renderizarVistaPedidos();

    }


    private void renderizarVistaPedidos() {
        contentContainer.removeAll();

        // Título de la sección
        H2 title = new H2("Pedidos Disponibles y En Curso");
        title.getStyle().set("color", "white").set("text-shadow", "0 2px 4px rgba(0,0,0,0.5)");
        title.getStyle().set("text-align", "center").set("width", "100%");

        // Contenedor Flex para las tarjetas
        cardsContainer = new FlexLayout();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        cardsContainer.getStyle().set("gap", "20px");
        cardsContainer.getStyle().set("padding", "20px");
        cardsContainer.setWidthFull();

        actualizarListaPedidos();

        contentContainer.add(title, cardsContainer);
    }

    private void actualizarListaPedidos() {
        cardsContainer.removeAll();

        if (empleadoActual == null) {
            // Caso de fallo en login o modo dev
            empleadoActual = empleadoService.findFirst();
        }

        List<Pedido> pedidos = new ArrayList<>(pedidoService.findAll());

        // Ordenar: Primero los NO entregados, luego por fecha más reciente
        pedidos.sort((p1, p2) -> {
            boolean p1Entregado = "ENTREGADO".equals(p1.getEstado());
            boolean p2Entregado = "ENTREGADO".equals(p2.getEstado());
            if (p1Entregado != p2Entregado) return p1Entregado ? 1 : -1;

            // Orden por fecha descendente
            if (p2.getFechaCreacion() == null) return -1;
            return p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
        });

        for (Pedido pedido : pedidos) {
            cardsContainer.add(crearCartaPedido(pedido));
        }
    }

    private Component crearCartaPedido(Pedido pedido) {
        // --- 1. Estructura de la Carta ---
        VerticalLayout card = new VerticalLayout();
        card.setWidth("350px");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle().set("background", "rgba(255, 255, 255, 0.95)");
        card.getStyle().set("border-radius", "15px");
        card.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        card.getStyle().set("transition", "transform 0.2s");

        // Efecto Hover simple
        card.getElement().addEventListener("mouseenter", e -> card.getStyle().set("transform", "translateY(-5px)"));
        card.getElement().addEventListener("mouseleave", e -> card.getStyle().set("transform", "translateY(0)"));

        // --- 2. Cabecera de la Carta (Estado y ID) ---
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topRow.setAlignItems(Alignment.CENTER);

        Span idSpan = new Span("#" + pedido.getId());
        idSpan.getStyle().set("font-weight", "bold").set("color", "#555");

        Span badgeEstado = crearBadgeEstado(pedido.getEstado());

        topRow.add(idSpan, badgeEstado);

        // --- 3. Información del Cliente ---
        H3 nombreCliente = new H3(pedido.getCliente() != null ? pedido.getCliente().getNombre() : "Cliente Anónimo");
        nombreCliente.getStyle().set("margin", "10px 0 5px 0").set("font-size", "1.2rem");

        HorizontalLayout dirRow = new HorizontalLayout(new Icon(VaadinIcon.MAP_MARKER), new Span(pedido.getDireccionEntrega()));
        dirRow.setAlignItems(Alignment.CENTER);
        dirRow.getStyle().set("color", "#666").set("font-size", "0.9rem");

        String hora = pedido.getFechaCreacion() != null ?
                pedido.getFechaCreacion().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) : "--:--";
        HorizontalLayout timeRow = new HorizontalLayout(new Icon(VaadinIcon.CLOCK), new Span(hora));
        timeRow.setAlignItems(Alignment.CENTER);
        timeRow.getStyle().set("color", "#666").set("font-size", "0.9rem").set("margin-bottom", "15px");

        // --- 4. Lógica de Asignación y Acciones ---
        VerticalLayout actionsLayout = new VerticalLayout();
        actionsLayout.setPadding(false);
        actionsLayout.setSpacing(true);

        // OBTENEMOS EL EMPLEADO DEL PEDIDO
        Empleado empPedido = pedido.getEmpleado();

        // LÓGICA CORREGIDA: Comparamos IDs, no objetos enteros
        boolean esMio = false;
        if (empleadoActual != null && empPedido != null) {
            esMio = empleadoActual.getId().equals(empPedido.getId());
        }

        boolean sinAsignar = (empPedido == null);

        if (sinAsignar) {
            // CASO 1: NADIE LO TIENE -> Botón para asignármelo
            Button btnAsignar = new Button("Asignarme Pedido", new Icon(VaadinIcon.TRUCK));
            btnAsignar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnAsignar.setWidthFull();
            btnAsignar.addClickListener(e -> {
                asignarmePedido(pedido);
            });
            actionsLayout.add(btnAsignar);

        } else if (esMio) {
            // CASO 2: ES MÍO -> Muestro el SELECTOR de Estados

            Span asignadoA = new Span("Asignado a ti ✅");
            asignadoA.getStyle().set("color", "green").set("font-weight", "bold").set("font-size", "0.9rem");

            Select<String> estadoSelect = new Select<>();
            estadoSelect.setLabel("Estado del pedido");
            // Aquí definimos las opciones exactas que quieres
            estadoSelect.setItems("PENDIENTE", "Preparando", "En Camino", "ENTREGADO");
            estadoSelect.setValue(pedido.getEstado()); // Selecciona el actual
            estadoSelect.setWidthFull();

            // Al cambiar el valor, actualizamos en BD
            estadoSelect.addValueChangeListener(event -> {
                if(event.isFromClient()) {
                    cambiarEstadoPedido(pedido, event.getValue());
                }
            });

            actionsLayout.add(asignadoA, estadoSelect);

        } else {
            // CASO 3: ES DE OTRO -> Muestro quién lo tiene
            String nombreOtro = empPedido.getNombre();
            Span asignadoOtro = new Span("Asignado a: " + nombreOtro);
            asignadoOtro.getStyle().set("color", "red").set("font-weight", "bold").set("font-size", "0.9rem");

            // Icono de candado o prohibido
            HorizontalLayout infoOtro = new HorizontalLayout(new Icon(VaadinIcon.LOCK), asignadoOtro);
            infoOtro.setAlignItems(Alignment.CENTER);

            actionsLayout.add(infoOtro);
        }

        // Botón común para ver productos
        Button btnVerProductos = new Button("Ver Productos", new Icon(VaadinIcon.LIST));
        btnVerProductos.setWidthFull();
        btnVerProductos.addClickListener(e -> abrirDialogProductos(pedido));

        actionsLayout.add(btnVerProductos);

        card.add(topRow, nombreCliente, dirRow, timeRow, new Hr(), actionsLayout);
        return card;
    }

    private void asignarmePedido(Pedido pedido) {
        if (empleadoActual == null) return;

        pedidoService.asignarPedidoAEmpleado(pedido, empleadoActual);

        // Actualizamos UI
        actualizarListaPedidos();
        Notification.show("Pedido asignado correctamente");
    }

    private void cambiarEstadoPedido(Pedido pedido, String nuevoEstado) {
        pedido.setEstado(nuevoEstado);
        pedidoService.guardar(pedido);

        // Refrescamos toda la lista para reordenar si hace falta (ej: entregados al final)
        actualizarListaPedidos();
        Notification.show("Estado cambiado a " + nuevoEstado);
    }

    private Span crearBadgeEstado(String estado) {
        Span badge = new Span(estado);
        badge.getElement().getThemeList().add("badge");

        switch (estado) {
            case "PENDIENTE": badge.getElement().getThemeList().add("error"); break; // Rojo
            case "Preparando": badge.getElement().getThemeList().add("contrast"); break; // Gris oscuro
            case "En Camino": badge.getElement().getThemeList().add("primary"); break; // Azul
            case "ENTREGADO": badge.getElement().getThemeList().add("success"); break; // Verde
        }
        return badge;
    }


    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 logo = new H1("Área Empleado");
        logo.getStyle().set("color", "white").set("margin", "0").set("font-size", "1.5rem");

        // Perfil usuario
        HorizontalLayout perfil = new HorizontalLayout();
        perfil.setAlignItems(Alignment.CENTER);

        String nombre = (empleadoActual != null) ? empleadoActual.getNombre() : "Usuario";
        Span nombreSpan = new Span(nombre);
        nombreSpan.getStyle().set("color", "white").set("font-weight", "bold");

        Avatar avatar = new Avatar(nombre);
        avatar.getStyle().set("cursor", "pointer");

        // Menu contextual logout
        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem("Mi Perfil", e -> getUI().ifPresent(ui -> ui.navigate("perfil")));
        menu.addItem("Cerrar Sesión", e -> UI.getCurrent().getPage().setLocation("/login"));

        perfil.add(nombreSpan, avatar);
        header.add(logo, perfil);
        return header;
    }

    private void styleTabs(Tabs tabs) {
        tabs.getStyle().set("background", "rgba(255, 255, 255, 0.9)");
        tabs.getStyle().set("border-radius", "50px");
        tabs.getStyle().set("padding", "4px 8px");
        tabs.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
        tabs.getStyle().set("margin", "10px auto"); // Centrado
        tabs.setWidth("fit-content");
    }

    private Component buildTurnoContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(Alignment.CENTER);

        Div card = new Div();
        card.getStyle().set("background", "white").set("padding", "30px").set("border-radius", "15px");
        card.setWidth("400px");

        H2 title = new H2("Mi Turno Actual");
        TextField turno = new TextField("Horario Asignado");
        turno.setReadOnly(true);
        turno.setValue("09:00 - 17:00");
        turno.setWidthFull();

        card.add(title, turno);
        layout.add(card);
        return layout;
    }


    private void abrirDialogProductos(Pedido pedido) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Pedido #" + pedido.getId());

        Grid<PedidoProducto> grid = new Grid<>(PedidoProducto.class, false);
        grid.addColumn(pp -> pp.getProducto().getNombre()).setHeader("Producto");
        grid.addColumn(PedidoProducto::getCantidad).setHeader("Cant.");
        grid.setItems(pedidoProductoService.productosDelPedido(pedido));

        Button cerrar = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(cerrar);
        dialog.open();
    }

}
