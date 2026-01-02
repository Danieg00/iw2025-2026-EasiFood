package com.easifood.app.views;

import com.easifood.app.model.Producto;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.easifood.app.model.Empleado;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.EmpleadoRepository;
import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.repository.RestauranteRepository;
import com.easifood.app.service.ProductoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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

import java.util.List;

@PageTitle("Área Gerente")
@Route("home-gerente")
//@RolesAllowed("ROLE_GERENTE")
@AnonymousAllowed
public class GerenteHomeView extends VerticalLayout {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProductoService productoService;
    private final FileStorageService fileStorageService;

    private FlexLayout cardsContainer;
    private Restaurante restauranteActual;

    private Tabs tabs;
    private Tab tabEmpleados;
    private Tab tabProductos;
    private Button btnNuevo;

    public GerenteHomeView(EmpleadoRepository empleadoRepository, UsuarioRepository usuarioRepository, RestauranteRepository restauranteRepository, ProductoService productoService, FileStorageService fileStorageService) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.productoService = productoService;
        this.fileStorageService = fileStorageService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        getStyle().set("background-image", "linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('https://images.unsplash.com/photo-1555396273-367ea4eb4db5?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        getStyle().set("background-attachment", "fixed");

        cargarDatosDelGerente();

        H1 titulo = new H1((restauranteActual != null ? restauranteActual.getNombre() : "Mi Restaurante"));
        titulo.getStyle().set("color", "white").set("text-shadow", "0 2px 4px rgba(0,0,0,0.5)").set("margin-bottom", "0");

        tabEmpleados = new Tab("Empleados");
        tabProductos = new Tab("Productos / Carta");

        tabs = new Tabs(tabEmpleados, tabProductos);
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

        add(titulo, navigationBar, btnNuevo, cardsContainer);
    }
    private void actualizarBotonNuevo() {
        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            btnNuevo.setText("Nuevo Empleado");
        } else {
            btnNuevo.setText("Nuevo Producto");
        }
    }

    private void accionBotonNuevo() {
        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            abrirEditorEmpleado(new Empleado());
        } else {
            abrirEditorProducto(new Producto());
        }
    }

    private void refrescarContenido() {
        cardsContainer.removeAll();
        if (restauranteActual == null) {
            cardsContainer.add(new Span("No hay datos de restaurante."));
            return;
        }

        if (tabs.getSelectedTab().equals(tabEmpleados)) {
            refrescarEmpleados();
        } else {
            refrescarProductos();
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

    //Abre un Diálogo con un Formulario (Grid de campos) para editar/crear.
    private void abrirEditorEmpleado(Empleado empleado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(empleado.getId() == null ? "Nuevo Empleado" : "Editar Empleado");

        // Formulario
        FormLayout formLayout = new FormLayout();
        TextField nombreField = new TextField("Nombre");
        TextField puestoField = new TextField("Puesto");
        BigDecimalField salarioField = new BigDecimalField("Salario");
        TextField imagenField = new TextField("URL Foto");

        formLayout.add(nombreField, puestoField, salarioField, imagenField);

        Binder<Empleado> binder = new Binder<>(Empleado.class);

        // Vinculación manual para seguridad
        binder.forField(nombreField).bind(Empleado::getNombre, Empleado::setNombre);
        binder.forField(puestoField).bind(Empleado::getPuesto, Empleado::setPuesto);
        binder.forField(salarioField).bind(Empleado::getSalario, Empleado::setSalario);
        binder.forField(imagenField).bind(Empleado::getImagen, Empleado::setImagen);
        binder.readBean(empleado);

        Button guardar = new Button("Guardar", e -> {
            try {
                binder.writeBean(empleado);
                empleado.setRestaurante(restauranteActual); // Asegurar relación
                empleadoRepository.save(empleado);
                refrescarContenido();
                dialog.close();
                Notification.show("Empleado guardado");
            } catch (Exception ex) {
                Notification.show("Error al guardar datos");
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Cancelar", e->dialog.close()), guardar);
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

        // ==========================
        // ✅ BLOQUE IMAGEN (upload + preview) SIN DESCUIDRAR
        // ==========================
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
        UI ui = UI.getCurrent();

        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveProductImage(
                            new java.io.ByteArrayInputStream(bytes),
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
        upload.setMaxFileSize(3 * 1024 * 1024); // 3MB
        upload.setDropLabel(new Span("Arrastra la imagen aquí o pulsa para seleccionar"));
        upload.setWidthFull();

        // ✅ Montamos el bloque: label + upload + preview
        imageBlock.add(labelImg, upload, preview);

        // ==========================
        // ✅ AÑADIR AL FORM (y hacemos que imagenBlock ocupe 2 columnas)
        // ==========================
        formLayout.add(nombreField, precioField);
        formLayout.add(descripcionField, ingredientesField);

        formLayout.add(imageBlock);
        formLayout.setColspan(imageBlock, 2); // 👈 CLAVE: ocupa el ancho completo (2 columnas)

        // ==========================
        // BINDER
        // ==========================
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
}