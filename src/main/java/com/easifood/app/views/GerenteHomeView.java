package com.easifood.app.views;

import com.easifood.app.model.*;
import com.easifood.app.service.PedidoService;
import com.easifood.app.service.UsuarioService;
import com.easifood.app.service.EmpleadoService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.easifood.app.repository.EmpleadoRepository;
import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.repository.RestauranteRepository;
import com.easifood.app.service.ProductoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.easifood.app.service.FileStorageService;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.component.UI;
import java.io.ByteArrayInputStream;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Área Gerente")
@Route("home-gerente")
@RolesAllowed("ROLE_GERENTE")
//@AnonymousAllowed
public class GerenteHomeView extends VerticalLayout {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProductoService productoService;
    private final FileStorageService fileStorageService;
    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final EmpleadoService empleadoService;

    private FlexLayout cardsContainer;
    private Restaurante restauranteActual;
    private Gerente gerenteActual;

    private Tabs tabs;
    private Tab tabEmpleados;
    private Tab tabProductos;
    private Tab tabOfertas;
    private Tab tabGestion;

    private Button btnNuevo;

    public GerenteHomeView(EmpleadoRepository empleadoRepository, UsuarioRepository usuarioRepository,
                           RestauranteRepository restauranteRepository, ProductoService productoService,
                           FileStorageService fileStorageService, UsuarioService usuarioService, PedidoService pedidoService,
                           EmpleadoService empleadoService) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.productoService = productoService;
        this.fileStorageService = fileStorageService;
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.empleadoService = empleadoService;

        setWidthFull();
        getStyle().set("min-height", "100vh");
        setPadding(true);
        setSpacing(true);

        getStyle().set("background-image", "linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        getStyle().set("background-attachment", "fixed");

        cargarDatosDelGerente();

        if (restauranteActual == null) {
            Notification.show("No se ha encontrado información del restaurante.");
        }

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

        String nombreMostrar = (gerenteActual != null) ? gerenteActual.getNombre() : "Gerente";
        Span nombreSpan = new Span(nombreMostrar);
        nombreSpan.getStyle().set("color", "white").set("font-weight", "bold");

        Avatar avatar = new Avatar(nombreMostrar);
        if (gerenteActual != null && gerenteActual.getImagen() != null) {
            avatar.setImage(gerenteActual.getImagen());
        }
        avatar.getStyle().set("cursor", "pointer");

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem("Mi Perfil", e -> getUI().ifPresent(ui -> ui.navigate("perfil")));
        menu.addItem("Cerrar Sesión", e -> usuarioService.logout());

        perfilGerente.add(nombreSpan, avatar);
        topRow.add(logoApp, perfilGerente);

        Div bottomRow = new Div();
        bottomRow.setWidthFull();
        bottomRow.getStyle().set("text-align", "left"); // Alineación a la derecha
        bottomRow.getStyle().set("margin-top", "-10px");

        String nombreRest = (restauranteActual != null) ? restauranteActual.getNombre() : "Sin Restaurante";
        H2 nombreRestaurante = new H2(nombreRest);
        nombreRestaurante.getStyle().set("margin", "0");
        nombreRestaurante.getStyle().set("color", "#e0e0e0"); // Gris muy claro
        nombreRestaurante.getStyle().set("font-size", "1.8rem");
        nombreRestaurante.getStyle().set("text-shadow", "0 2px 4px rgba(0,0,0,0.5)");

        bottomRow.add(nombreRestaurante);
        headerContainer.add(topRow, bottomRow);

        //H1 titulo = new H1((restauranteActual != null ? restauranteActual.getNombre() : "Mi Restaurante"));
        //titulo.getStyle().set("color", "white").set("text-shadow", "0 2px 4px rgba(0,0,0,0.5)").set("margin-bottom", "0");

        tabEmpleados = new Tab("Empleados");
        tabProductos = new Tab("Productos / Carta");
        tabOfertas = new Tab("Ofertas y Menús");
        tabGestion = new Tab("Gestión / Estadísticas");

        tabs = new Tabs(tabEmpleados, tabProductos, tabOfertas, tabGestion);
        tabs.getStyle().set("background", "transparent");
        // Color del texto de las pestañas
        Div navigationBar = new Div(tabs);
        navigationBar.setWidth("fit-content");

        navigationBar.getStyle().set("margin", "1rem 0 1.5rem 0");
        navigationBar.getStyle().set("background-color", "rgba(255, 255, 255, 0.8)");
        navigationBar.getStyle().set("border-radius", "50px");
        navigationBar.getStyle().set("padding", "4px 8px");
        navigationBar.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.2)");
        // Listener para cambio de pestaña
        tabs.addSelectedChangeListener(event -> {
            actualizarBotonNuevo();
            refrescarContenido();
        });

        btnNuevo = new Button("Nuevo Empleado"); // Texto inicial
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNuevo.getStyle().set("cursor", "pointer");
        btnNuevo.addClickListener(e -> accionBotonNuevo());

        cardsContainer = new FlexLayout();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.setJustifyContentMode(JustifyContentMode.START);
        cardsContainer.getStyle().set("gap", "20px");
        cardsContainer.setWidthFull();

        refrescarContenido();

        add(headerContainer, navigationBar, btnNuevo, cardsContainer);
    }
    private void actualizarBotonNuevo() {
        btnNuevo.setVisible(true);
        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            btnNuevo.setText("Nuevo Empleado");
        } else if (tabs.getSelectedTab().equals(tabProductos)) {
            btnNuevo.setText("Nuevo Producto");
        }else if (tabs.getSelectedTab().equals(tabOfertas)) {
            btnNuevo.setText("Crear Oferta / Menú");
        } else {
            //No se ve en ventana Gestion
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
            cardsContainer.add(new Span("No hay datos de restaurante."));
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
                productos.stream().filter(p -> p.isEsMenu()).forEach(p -> cardsContainer.add(crearCardProducto(p)));
            }
        }
    }
    //Recupera el Usuario logueado, verifica que sea Gerente y obtiene su Restaurante.
    private void cargarDatosDelGerente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //Datos de prueba falsos para hacer pruebas
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Notification.show("MODO PRUEBA: Cargando primer restaurante de la BD", 3000, Notification.Position.TOP_CENTER);

            // Carga el primer restaurante que encuentre en tu base de datos
            this.restauranteActual = restauranteRepository.findAll().stream().findFirst().orElse(null);
            return;
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);

        if (usuario instanceof Gerente) {
            this.restauranteActual = ((Gerente) usuario).getRestaurante();
        }
    }

    //Busca los empleados en BD y regenera las tarjetas.
    private void refrescarEmpleados() {
        cardsContainer.removeAll();
        if (restauranteActual == null) {
            cardsContainer.add(new Span("No tienes un restaurante asignado."));
            return;
        }
        List<Empleado> empleados = empleadoRepository.findByRestaurante(restauranteActual);
        for (Empleado emp : empleados) {
            cardsContainer.add(crearCardEmpleado(emp));
        }
    }

    //Crea el componente visual "Card" para un empleado.
    private Component crearCardEmpleado(Empleado empleado) {
        // Datos
        String urlImagen = (empleado.getImagen() != null && !empleado.getImagen().isEmpty())
                ? empleado.getImagen() : "https://cdn-icons-png.flaticon.com/512/847/847969.png";
        Image avatar = new Image(urlImagen, "Foto");
        avatar.setWidth("80px"); avatar.setHeight("80px");
        avatar.getStyle().set("border-radius", "50%").set("object-fit", "cover").set("margin", "0 auto 1rem auto");
        Div avatarContainer = new Div(avatar);
        avatarContainer.getStyle().set("display", "flex").set("justify-content", "center");

        H3 nombre = new H3(empleado.getNombre());
        nombre.getStyle().set("margin", "0");

        Span puesto = new Span("Puesto: " + empleado.getPuesto());
        Span salario = new Span("Salario: " + empleado.getSalario() + " €");

        puesto.getStyle().set("color", "gray");
        salario.getStyle().set("font-weight", "bold");

        // Botón Editar
        Button btnEditar = new Button("Editar");
        btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnEditar.addClickListener(e -> abrirEditorEmpleado(empleado));

        // Layout vertical dentro de la tarjeta
        VerticalLayout content = new VerticalLayout(nombre, puesto, salario, btnEditar);
        content.setSpacing(false);
        content.setPadding(false);

        // Contenedor principal de la tarjeta (Estilos solicitados)
        Div card = new Div(content);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 6px 20px rgba(0,0,0,0.15)")
                .set("width", "300px"); // Ancho fijo o min-width para las tarjetas

        return card;
    }

    private void refrescarProductos() {
        List<Producto> productos = productoService.productosDelRestaurante(restauranteActual);
        for (Producto prod : productos) {
            cardsContainer.add(crearCardProducto(prod));
        }
    }

    private Component crearCardProducto(Producto producto) {
        // Imagen del plato
        String urlImagen = (producto.getImagenUrl() != null && !producto.getImagenUrl().isEmpty())
                ? producto.getImagenUrl()
                : "https://cdn-icons-png.flaticon.com/512/3081/3081986.png"; // Icono comida default

        Image img = new Image(urlImagen, "Plato");
        img.setWidth("100%");
        img.setHeight("140px");
        img.getStyle().set("border-radius", "12px").set("object-fit", "cover").set("margin-bottom", "1rem");

        // Datos
        H3 nombre = new H3(producto.getNombre());
        nombre.getStyle().set("margin", "0 0 0.5rem 0").set("font-size", "1.1rem");

        Span precio = new Span(producto.getPrecio() + " €");
        precio.getStyle().set("font-weight", "bold").set("color", "#27ae60").set("font-size", "1.2rem");

        // Ingredientes (mostramos truncado si es muy largo)
        String textoIngredientes = producto.getIngredientes() != null ? producto.getIngredientes() : "Sin especificaciones";
        Paragraph ingredientes = new Paragraph("🥗 " + textoIngredientes);
        ingredientes.getStyle().set("font-size", "0.85rem").set("color", "#7f8c8d").set("margin", "0.5rem 0");
        // Limitar a 2 líneas visualmente
        ingredientes.getStyle().set("display", "-webkit-box");
        ingredientes.getStyle().set("-webkit-line-clamp", "2");
        ingredientes.getStyle().set("-webkit-box-orient", "vertical");
        ingredientes.getStyle().set("overflow", "hidden");

        Button btnEditar = new Button("Editar", e -> abrirEditorProducto(producto));
        btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
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
                .set("width", "250px"); // Tarjeta de producto

        return card;
    }

    // Abre un Diálogo con un Formulario (Grid de campos) para editar/crear.
    private void abrirEditorEmpleado(Empleado empleado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(empleado.getId() == null ? "Nuevo Empleado" : "Editar Empleado");
        dialog.setWidth("600px");

        // Formulario
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        TextField nombreField = new TextField("Nombre");
        TextField apellidosField = new TextField("Apellidos");
        TextField puestoField = new TextField("Puesto");
        TextField correoField = new TextField("Correo Electrónico");
        com.vaadin.flow.component.textfield.PasswordField contraField = new com.vaadin.flow.component.textfield.PasswordField("Contraseña");
        if (empleado.getId() != null) {
            contraField.setHelperText("Dejar vacía para no cambiarla si ya existe");
        }
        BigDecimalField salarioField = new BigDecimalField("Salario (€)");
        salarioField.addThemeVariants(com.vaadin.flow.component.textfield.TextFieldVariant.LUMO_ALIGN_RIGHT);

        Span labelImg = new Span("Foto de perfil");
        labelImg.getStyle().set("font-size", "0.9rem").set("color", "gray");

        Image preview = new Image();
        preview.setWidth("100px");
        preview.setHeight("100px");
        preview.getStyle().set("object-fit", "cover").set("border-radius", "50%").set("background", "#f0f0f0");

        if (empleado.getImagen() != null && !empleado.getImagen().isEmpty()) {
            preview.setSrc(empleado.getImagen());
        }

        // Variable auxiliar para guardar la URL de la imagen subida
        final String[] imagenUrlSubida = { empleado.getImagen() };

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            if (bytes == null || bytes.length == 0) return;

            // ✅ Empleado -> imagen de usuario (no producto)
            String url = fileStorageService.saveUserImage(
                    new ByteArrayInputStream(bytes),
                    metadata.fileName()
            );
            imagenUrlSubida[0] = url;

            // ✅ Actualizamos la previsualización
            getUI().ifPresent(ui -> ui.access(() -> {
                preview.setSrc(url);
                preview.setVisible(true);
            }));
        }));
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Subir foto..."));

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Subir foto..."));

        // Layout para la imagen (Upload + Preview)
        HorizontalLayout imageLayout = new HorizontalLayout(preview, upload);
        imageLayout.setAlignItems(Alignment.CENTER);

        formLayout.add(nombreField, apellidosField);
        formLayout.add(correoField, puestoField);
        formLayout.add(salarioField, contraField);

        formLayout.add(labelImg, imageLayout);
        formLayout.setColspan(labelImg, 2);
        formLayout.setColspan(imageLayout, 2);

        Binder<Empleado> binder = new Binder<>(Empleado.class);

        // Vinculación manual para seguridad
        binder.forField(nombreField).asRequired("El nombre es obligatorio").bind(Empleado::getNombre, Empleado::setNombre);
        binder.forField(apellidosField).asRequired("Los apellidos son obligatorios").bind(Empleado::getApellidos, Empleado::setApellidos);
        binder.forField(correoField).asRequired("El correo es obligatorio").bind(Empleado::getCorreo, Empleado::setCorreo);
        binder.forField(puestoField).asRequired("El puesto es obligatorio").bind(Empleado::getPuesto, Empleado::setPuesto);
        binder.forField(salarioField).asRequired("El salario es obligatorio").bind(Empleado::getSalario, Empleado::setSalario);
        binder.readBean(empleado);

        Button guardar = new Button("Guardar", e -> {
            try {
                binder.writeBean(empleado);

                // Imagen
                if (imagenUrlSubida[0] != null) {
                    empleado.setImagen(imagenUrlSubida[0]);
                }

                // Restaurante + Rol
                empleado.setRestaurante(restauranteActual);
                if (empleado.getRole() == null || empleado.getRole().isBlank()) {
                    empleado.setRole("ROLE_EMPLEADO"); // ✅ consistente con @RolesAllowed en EmpleadoView
                }

                // Password
                String pass = contraField.getValue();

                // Nuevo empleado: password obligatorio
                if (empleado.getId() == null && (pass == null || pass.isBlank())) {
                    Notification.show("La contraseña es obligatoria para nuevos empleados");
                    return;
                }

                // Edición: si no escribe pass, NO la tocamos
                if (empleado.getId() != null && (pass == null || pass.isBlank())) {
                    // dejamos la contraseña como está en BD
                    Empleado existente = empleadoRepository.findById(empleado.getId()).orElse(null);
                    if (existente != null) {
                        empleado.setContra(existente.getContra());
                    }
                } else if (pass != null && !pass.isBlank()) {
                    // ponemos en claro; EmpleadoService.guardar() la cifrará
                    empleado.setContra(pass);
                }

                // ✅ GUARDAR SIEMPRE por el service (cifra + normaliza)
                empleadoService.guardar(empleado);

                refrescarContenido();
                dialog.close();
                Notification.show("Empleado guardado correctamente");

            } catch (Exception ex) {
                Notification.show("Error al guardar: " + ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelar = new Button("Cancelar", e -> dialog.close());
        dialog.getFooter().add(cancelar, guardar);
        dialog.add(formLayout);
        dialog.open();
    }
    private void abrirEditorProducto(Producto producto) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(producto.getId() == null ? "Nuevo Plato" : "Editar Plato");
        dialog.setWidth("720px");
        dialog.setHeight("auto");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        // 2 columnas en pantallas normales, 1 en móvil
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        TextField nombreField = new TextField("Nombre del Plato");
        nombreField.setWidthFull();

        BigDecimalField precioField = new BigDecimalField("Precio (€)");
        precioField.setWidthFull();

        TextArea descripcionField = new TextArea("Descripción");
        descripcionField.setWidthFull();
        descripcionField.setMinHeight("110px");

        TextArea ingredientesField = new TextArea("Ingredientes (Lista)");
        ingredientesField.setWidthFull();
        ingredientesField.setMinHeight("110px");
        ingredientesField.setPlaceholder("Ej: Huevo, Harina, Leche, Cacahuetes...");
        ingredientesField.setHelperText("Separa los ingredientes por comas para filtrar alérgenos.");

        // Upload + review de imagen
        Span labelImg = new Span("Imagen del producto");
        labelImg.getStyle().set("font-weight", "600");

        // Contenedor del bloque imagen
        VerticalLayout imageBlock = new VerticalLayout();
        imageBlock.setPadding(false);
        imageBlock.setSpacing(false);
        imageBlock.setWidthFull();
        imageBlock.getStyle().set("row-gap", "0.4rem");

        // Preview grande y COMPLETO (sin recortar)
        Image preview = new Image();
        preview.setVisible(false);
        preview.setWidthFull();
        preview.setHeight("260px"); // alto fijo para que no “salte” el layout
        preview.getStyle()
                .set("border-radius", "14px")
                .set("background", "rgba(0,0,0,0.04)")
                .set("object-fit", "contain"); // ✅ para ver la imagen completa

        // Si el producto ya tiene imagen, mostrarla
        if (producto.getImagenUrl() != null && !producto.getImagenUrl().isBlank()) {
            preview.setSrc(producto.getImagenUrl());
            preview.setVisible(true);
        }

        final String[] imagenUrlSubida = { producto.getImagenUrl() };

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            if (bytes == null || bytes.length == 0) return;

            // ✅ Para empleados: guarda en carpeta usuarios (no productos)
            String url = fileStorageService.saveUserImage(
                    new ByteArrayInputStream(bytes),
                    metadata.fileName()
            );

            imagenUrlSubida[0] = url;

            // Actualizamos la previsualización
            getUI().ifPresent(currentUi -> currentUi.access(() -> {
                preview.setSrc(url);
                preview.setVisible(true);
            }));
        }));

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024); // 3MB
        upload.setDropLabel(new Span("Arrastra la imagen aquí o pulsa para seleccionar"));
        upload.setWidthFull();

        // ✅ Montamos el bloque: label + upload + preview
        imageBlock.add(labelImg, upload, preview);

        formLayout.add(nombreField, precioField);
        formLayout.add(descripcionField, ingredientesField);

        formLayout.add(imageBlock);
        formLayout.setColspan(imageBlock, 2); // 👈 CLAVE: ocupa el ancho completo (2 columnas)

        Binder<Producto> binder = new Binder<>(Producto.class);

        binder.forField(nombreField)
                .asRequired("El nombre es obligatorio")
                .bind(Producto::getNombre, Producto::setNombre);

        binder.forField(precioField)
                .asRequired("El precio es obligatorio")
                .bind(Producto::getPrecio, Producto::setPrecio);

        binder.forField(descripcionField)
                .bind(Producto::getDescripcion, Producto::setDescripcion);

        binder.forField(ingredientesField)
                .bind(Producto::getIngredientes, Producto::setIngredientes);

        binder.readBean(producto);

        Button guardar = new Button("Guardar Producto", e -> {
            try {
                binder.writeBean(producto);
                producto.setRestaurante(restauranteActual);

                // Imagen obligatoria solo en creación (si quieres)
                if (producto.getId() == null &&
                        (imagenUrlSubida[0] == null || imagenUrlSubida[0].isBlank())) {
                    Notification.show("Debes subir una imagen del producto");
                    return;
                }

                // Guardar imagen subida (si hay)
                if (imagenUrlSubida[0] != null && !imagenUrlSubida[0].isBlank()) {
                    producto.setImagenUrl(imagenUrlSubida[0]);
                }

                productoService.guardar(producto);
                refrescarContenido();
                dialog.close();
                Notification.show("Producto guardado correctamente");
            } catch (Exception ex) {
                Notification.show("Error al guardar producto: " + ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }
    private void abrirEditorOferta(Producto oferta) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(oferta.getId() == null ? "Nueva Oferta" : "Editar Oferta");
        dialog.setWidth("600px");

        FormLayout form = new FormLayout();

        TextField nombre = new TextField("Nombre de la Oferta (Ej: Pack Pareja)");
        BigDecimalField precio = new BigDecimalField("Precio del Pack (€)");
        TextArea descripcion = new TextArea("Descripción de la oferta");

        // Obtenemos solo los platos individuales para meter en el menú
        List<Producto> platosDisponibles = productoService.productosDelRestaurante(restauranteActual)
                .stream().filter(p -> !p.isEsMenu()).toList();

        MultiSelectComboBox<Producto> selectorPlatos = new MultiSelectComboBox<>("Incluye los siguientes platos:");
        selectorPlatos.setItems(platosDisponibles);
        selectorPlatos.setItemLabelGenerator(Producto::getNombre);
        selectorPlatos.setPlaceholder("Elige hamburguesas, bebidas, etc.");

        if (oferta.getItemsMenu() != null) {
            selectorPlatos.select(oferta.getItemsMenu());
        }

        Span labelImg = new Span("Imagen del producto");
        labelImg.getStyle().set("font-weight", "600");

        // Contenedor del bloque imagen
        VerticalLayout imageBlock = new VerticalLayout();
        imageBlock.setPadding(false);
        imageBlock.setSpacing(false);
        imageBlock.setWidthFull();
        imageBlock.getStyle().set("row-gap", "0.4rem");

        // Preview grande y COMPLETO (sin recortar)
        Image preview = new Image();
        preview.setVisible(false);
        preview.setWidthFull();
        preview.setHeight("260px");
        preview.getStyle()
                .set("border-radius", "14px")
                .set("background", "rgba(0,0,0,0.04)")
                .set("object-fit", "contain");

        // Si el producto ya tiene imagen, mostrarla
        if (oferta.getImagenUrl() != null && !oferta.getImagenUrl().isBlank()) {
            preview.setSrc(oferta.getImagenUrl());
            preview.setVisible(true);
        }

        final String[] imagenUrlSubida = { oferta.getImagenUrl() };

        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            if (bytes == null || bytes.length == 0) return;

            // ✅ Para empleados: guarda en carpeta usuarios (no productos)
            String url = fileStorageService.saveUserImage(
                    new ByteArrayInputStream(bytes),
                    metadata.fileName()
            );

            imagenUrlSubida[0] = url;

            // Actualizamos la previsualización
            getUI().ifPresent(currentUi -> currentUi.access(() -> {
                preview.setSrc(url);
                preview.setVisible(true);
            }));
        }));

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024); // 3MB
        upload.setDropLabel(new Span("Arrastra la imagen aquí o pulsa para seleccionar"));
        upload.setWidthFull();

        // ✅ Montamos el bloque: label + upload + preview
        imageBlock.add(labelImg, upload, preview);

        form.add(nombre, precio, descripcion, selectorPlatos, imageBlock);

        Binder<Producto> binder = new Binder<>(Producto.class);
        binder.forField(nombre).asRequired().bind(Producto::getNombre, Producto::setNombre);
        binder.forField(precio).asRequired().bind(Producto::getPrecio, Producto::setPrecio);
        binder.forField(descripcion).bind(Producto::getDescripcion, Producto::setDescripcion);
        binder.readBean(oferta);

        Button guardar = new Button("Guardar Oferta", e -> {
            try {
                binder.writeBean(oferta);
                oferta.setRestaurante(restauranteActual);
                oferta.setEsMenu(true);

                if (imagenUrlSubida[0] != null && !imagenUrlSubida[0].isBlank()) {
                    oferta.setImagenUrl(imagenUrlSubida[0]);
                } else if (oferta.getId() == null) {
                    Notification.show("Te recomendamos poner una foto a la oferta");
                }

                // Guardamos la relación
                oferta.setItemsMenu(new ArrayList<>(selectorPlatos.getSelectedItems()));

                if(oferta.getItemsMenu() != null && !oferta.getItemsMenu().isEmpty()){
                    StringBuilder resumen = new StringBuilder("Incluye: ");
                    oferta.getItemsMenu().forEach(p -> resumen.append(p.getNombre()).append(", "));
                    oferta.setIngredientes(resumen.toString());
                }

                productoService.guardar(oferta);
                refrescarContenido();
                dialog.close();
                Notification.show("Oferta creada correctamente");
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(form);
        dialog.getFooter().add(new Button("Cancelar", event -> dialog.close()), guardar);
        dialog.open();
    }

    // Estadisticas y datos del restaurante
    private void renderGestionRestaurante() {
        cardsContainer.setWidthFull();

        List<Pedido> pedidosHoy = pedidoService.obtenerPedidosDeHoy(restauranteActual);
        BigDecimal totalVentas = pedidoService.calcularVentasHoy(restauranteActual);
        int numeroPedidos = pedidosHoy.size();

        // Calcular ticket medio (evitando división por cero)
        BigDecimal ticketMedio = (numeroPedidos > 0)
                ? totalVentas.divide(BigDecimal.valueOf(numeroPedidos), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        // Usamos un layout horizontal para separar Editor vs Estadísticas
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.setAlignItems(Alignment.START);

        // Layout moviles
        mainLayout.getStyle().set("flex-wrap", "wrap");
        // Editor de restaurante
        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setWidth("450px");
        editorLayout.getStyle().set("background", "white");
        editorLayout.getStyle().set("border-radius", "16px");
        editorLayout.getStyle().set("padding", "20px");
        editorLayout.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)");

        H3 tituloEditor = new H3("Datos del Restaurante");

        TextField nombre = new TextField("Nombre Comercial");
        TextField direccion = new TextField("Dirección");
        TextField telefono = new TextField("Teléfono");
        TextField horario = new TextField("Horario");
        TextField aforo = new TextField("Aforo Máximo");

        Span labelImg = new Span("Logotipo / Imagen Principal");
        labelImg.getStyle().set("font-size", "0.9rem").set("color", "gray");

        Image preview = new Image();
        preview.setWidth("100%");
        preview.setHeight("150px");
        preview.getStyle().set("object-fit", "cover").set("border-radius", "8px").set("background", "#f0f0f0");

        if (restauranteActual.getImagenUrl() != null) {
            preview.setSrc(restauranteActual.getImagenUrl());
        }

        final String[] nuevaImagen = { restauranteActual.getImagenUrl() };
        Upload upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            if (bytes == null || bytes.length == 0) return;
            String url = fileStorageService.saveProductImage(new ByteArrayInputStream(bytes), metadata.fileName());
            nuevaImagen[0] = url;
            getUI().ifPresent(ui -> ui.access(() -> preview.setSrc(url)));
        }));
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Span("Cambiar foto..."));

        Binder<Restaurante> binder = new Binder<>(Restaurante.class);
        binder.forField(nombre).asRequired().bind(Restaurante::getNombre, Restaurante::setNombre);
        binder.forField(direccion).asRequired().bind(Restaurante::getDireccion, Restaurante::setDireccion);
        binder.forField(telefono).bind(Restaurante::getTelefono, Restaurante::setTelefono);
        binder.forField(horario).bind(Restaurante::getHorario, Restaurante::setHorario);
        binder.forField(aforo)
                .withConverter(new StringToIntegerConverter("Debe ser un número"))
                .bind(Restaurante::getAforo, Restaurante::setAforo);

        binder.readBean(restauranteActual);

        Button btnGuardarDatos = new Button("Actualizar Datos", e -> {
            try {
                binder.writeBean(restauranteActual);
                if(nuevaImagen[0] != null) restauranteActual.setImagenUrl(nuevaImagen[0]);

                restauranteRepository.save(restauranteActual);
                Notification.show("Datos del restaurante actualizados");

                // Recargar página para actualizar cabecera
                UI.getCurrent().getPage().reload();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage());
            }
        });
        btnGuardarDatos.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardarDatos.setWidthFull();

        editorLayout.add(tituloEditor, preview, upload, nombre, direccion, telefono, horario, aforo, btnGuardarDatos);

        // Dashboard de estadisticas
        VerticalLayout statsLayout = new VerticalLayout();
        statsLayout.getStyle().set("flex", "1");
        statsLayout.setSpacing(true);

        HorizontalLayout headerStats = new HorizontalLayout();
        headerStats.setWidthFull();
        headerStats.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerStats.setAlignItems(Alignment.CENTER);

        H3 tituloStats = new H3("Panel de Control");
        tituloStats.getStyle().set("margin", "0");

        Div cajaTitulo = new Div(tituloStats);
        cajaTitulo.getStyle().set("background", "rgba(255, 255, 255, 0.85)");
        cajaTitulo.getStyle().set("padding", "10px 20px");
        cajaTitulo.getStyle().set("border-radius", "10px");
        cajaTitulo.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");

        Button btnCerrarCaja = new Button("Cerrar Caja (Día)", new Icon("lumo", "checkmark"));
        btnCerrarCaja.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        Div cajaBoton = new Div(btnCerrarCaja);
        cajaBoton.getStyle().set("background", "rgba(255, 255, 255, 0.85)"); // Blanco semitransparente
        cajaBoton.getStyle().set("padding", "10px");
        cajaBoton.getStyle().set("border-radius", "10px");
        cajaBoton.getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.1)");
        btnCerrarCaja.addClickListener(e -> {
            // Creamos un diálogo de resumen usando los datos calculados al inicio
            Dialog reporte = new Dialog();
            reporte.setHeaderTitle("Cierre de Caja - " + java.time.LocalDate.now());

            VerticalLayout layoutReporte = new VerticalLayout();
            layoutReporte.setSpacing(false);
            layoutReporte.add(new Paragraph("Restaurante: " + restauranteActual.getNombre()));
            layoutReporte.add(new Hr()); // Línea separadora
            layoutReporte.add(new H3("Total Facturado: " + totalVentas + " €"));
            layoutReporte.add(new Span("Pedidos realizados: " + numeroPedidos));
            layoutReporte.add(new Span("Ticket medio: " + ticketMedio + " €"));

            Button confirmar = new Button("Confirmar Cierre", ev -> {
                // Aquí iría la llamada al backend para guardar el histórico de cierres
                Notification.show("Caja cerrada correctamente. Reporte generado.");
                reporte.close();
            });
            confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            reporte.add(layoutReporte);
            reporte.getFooter().add(new Button("Cancelar", ev -> reporte.close()), confirmar);
            reporte.open();
        });
        headerStats.add(cajaTitulo, cajaBoton);

        HorizontalLayout kpiRow = new HorizontalLayout();
        kpiRow.setWidthFull();
        kpiRow.getStyle().set("flex-wrap", "wrap");
        // Ventas Hoy
        kpiRow.add(crearKpiCard("Ventas Hoy", totalVentas + " €", "Balance diario", "#2ecc71"));
        // Pedidos
        kpiRow.add(crearKpiCard("Pedidos Totales", String.valueOf(numeroPedidos), "Hoy", "#3498db"));
        // Ticket Medio
        kpiRow.add(crearKpiCard("Ticket Medio", ticketMedio + " €", "Promedio", "#9b59b6"));

        // Gráfico simulado / Lista de top ventas
        Div topVentasPanel = new Div();
        topVentasPanel.setWidthFull();
        topVentasPanel.getStyle().set("background", "rgba(255,255,255,0.9)");
        topVentasPanel.getStyle().set("border-radius", "12px");
        topVentasPanel.getStyle().set("padding", "20px");
        topVentasPanel.add(new H3("Productos más vendidos (Estadística futura)"));
        topVentasPanel.add(new Paragraph("Aquí aparecerá el gráfico de barras comparando las ventas de Platos vs Menús."));

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
}