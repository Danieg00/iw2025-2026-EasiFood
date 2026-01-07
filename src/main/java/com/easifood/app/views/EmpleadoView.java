package com.easifood.app.views;

import com.easifood.app.model.Pedido;
import com.easifood.app.service.PedidoService;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.service.PedidoProductoService;
import com.easifood.app.model.Empleado;
import com.easifood.app.service.EmpleadoService;
import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;



@PageTitle("Área Repartidor Test")
@Route("home-empleado")
@PermitAll
//@RolesAllowed("ROLE_REPARTIDOR")

public class EmpleadoView extends VerticalLayout {

    /*com.vaadin.flow.component.dialog.Dialog dialog =
            new com.vaadin.flow.component.dialog.Dialog();*/

    private final VerticalLayout content = new VerticalLayout();
    private final Map<Tab, Component> tabToContent = new HashMap<>();

    private final RestauranteService restauranteService;
    private final EmpleadoService empleadoService;
    private final PedidoService pedidoService;
    private final PedidoProductoService pedidoProductoService;


    public EmpleadoView(RestauranteService restauranteService,
                        EmpleadoService empleadoService,
                        PedidoService pedidoService,
                        PedidoProductoService pedidoProductoService) {

        this.restauranteService = restauranteService;
        this.empleadoService = empleadoService;
        this.pedidoService = pedidoService;
        this.pedidoProductoService = pedidoProductoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        Tab tabTurno = new Tab("Mi turno");
        Tab tabEnvios = new Tab("Mis envíos");

        Tabs tabs = new Tabs(tabTurno, tabEnvios);
        tabs.setWidthFull();

        tabToContent.put(tabTurno, buildTurnoContent());
        tabToContent.put(tabEnvios, buildEnviosContent());

        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);

        add(tabs, content);
        expand(content);

        showContent(tabTurno);

        tabs.addSelectedChangeListener(e -> showContent(e.getSelectedTab()));
    }

    private Component buildEnviosContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mis pedidos");
        title.getStyle().set("margin", "0");

        Empleado empleado = empleadoService.empleadoActual();

        if (1 == 0) {//if (empleado == null) {  ARREGLAR LOGIN
            layout.add(new Span("Empleado no autenticado"));
            return layout;
        }

        //List<Pedido> pedidos = pedidoService.findAll();//pedidoService.pedidosDelEmpleado(empleado);//(empleadoService.findFirst());//(empleado);

        List<Pedido> pedidos = new ArrayList<>(pedidoService.findAll());

        pedidos.sort(
                // 1️⃣ Primero: los que NO están ENTREGADOS
                (p1, p2) -> {
                    boolean p1Entregado = "ENTREGADO".equals(p1.getEstado());
                    boolean p2Entregado = "ENTREGADO".equals(p2.getEstado());

                    if (p1Entregado != p2Entregado) {
                        return p1Entregado ? 1 : -1; // no entregados arriba
                    }

                    // 2️⃣ Luego: fecha más reciente primero
                    return
                            p2.getFechaCreacion() == null ? 1 :
                                    p1.getFechaCreacion() == null ? -1 :
                                            p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
                }
        );

        ListDataProvider<Pedido> provider = new ListDataProvider<>(pedidos);

        Grid<Pedido> grid = new Grid<>(Pedido.class, false);
        grid.setSizeFull();

       /* grid.addComponentColumn(pedido ->
                new Button("Cambiar estado", e -> cambiarEstadoPedido(pedido, provider))
        );*/

        grid.addComponentColumn(pedido -> {

            VerticalLayout actions = new VerticalLayout();
            actions.setPadding(false);
            actions.setSpacing(false);

            Button cambiarEstado = new Button("Cambiar estado",
                    e -> cambiarEstadoPedido(pedido, provider)
            );

            Button asignarme = new Button("Asignarme",
                    e -> asignarmePedido(pedido, provider)
            );

            Button verProductos = new Button("Ver productos");
            verProductos.addClickListener(e -> abrirDialogProductos(pedido));
           /*Button verProductos = new Button("Ver productos",
                    e -> abrirDialogProductos(pedido)
            );*/

            // Mostrar solo si no tiene empleado
            asignarme.setVisible(pedido.getEmpleado() == null);

            actions.add(verProductos, asignarme, cambiarEstado);
            return actions;

        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        /*grid.Column(pedido -> {
            Button verProductos = new Button("Ver productos",
                    e -> abrirDialogProductos(pedido)
            );
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);*/

        grid.addColumn(p ->
                        p.getCliente() != null ? p.getCliente().getNombre() : "—"
                ).setHeader("Cliente")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getDireccionEntrega)
                .setHeader("Dirección")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getEstado)
                .setHeader("Estado")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getFechaCreacion)
                .setHeader("Fecha")
                .setAutoWidth(true);

        grid.setDataProvider(provider);

        layout.add(title, grid);
        layout.expand(grid);
        return layout;
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.BASELINE);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 title = new H1("Área Repartidor");
        title.getStyle().set("margin", "0");

        Empleado empleado = empleadoService.empleadoActual();

        Span userInfo;

        if (empleado == null) {
            userInfo = new Span("Empleado no autenticado");
        } else if (empleado.getRestaurante() == null) {
            userInfo = new Span("Sin restaurante asignado");
        } else {
            userInfo = new Span(
                    empleado.getNombre() + " — " +
                            empleado.getRestaurante().getNombre()
            );
        }

        userInfo.getStyle().set("opacity", "0.7");

        header.add(title, userInfo);
        return header;
    }

    private void showContent(Tab selected) {
        content.removeAll();
        Component c = tabToContent.get(selected);
        if (c != null) content.add(c);
    }

    private Component buildTurnoContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mi turno");
        title.getStyle().set("margin", "0");

        TextField turno = new TextField("Horario del turno");
        turno.setReadOnly(true);
        turno.setValue("Turno actual: 9:00 AM - 5:00 PM"); // Valor hardcodeado

        layout.add(title, turno);
        return layout;
    }

    private void asignarmePedido(Pedido pedido, ListDataProvider<Pedido> provider) {

        Empleado empleadoActual = empleadoService.empleadoActual();

        if (empleadoActual == null) {
            return; // o Notification
        }

        pedidoService.asignarPedidoAEmpleado(pedido, empleadoActual);

        provider.refreshItem(pedido);
    }

    /*private Component buildEnviosContent() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mis pedidos");
        title.getStyle().set("margin", "0");

        Empleado empleado = empleadoService.empleadoActual();

        if (empleado == null) {
            layout.add(new Span("Empleado no autenticado"));
            return layout;
        }

        List<Pedido> pedidos = pedidoService.pedidosDelEmpleado(empleado);
        ListDataProvider<Pedido> provider = new ListDataProvider<>(pedidos);

        Grid<Pedido> grid = new Grid<>(Pedido.class, false);
        grid.setSizeFull();

        grid.addColumn(p -> p.getCliente().getNombre())
                .setHeader("Cliente")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getDireccionEntrega)
                .setHeader("Dirección")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getEstado)
                .setHeader("Estado")
                .setAutoWidth(true);

        grid.addColumn(Pedido::getFechaCreacion)
                .setHeader("Fecha")
                .setAutoWidth(true);

        grid.addComponentColumn(pedido ->
                        new Button("Cambiar estado", e -> cambiarEstadoPedido(pedido, provider))
                ).setHeader("Acciones")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setDataProvider(provider);

        layout.add(title, grid);
        layout.expand(grid);

        return layout;
    }

    /*private Component buildEnviosContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        H1 title = new H1("Mis envíos");
        title.getStyle().set("margin", "0");

        TextField search = new TextField();
        search.setPlaceholder("Buscar por cliente...");
        search.setClearButtonVisible(true);
        search.setWidth("320px");

        // Lista de envíos hardcodeada
        List<Envio> envios = new ArrayList<>();
        envios.add(new Envio(1, "Juan Pérez", "Calle Falsa 123", "En espera", "2023-12-28"));
        envios.add(new Envio(2, "María López", "Av. Siempre Viva 742", "En camino", "2023-12-27"));
        envios.add(new Envio(3, "Carlos García", "Plaza Mayor 456", "Entregado", "2023-12-29"));

        ListDataProvider<Envio> provider = new ListDataProvider<>(envios);

        Grid<Envio> grid = new Grid<>(Envio.class, false);
        grid.setSizeFull();

        grid.addColumn(Envio::getClienteNombre)
                .setHeader("Cliente")
                .setAutoWidth(true);

        grid.addColumn(Envio::getDireccion)
                .setHeader("Dirección")
                .setAutoWidth(true);

        grid.addColumn(Envio::getEstado)
                .setHeader("Estado")
                .setAutoWidth(true);

        grid.addColumn(Envio::getFechaAsignacion)
                .setHeader("Fecha de asignación")
                .setAutoWidth(true);

        // Columna de acciones con el botón para cambiar estado
        grid.addComponentColumn(envio ->
                new Button("Cambiar estado", e -> cambiarEstado(envio))
        ).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        grid.setDataProvider(provider);

        search.addValueChangeListener(e -> {
            String term = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            provider.setFilter(envio ->
                    envio.getClienteNombre() != null && envio.getClienteNombre().toLowerCase().contains(term)
            );
        });

        layout.add(title, search, grid);
        layout.expand(grid);
        return layout;
    }*/

    //////////////AQUI
    /*private void cambiarEstadoPedido(Pedido pedido, ListDataProvider<Pedido> provider) {

        // Cambia el estado cíclicamente
        switch (pedido.getEstado()) {
            case "PENDIENTE" -> pedido.setEstado("EN_PREPARACION");
            case "EN_PREPARACION" -> pedido.setEstado("EN_CAMINO");
            case "EN_CAMINO" -> pedido.setEstado("ENTREGADO");
            default -> pedido.setEstado("PENDIENTE");
        }

        // Guarda en base de datos
        pedidoService.guardar(pedido);

        // Actualiza la fila correspondiente
        provider.refreshItem(pedido);
    }*/

    private void cambiarEstadoPedido(Pedido pedido, ListDataProvider<Pedido> provider) {

        switch (pedido.getEstado()) {
            case "PENDIENTE":
                pedido.setEstado("EN_PREPARACION");
                break;
            case "EN_PREPARACION":
                pedido.setEstado("EN_CAMINO");
                break;
            case "EN_CAMINO":
                pedido.setEstado("ENTREGADO");
                break;
            default:
                pedido.setEstado("PENDIENTE");
        }

        pedidoService.guardar(pedido);
        //provider.refreshItem(pedido);

        //reordenar si pedido cambia a entregado
        List<Pedido> pedidosOrdenados = new ArrayList<>(pedidoService.findAll());

        pedidosOrdenados.sort((p1, p2) -> {
            boolean p1Entregado = "ENTREGADO".equals(p1.getEstado());
            boolean p2Entregado = "ENTREGADO".equals(p2.getEstado());

            if (p1Entregado != p2Entregado) {
                return p1Entregado ? 1 : -1;
            }

            return
                    p2.getFechaCreacion() == null ? 1 :
                            p1.getFechaCreacion() == null ? -1 :
                                    p2.getFechaCreacion().compareTo(p1.getFechaCreacion());
        });

        provider.getItems().clear();
        provider.getItems().addAll(pedidosOrdenados);
        provider.refreshAll();

        //provider.refreshAll();
    }

    private void abrirDialogProductos(Pedido pedido) {

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H1 title = new H1("Productos del pedido #" +
                (pedido.getId() != null ? pedido.getId() : "—"));

        Grid<PedidoProducto> grid = new Grid<>(PedidoProducto.class, false);

        grid.addColumn(pp ->
                pp.getProducto() != null ? pp.getProducto().getNombre() : "—"
        ).setHeader("Producto");

        grid.addColumn(PedidoProducto::getCantidad)
                .setHeader("Cantidad");

        grid.addColumn(pp ->
                pp.getPrecioUnitario() != null ? pp.getPrecioUnitario() : BigDecimal.ZERO
        ).setHeader("Precio");

        grid.addColumn(pp ->
                pp.getPrecioUnitario() != null
                        ? pp.getPrecioUnitario().multiply(BigDecimal.valueOf(pp.getCantidad()))
                        : BigDecimal.ZERO
        ).setHeader("Subtotal");

        List<PedidoProducto> lineas =
                pedidoProductoService.productosDelPedido(pedido);

        if (lineas != null) {
            grid.setItems(lineas);
        }

        Button cerrar = new Button("Cerrar", e -> dialog.close());

        dialog.add(title, grid, cerrar);
        dialog.open();
    }

    /*
    private void abrirDialogProductos(Pedido pedido) {

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        H1 title = new H1("Productos del pedido #" + pedido.getId());

        Grid<PedidoProducto> grid = new Grid<>(PedidoProducto.class, false);

        grid.addColumn(pp -> pp.getProducto().getNombre())
                .setHeader("Producto")
                .setAutoWidth(true);

        grid.addColumn(PedidoProducto::getCantidad)
                .setHeader("Cantidad")
                .setAutoWidth(true);

        grid.addColumn(pp -> pp.getPrecioUnitario())
                .setHeader("Precio")
                .setAutoWidth(true);

        grid.addColumn(pp ->
                pp.getPrecioUnitario().multiply(
                        BigDecimal.valueOf(pp.getCantidad())
                )
        ).setHeader("Subtotal");

        List<PedidoProducto> lineas = pedidoProductoService.productosDelPedido(pedido);
        grid.setItems(lineas);

        Button cerrar = new Button("Cerrar", e -> dialog.close());

        dialog.add(
                title,
                grid,
                cerrar
        );

        dialog.open();
    }*/

    /*
    private void cambiarEstado(Envio envio) {
        // Cambia el estado cíclicamente entre "En espera", "En camino", "Entregado"
        switch (envio.getEstado()) {
            case "En espera":
                envio.setEstado("En camino");
                break;
            case "En camino":
                envio.setEstado("Entregado");
                break;
            case "Entregado":
                envio.setEstado("En espera");
                break;
        }
        // Actualiza la vista después de cambiar el estado
        UI.getCurrent().getPage().reload();
    }

    // Clase Envio incluida en el mismo archivo
    public static class Envio {
        private int id;
        private String clienteNombre;
        private String direccion;
        private String estado;
        private String fechaAsignacion;

        public Envio(int id, String clienteNombre, String direccion, String estado, String fechaAsignacion) {
            this.id = id;
            this.clienteNombre = clienteNombre;
            this.direccion = direccion;
            this.estado = estado;
            this.fechaAsignacion = fechaAsignacion;
        }

        public int getId() {
            return id;
        }

        public String getClienteNombre() {
            return clienteNombre;
        }

        public String getDireccion() {
            return direccion;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getFechaAsignacion() {
            return fechaAsignacion;
        }
    }*/
}
