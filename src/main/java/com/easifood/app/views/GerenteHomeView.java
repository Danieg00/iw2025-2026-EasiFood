package com.easifood.app.views;

import com.easifood.app.model.*;
import com.easifood.app.repository.EmpleadoRepository;
import com.easifood.app.repository.RestauranteRepository;
import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.service.FileStorageService;
import com.easifood.app.service.PedidoService;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.UsuarioService;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.component.upload.Upload;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Route("home-gerente")
@RolesAllowed("ROLE_GERENTE")
public class GerenteHomeView extends VerticalLayout implements AfterNavigationObserver {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProductoService productoService;
    private final FileStorageService fileStorageService;
    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;

    private FlexLayout cardsContainer;
    private Restaurante restauranteActual;
    private Gerente gerenteActual;

    private Tabs tabs;
    private Tab tabEmpleados;
    private Tab tabProductos;
    private Tab tabOfertas;
    private Tab tabGestion;

    private Button btnNuevo;

    public GerenteHomeView(EmpleadoRepository empleadoRepository,
                           UsuarioRepository usuarioRepository,
                           RestauranteRepository restauranteRepository,
                           ProductoService productoService,
                           FileStorageService fileStorageService,
                           UsuarioService usuarioService,
                           PedidoService pedidoService) {

        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.productoService = productoService;
        this.fileStorageService = fileStorageService;
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        getStyle().set("background-image",
                "linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        getStyle().set("background-attachment", "fixed");

        cargarDatosDelGerente();

        if (restauranteActual == null) {
            Notification.show(getTranslation("manager.home.noRestaurantInfo"));
        }

        // ==========================
        // HEADER
        // ==========================
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.setPadding(false);
        headerContainer.setSpacing(false);
        headerContainer.setWidthFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        topRow.setAlignItems(Alignment.CENTER);

        H1 logoApp = new H1("EasiFood");
        logoApp.getStyle().set("color", "white");
        logoApp.getStyle().set("margin", "0");

        HorizontalLayout perfilGerente = new HorizontalLayout();
        perfilGerente.setAlignItems(Alignment.CENTER);

        String nombreMostrar = (gerenteActual != null && gerenteActual.getNombre() != null && !gerenteActual.getNombre().isBlank())
                ? gerenteActual.getNombre()
                : getTranslation("manager.home.managerFallback");

        Span nombreSpan = new Span(nombreMostrar);
        nombreSpan.getStyle().set("color", "white").set("font-weight", "bold");

        Avatar avatar = new Avatar(nombreMostrar);
        if (gerenteActual != null && gerenteActual.getImagen() != null && !gerenteActual.getImagen().isBlank()) {
            avatar.setImage(gerenteActual.getImagen());
        }
        avatar.getStyle().set("cursor", "pointer");

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem(getTranslation("common.myProfile"), e -> getUI().ifPresent(ui -> ui.navigate("perfil")));
        menu.addItem(getTranslation("common.logout"), e -> usuarioService.logout());

        perfilGerente.add(nombreSpan, avatar);
        topRow.add(logoApp, perfilGerente);

        Div bottomRow = new Div();
        bottomRow.setWidthFull();
        bottomRow.getStyle().set("text-align", "left");
        bottomRow.getStyle().set("margin-top", "-10px");

        String nombreRest = (restauranteActual != null && restauranteActual.getNombre() != null && !restauranteActual.getNombre().isBlank())
                ? restauranteActual.getNombre()
                : getTranslation("manager.home.noRestaurantAssignedShort");

        H2 nombreRestaurante = new H2(nombreRest);
        nombreRestaurante.getStyle().set("margin", "0");
        nombreRestaurante.getStyle().set("color", "#e0e0e0");
        nombreRestaurante.getStyle().set("font-size", "1.8rem");
        nombreRestaurante.getStyle().set("text-shadow", "0 2px 4px rgba(0,0,0,0.5)");

        bottomRow.add(nombreRestaurante);
        headerContainer.add(topRow, bottomRow);

        // ==========================
        // TABS
        // ==========================
        tabEmpleados = new Tab(getTranslation("manager.home.tabs.employees"));
        tabProductos = new Tab(getTranslation("manager.home.tabs.products"));
        tabOfertas = new Tab(getTranslation("manager.home.tabs.offers"));
        tabGestion = new Tab(getTranslation("manager.home.tabs.management"));

        tabs = new Tabs(tabEmpleados, tabProductos, tabOfertas, tabGestion);
        tabs.getStyle().set("background", "transparent");

        Div navigationBar = new Div(tabs);
        navigationBar.setWidth("fit-content");
        navigationBar.getStyle().set("margin", "1rem 0 1.5rem 0");
        navigationBar.getStyle().set("background-color", "rgba(255, 255, 255, 0.8)");
        navigationBar.getStyle().set("border-radius", "50px");
        navigationBar.getStyle().set("padding", "4px 8px");
        navigationBar.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");

        tabs.addSelectedChangeListener(event -> {
            actualizarBotonNuevo();
            refrescarContenido();
        });

        // ==========================
        // BOTÓN NUEVO
        // ==========================
        btnNuevo = new Button(getTranslation("manager.home.new.employee"));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.getStyle().set("cursor", "pointer");
        btnNuevo.addClickListener(e -> accionBotonNuevo());

        // ==========================
        // CONTENEDOR CARDS
        // ==========================
        cardsContainer = new FlexLayout();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.setJustifyContentMode(JustifyContentMode.START);
        cardsContainer.getStyle().set("gap", "20px");
        cardsContainer.setWidthFull();

        refrescarContenido();

        add(headerContainer, navigationBar, btnNuevo, cardsContainer);
    }

    // PageTitle dinámico para multiidioma
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("manager.home.pageTitle"));
    }

    private void actualizarBotonNuevo() {
        btnNuevo.setVisible(true);

        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            btnNuevo.setText(getTranslation("manager.home.new.employee"));
        } else if (tabs.getSelectedTab().equals(tabProductos)) {
            btnNuevo.setText(getTranslation("manager.home.new.product"));
        } else if (tabs.getSelectedTab().equals(tabOfertas)) {
            btnNuevo.setText(getTranslation("manager.home.new.offer"));
        } else {
            btnNuevo.setVisible(false);
        }
    }

    private void accionBotonNuevo() {
        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            abrirEditorEmpleado(new Empleado());
        } else if (tabs.getSelectedTab().equals(tabProductos)) {
            abrirEditorProducto(new Producto());
        } else {
            abrirEditorOferta(new Producto());
        }
    }

    private void refrescarContenido() {
        cardsContainer.removeAll();
        if (restauranteActual == null) {
            cardsContainer.add(new Span(getTranslation("manager.home.noRestaurantData")));
            return;
        }

        if (tabs.getSelectedTab().equals(tabGestion)) {
            renderGestionRestaurante();
        } else {
            if (tabs.getSelectedTab().equals(tabEmpleados)) {
                refrescarEmpleados();
            } else if (tabs.getSelectedTab().equals(tabProductos)) {
                List<Producto> productos = productoService.productosDelRestaurante(restauranteActual);
                productos.stream().filter(p -> !p.isEsMenu()).forEach(p -> cardsContainer.add(crearCardProducto(p)));
            } else {
                List<Producto> productos = productoService.productosDelRestaurante(restauranteActual);
                productos.stream().filter(Producto::isEsMenu).forEach(p -> cardsContainer.add(crearCardProducto(p)));
            }
        }
    }

    // Recupera el Usuario logueado, verifica que sea Gerente y obtiene su Restaurante.
    private void cargarDatosDelGerente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Modo prueba si no hay auth real
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Notification.show(getTranslation("manager.home.testModeLoadingFirst"), 3000, Notification.Position.TOP_CENTER);

            this.restauranteActual = restauranteRepository.findAll().stream().findFirst().orElse(null);
            if (restauranteActual != null) {
                // gerenteActual puede quedarse null en modo prueba
                this.gerenteActual = null;
            }
            return;
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);

        if (usuario instanceof Gerente g) {
            this.gerenteActual = g;
            this.restauranteActual = g.getRestaurante();
        }
    }

    // Busca los empleados en BD y regenera las tarjetas.
    private void refrescarEmpleados() {
        cardsContainer.removeAll();
        if (restauranteActual == null) {
            cardsContainer.add(new Span(getTranslation("manager.home.noRestaurantAssigned")));
            return;
        }
        List<Empleado> empleados = empleadoRepository.findByRestaurante(restauranteActual);
        for (Empleado emp : empleados) {
            cardsContainer.add(crearCardEmpleado(emp));
        }
    }

    // Crea la Card de empleado.
    private Component crearCardEmpleado(Empleado empleado) {
        String urlImagen = (empleado.getImagen() != null && !empleado.getImagen().isEmpty())
                ? empleado.getImagen()
                : "https://cdn-icons-png.flaticon.com/512/847/847969.png";

        Image avatar = new Image(urlImagen, getTranslation("common.photo"));
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        avatar.getStyle().set("border-radius", "50%").set("object-fit", "cover").set("margin", "0 auto 1rem auto");

        Div avatarContainer = new Div(avatar);
        avatarContainer.getStyle().set("display", "flex").set("justify-content", "center");

        H3 nombre = new H3(safe(empleado.getNombre()));
        nombre.getStyle().set("margin", "0");

        Span puesto = new Span(getTranslation("manager.home.employee.position") + ": " + safe(empleado.getPuesto()));
        Span salario = new Span(getTranslation("manager.home.employee.salary") + ": " + safeMoney(empleado.getSalario()) + " €");

        puesto.getStyle().set("color", "gray");
        salario.getStyle().set("font-weight", "bold");

        Button btnEditar = new Button(getTranslation("common.edit"));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnEditar.getStyle().set("cursor", "pointer");
        btnEditar.addClickListener(e -> abrirEditorEmpleado(empleado));

        VerticalLayout content = new VerticalLayout(nombre, puesto, salario, btnEditar);
        content.setSpacing(false);
        content.setPadding(false);

        Div card = new Div(content);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 6px 20px rgba(0,0,0,0.15)")
                .set("width", "300px");

        return card;
    }

    private Component crearCardProducto(Producto producto) {
        String urlImagen = (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty())
                ? producto.getImagenUrl()
                : "https://cdn-icons-png.flaticon.com/512/3081/3081986.png";

        Image img = new Image(urlImagen, getTranslation("manager.home.product.imageAlt"));
        img.setWidth("100%");
        img.setHeight("140px");
        img.getStyle().set("border-radius", "12px").set("object-fit", "cover").set("margin-bottom", "1rem");

        H3 nombre = new H3(safe(producto.getNombre()));
        nombre.getStyle().set("margin", "0 0 0.5rem 0").set("font-size", "1.1rem");

        Span precio = new Span((producto.getPrecio() != null ? producto.getPrecio().toString() : "-") + " €");
        precio.getStyle().set("font-weight", "bold").set("color", "#27ae60").set("font-size", "1.2rem");

        String textoIngredientes = producto.getIngredientes() != null ? producto.getIngredientes() : getTranslation("manager.home.product.noSpecs");
        Paragraph ingredientes = new Paragraph("🥗 " + textoIngredientes);
        ingredientes.getStyle().set("font-size", "0.85rem").set("color", "#7f8c8d").set("margin", "0.5rem 0");
        ingredientes.getStyle().set("display", "-webkit-box");
        ingredientes.getStyle().set("-webkit-line-clamp", "2");
        ingredientes.getStyle().set("-webkit-box-orient", "vertical");
        ingredientes.getStyle().set("overflow", "hidden");

        Button btnEditar = new Button(getTranslation("common.edit"), e -> abrirEditorProducto(producto));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnEditar.getStyle().set("cursor", "pointer");
        btnEditar.setWidthFull();

        VerticalLayout content = new VerticalLayout(img, nombre, precio, ingredientes, btnEditar);
        content.setSpacing(false);
        content.setPadding(false);

        Div card = new Div(content);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "1rem")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("width", "250px");

        return card;
    }

    // ==========================
    // DIALOG EMPLEADO
    // ==========================
    private void abrirEditorEmpleado(Empleado empleado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(empleado.getId() == null
                ? getTranslation("manager.home.employee.dialog.newTitle")
                : getTranslation("manager.home.employee.dialog.editTitle"));

        FormLayout formLayout = new FormLayout();

        TextField nombreField = new TextField(getTranslation("manager.home.employee.field.name"));
        TextField puestoField = new TextField(getTranslation("manager.home.employee.field.position"));
        BigDecimalField salarioField = new BigDecimalField(getTranslation("manager.home.employee.field.salary"));
        TextField imagenField = new TextField(getTranslation("manager.home.employee.field.photoUrl"));

        formLayout.add(nombreField, puestoField, salarioField, imagenField);

        Binder<Empleado> binder = new Binder<>(Empleado.class);
        binder.forField(nombreField).bind(Empleado::getNombre, Empleado::setNombre);
        binder.forField(puestoField).bind(Empleado::getPuesto, Empleado::setPuesto);
        binder.forField(salarioField).bind(Empleado::getSalario, Empleado::setSalario);
        binder.forField(imagenField).bind(Empleado::getImagen, Empleado::setImagen);
        binder.readBean(empleado);

        Button guardar = new Button(getTranslation("common.save"), e -> {
            try {
                binder.writeBean(empleado);
                empleado.setRestaurante(restauranteActual);
                empleadoRepository.save(empleado);
                refrescarContenido();
                dialog.close();
                Notification.show(getTranslation("manager.home.employee.saved"));
            } catch (Exception ex) {
                Notification.show(getTranslation("common.saveError"));
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("cursor", "pointer");

        Button cancelar = new Button(getTranslation("common.cancel"), e -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        dialog.getFooter().add(cancelar, guardar);
        dialog.add(formLayout);
        dialog.open();
    }

    // ==========================
    // DIALOG PRODUCTO
    // ==========================
    private void abrirEditorProducto(Producto producto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(producto.getId() == null
                ? getTranslation("manager.home.product.dialog.newTitle")
                : getTranslation("manager.home.product.dialog.editTitle"));

        dialog.setWidth("720px");
        dialog.setHeight("auto");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        TextField nombreField = new TextField(getTranslation("manager.home.product.field.name"));
        nombreField.setWidthFull();

        BigDecimalField precioField = new BigDecimalField(getTranslation("manager.home.product.field.price"));
        precioField.setWidthFull();

        TextArea descripcionField = new TextArea(getTranslation("manager.home.product.field.description"));
        descripcionField.setWidthFull();
        descripcionField.setMinHeight("110px");

        TextArea ingredientesField = new TextArea(getTranslation("manager.home.product.field.ingredients"));
        ingredientesField.setWidthFull();
        ingredientesField.setMinHeight("110px");
        ingredientesField.setPlaceholder(getTranslation("manager.home.product.ingredients.placeholder"));
        ingredientesField.setHelperText(getTranslation("manager.home.product.ingredients.helper"));

        Span labelImg = new Span(getTranslation("manager.home.product.imageBlock.label"));
        labelImg.getStyle().set("font-weight", "600");

        VerticalLayout imageBlock = new VerticalLayout();
        imageBlock.setPadding(false);
        imageBlock.setSpacing(false);
        imageBlock.setWidthFull();
        imageBlock.getStyle().set("row-gap", "0.4rem");

        Image preview = new Image();
        preview.setVisible(false);
        preview.setWidthFull();
        preview.setHeight("260px");
        preview.getStyle()
                .set("border-radius", "14px")
                .set("background", "rgba(0,0,0,0.04)")
                .set("object-fit", "contain");

        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isBlank()) {
            preview.setSrc(producto.getImagenUrl());
            preview.setVisible(true);
        }

        final String[] imagenUrlSubida = { producto.getImagenUrl() };
        UI ui = UI.getCurrent();

        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveProductImage(
                            new ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenUrlSubida[0] = savedUrl;

                    if (ui != null) {
                        ui.access(() -> {
                            preview.setSrc(savedUrl);
                            preview.setVisible(true);
                        });
                    }
                })
        );

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024);
        upload.setDropLabel(new Span(getTranslation("manager.home.upload.dropLabel")));
        upload.setWidthFull();
        upload.getStyle().set("cursor", "pointer");

        imageBlock.add(labelImg, upload, preview);

        formLayout.add(nombreField, precioField);
        formLayout.add(descripcionField, ingredientesField);

        formLayout.add(imageBlock);
        formLayout.setColspan(imageBlock, 2);

        Binder<Producto> binder = new Binder<>(Producto.class);

        binder.forField(nombreField)
                .asRequired(getTranslation("validation.required.name"))
                .bind(Producto::getNombre, Producto::setNombre);

        binder.forField(precioField)
                .asRequired(getTranslation("validation.required.price"))
                .bind(Producto::getPrecio, Producto::setPrecio);

        binder.forField(descripcionField)
                .bind(Producto::getDescripcion, Producto::setDescripcion);

        binder.forField(ingredientesField)
                .bind(Producto::getIngredientes, Producto::setIngredientes);

        binder.readBean(producto);

        Button guardar = new Button(getTranslation("manager.home.product.actions.save"), e -> {
            try {
                binder.writeBean(producto);
                producto.setRestaurante(restauranteActual);

                if (producto.getId() == null && (imagenUrlSubida[0] == null || imagenUrlSubida[0].isBlank())) {
                    Notification.show(getTranslation("manager.home.product.imageRequired"));
                    return;
                }

                if (imagenUrlSubida[0] != null && !imagenUrlSubida[0].isBlank()) {
                    producto.setImagenUrl(imagenUrlSubida[0]);
                }

                productoService.guardar(producto);
                refrescarContenido();
                dialog.close();
                Notification.show(getTranslation("manager.home.product.saved"));
            } catch (Exception ex) {
                Notification.show(getTranslation("manager.home.product.saveError") + ": " + ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("cursor", "pointer");

        Button cancelar = new Button(getTranslation("common.cancel"), e -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        dialog.add(formLayout);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    // ==========================
    // DIALOG OFERTA / MENÚ
    // ==========================
    private void abrirEditorOferta(Producto oferta) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(oferta.getId() == null
                ? getTranslation("manager.home.offer.dialog.newTitle")
                : getTranslation("manager.home.offer.dialog.editTitle"));

        dialog.setWidth("600px");

        FormLayout form = new FormLayout();

        TextField nombre = new TextField(getTranslation("manager.home.offer.field.name"));
        BigDecimalField precio = new BigDecimalField(getTranslation("manager.home.offer.field.price"));
        TextArea descripcion = new TextArea(getTranslation("manager.home.offer.field.description"));

        List<Producto> platosDisponibles = productoService.productosDelRestaurante(restauranteActual)
                .stream().filter(p -> !p.isEsMenu()).toList();

        MultiSelectComboBox<Producto> selectorPlatos = new MultiSelectComboBox<>(getTranslation("manager.home.offer.field.items"));
        selectorPlatos.setItems(platosDisponibles);
        selectorPlatos.setItemLabelGenerator(Producto::getNombre);
        selectorPlatos.setPlaceholder(getTranslation("manager.home.offer.items.placeholder"));

        if (oferta.getItemsMenu() != null) {
            selectorPlatos.select(oferta.getItemsMenu());
        }

        Span labelImg = new Span(getTranslation("manager.home.product.imageBlock.label"));
        labelImg.getStyle().set("font-weight", "600");

        VerticalLayout imageBlock = new VerticalLayout();
        imageBlock.setPadding(false);
        imageBlock.setSpacing(false);
        imageBlock.setWidthFull();
        imageBlock.getStyle().set("row-gap", "0.4rem");

        Image preview = new Image();
        preview.setVisible(false);
        preview.setWidthFull();
        preview.setHeight("260px");
        preview.getStyle()
                .set("border-radius", "14px")
                .set("background", "rgba(0,0,0,0.04)")
                .set("object-fit", "contain");

        if (oferta.getImagenUrl() != null && !oferta.getImagenUrl().isBlank()) {
            preview.setSrc(oferta.getImagenUrl());
            preview.setVisible(true);
        }

        final String[] imagenUrlSubida = { oferta.getImagenUrl() };
        UI ui = UI.getCurrent();

        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveProductImage(
                            new ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenUrlSubida[0] = savedUrl;

                    if (ui != null) {
                        ui.access(() -> {
                            preview.setSrc(savedUrl);
                            preview.setVisible(true);
                        });
                    }
                })
        );

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024);
        upload.setDropLabel(new Span(getTranslation("manager.home.upload.dropLabel")));
        upload.setWidthFull();
        upload.getStyle().set("cursor", "pointer");

        imageBlock.add(labelImg, upload, preview);

        form.add(nombre, precio, descripcion, selectorPlatos, imageBlock);

        Binder<Producto> binder = new Binder<>(Producto.class);
        binder.forField(nombre).asRequired(getTranslation("validation.required.name")).bind(Producto::getNombre, Producto::setNombre);
        binder.forField(precio).asRequired(getTranslation("validation.required.price")).bind(Producto::getPrecio, Producto::setPrecio);
        binder.forField(descripcion).bind(Producto::getDescripcion, Producto::setDescripcion);
        binder.readBean(oferta);

        Button guardar = new Button(getTranslation("manager.home.offer.actions.save"), e -> {
            try {
                binder.writeBean(oferta);
                oferta.setRestaurante(restauranteActual);
                oferta.setEsMenu(true);

                if (imagenUrlSubida[0] != null && !imagenUrlSubida[0].isBlank()) {
                    oferta.setImagenUrl(imagenUrlSubida[0]);
                } else if (oferta.getId() == null) {
                    Notification.show(getTranslation("manager.home.offer.recommendImage"));
                }

                oferta.setItemsMenu(new ArrayList<>(selectorPlatos.getSelectedItems()));

                if (oferta.getItemsMenu() != null && !oferta.getItemsMenu().isEmpty()) {
                    StringBuilder resumen = new StringBuilder(getTranslation("manager.home.offer.includesPrefix") + " ");
                    oferta.getItemsMenu().forEach(p -> resumen.append(p.getNombre()).append(", "));
                    oferta.setIngredientes(resumen.toString());
                }

                productoService.guardar(oferta);
                refrescarContenido();
                dialog.close();
                Notification.show(getTranslation("manager.home.offer.saved"));
            } catch (Exception ex) {
                Notification.show(getTranslation("common.error") + ": " + ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.getStyle().set("cursor", "pointer");

        Button cancelar = new Button(getTranslation("common.cancel"), event -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        dialog.add(form);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }

    // ==========================
    // Gestión / Estadísticas
    // ==========================
    private void renderGestionRestaurante() {
        cardsContainer.setWidthFull();

        List<Pedido> pedidosHoy = pedidoService.obtenerPedidosDeHoy(restauranteActual);
        BigDecimal totalVentas = pedidoService.calcularVentasHoy(restauranteActual);
        int numeroPedidos = pedidosHoy.size();

        BigDecimal ticketMedio = (numeroPedidos > 0)
                ? totalVentas.divide(BigDecimal.valueOf(numeroPedidos), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.getStyle().set("flex-wrap", "wrap");

        // Editor restaurante
        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setWidth("450px");
        editorLayout.getStyle().set("background", "white");
        editorLayout.getStyle().set("border-radius", "16px");
        editorLayout.getStyle().set("padding", "20px");
        editorLayout.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)");

        H3 tituloEditor = new H3(getTranslation("manager.home.management.restaurantData"));

        TextField nombre = new TextField(getTranslation("manager.home.management.tradeName"));
        TextField direccion = new TextField(getTranslation("manager.home.management.address"));
        TextField telefono = new TextField(getTranslation("manager.home.management.phone"));
        TextField horario = new TextField(getTranslation("manager.home.management.hours"));
        TextField aforo = new TextField(getTranslation("manager.home.management.capacity"));

        Span labelImg = new Span(getTranslation("manager.home.management.logoLabel"));
        labelImg.getStyle().set("font-size", "0.9rem").set("color", "gray");

        Image preview = new Image();
        preview.setWidth("100%");
        preview.setHeight("150px");
        preview.getStyle().set("object-fit", "cover").set("border-radius", "8px").set("background", "#f0f0f0");

        if (restauranteActual.getImagenUrl() != null && !restauranteActual.getImagenUrl().isBlank()) {
            preview.setSrc(restauranteActual.getImagenUrl());
        }

        final String[] nuevaImagen = { restauranteActual.getImagenUrl() };

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            if (bytes == null || bytes.length == 0) return;
            String url = fileStorageService.saveProductImage(new ByteArrayInputStream(bytes), metadata.fileName());
            nuevaImagen[0] = url;
            getUI().ifPresent(ui -> ui.access(() -> preview.setSrc(url)));
        }));
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span(getTranslation("manager.home.management.changePhoto")));
        upload.getStyle().set("cursor", "pointer");

        Binder<Restaurante> binder = new Binder<>(Restaurante.class);
        binder.forField(nombre).asRequired(getTranslation("validation.required.name")).bind(Restaurante::getNombre, Restaurante::setNombre);
        binder.forField(direccion).asRequired(getTranslation("validation.required.address")).bind(Restaurante::getDireccion, Restaurante::setDireccion);
        binder.forField(telefono).bind(Restaurante::getTelefono, Restaurante::setTelefono);
        binder.forField(horario).bind(Restaurante::getHorario, Restaurante::setHorario);
        binder.forField(aforo)
                .withConverter(new StringToIntegerConverter(getTranslation("validation.mustBeNumber")))
                .bind(Restaurante::getAforo, Restaurante::setAforo);

        binder.readBean(restauranteActual);

        Button btnGuardarDatos = new Button(getTranslation("manager.home.management.updateData"), e -> {
            try {
                binder.writeBean(restauranteActual);
                if (nuevaImagen[0] != null) restauranteActual.setImagenUrl(nuevaImagen[0]);

                restauranteRepository.save(restauranteActual);
                Notification.show(getTranslation("manager.home.management.updated"));

                UI.getCurrent().getPage().reload();
            } catch (Exception ex) {
                Notification.show(getTranslation("common.error") + ": " + ex.getMessage());
            }
        });
        btnGuardarDatos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardarDatos.setWidthFull();
        btnGuardarDatos.getStyle().set("cursor", "pointer");

        editorLayout.add(tituloEditor, preview, upload, nombre, direccion, telefono, horario, aforo, btnGuardarDatos);

        // Dashboard
        VerticalLayout statsLayout = new VerticalLayout();
        statsLayout.getStyle().set("flex", "1");
        statsLayout.setSpacing(true);

        HorizontalLayout headerStats = new HorizontalLayout();
        headerStats.setWidthFull();
        headerStats.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerStats.setAlignItems(Alignment.CENTER);

        H3 tituloStats = new H3(getTranslation("manager.home.management.dashboard"));
        Button btnCerrarCaja = new Button(getTranslation("manager.home.management.closeCash"), new Icon("lumo", "checkmark"));
        btnCerrarCaja.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        btnCerrarCaja.getStyle().set("cursor", "pointer");

        btnCerrarCaja.addClickListener(e -> {
            Dialog reporte = new Dialog();
            reporte.setHeaderTitle(getTranslation("manager.home.management.cashCloseTitle") + " - " + java.time.LocalDate.now());

            VerticalLayout layoutReporte = new VerticalLayout();
            layoutReporte.setSpacing(false);
            layoutReporte.add(new Paragraph(getTranslation("manager.home.management.restaurant") + ": " + restauranteActual.getNombre()));
            layoutReporte.add(new Hr());
            layoutReporte.add(new H3(getTranslation("manager.home.management.totalBilled") + ": " + totalVentas + " €"));
            layoutReporte.add(new Span(getTranslation("manager.home.management.ordersMade") + ": " + numeroPedidos));
            layoutReporte.add(new Span(getTranslation("manager.home.management.avgTicket") + ": " + ticketMedio + " €"));

            Button confirmar = new Button(getTranslation("manager.home.management.confirmClose"), ev -> {
                Notification.show(getTranslation("manager.home.management.cashClosedOk"));
                reporte.close();
            });
            confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            confirmar.getStyle().set("cursor", "pointer");

            Button cancelar = new Button(getTranslation("common.cancel"), ev -> reporte.close());
            cancelar.getStyle().set("cursor", "pointer");

            reporte.add(layoutReporte);
            reporte.getFooter().add(cancelar, confirmar);
            reporte.open();
        });

        headerStats.add(tituloStats, btnCerrarCaja);

        HorizontalLayout kpiRow = new HorizontalLayout();
        kpiRow.setWidthFull();
        kpiRow.getStyle().set("flex-wrap", "wrap");

        kpiRow.add(crearKpiCard(
                getTranslation("manager.home.management.kpi.salesToday"),
                totalVentas + " €",
                getTranslation("manager.home.management.kpi.dailyBalance"),
                "#2ecc71"
        ));
        kpiRow.add(crearKpiCard(
                getTranslation("manager.home.management.kpi.totalOrders"),
                String.valueOf(numeroPedidos),
                getTranslation("manager.home.management.kpi.today"),
                "#3498db"
        ));
        kpiRow.add(crearKpiCard(
                getTranslation("manager.home.management.kpi.avgTicket"),
                ticketMedio + " €",
                getTranslation("manager.home.management.kpi.average"),
                "#9b59b6"
        ));

        Div topVentasPanel = new Div();
        topVentasPanel.setWidthFull();
        topVentasPanel.getStyle().set("background", "rgba(255,255,255,0.9)");
        topVentasPanel.getStyle().set("border-radius", "12px");
        topVentasPanel.getStyle().set("padding", "20px");
        topVentasPanel.add(new H3(getTranslation("manager.home.management.topProductsTitle")));
        topVentasPanel.add(new Paragraph(getTranslation("manager.home.management.topProductsDesc")));

        statsLayout.add(headerStats, kpiRow, topVentasPanel);

        mainLayout.add(editorLayout, statsLayout);
        cardsContainer.add(mainLayout);
    }

    private Component crearKpiCard(String titulo, String valor, String subtexto, String colorBorde) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("30%");
        card.getStyle().set("background", "white");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.05)");
        card.getStyle().set("border-left", "5px solid " + colorBorde);

        Span t = new Span(titulo);
        t.getStyle().set("color", "gray").set("font-size", "0.9rem");

        Span v = new Span(valor);
        v.getStyle().set("font-size", "2rem").set("font-weight", "bold").set("color", "#2c3e50");

        Span s = new Span(subtexto);
        s.getStyle().set("font-size", "0.8rem").set("color", "#95a5a6");

        card.add(t, v, s);
        return card;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String safeMoney(BigDecimal v) {
        return v == null ? "-" : v.toString();
    }
}
