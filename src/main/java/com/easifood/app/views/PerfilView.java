package com.easifood.app.views;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.FileStorageService;
import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mi perfil")
@Route("perfil")
@RolesAllowed({"ROLE_CLIENTE", "ROLE_GERENTE"})
public class PerfilView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final AuthenticationContext authenticationContext;
    private final FileStorageService fileStorageService;

    private Usuario usuario;

    // UI foto
    private Image fotoPreview;
    private Avatar avatarFallback;
    private HorizontalLayout photoCenter;

    // Para restaurar/cambios
    private String imagenOriginal;
    private String imagenActual;

    // Binder
    private final Binder<Usuario> binderUsuario = new Binder<>(Usuario.class);
    private final Binder<Cliente> binderCliente = new Binder<>(Cliente.class);

    // Campos datos
    private TextField nombre;
    private TextField apellidos;
    private TextField direccion1;
    private TextField direccion2;

    // Seguridad (todo se guarda con UN botón)
    private EmailField correoEditable;
    private PasswordField passActual;
    private PasswordField passNueva;
    private PasswordField passRepetir;

    // Botón único
    private Button guardarUnico;
    private Button cancelar;

    public PerfilView(UsuarioService usuarioService,
                      AuthenticationContext authenticationContext,
                      FileStorageService fileStorageService) {

        this.usuarioService = usuarioService;
        this.authenticationContext = authenticationContext;
        this.fileStorageService = fileStorageService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        VerticalLayout page = new VerticalLayout();
        page.setWidthFull();
        page.setMaxWidth("980px");
        page.getStyle().set("margin", "0 auto");
        page.setPadding(false);
        page.setSpacing(true);

        page.add(buildTopBar());

        String correoAuth = authenticationContext.getPrincipalName().orElse(null);
        if (correoAuth == null) {
            page.add(new Paragraph("No se pudo obtener el usuario autenticado."));
            add(page);
            return;
        }

        usuario = usuarioService.findByCorreo(correoAuth);
        if (usuario == null) {
            page.add(new Paragraph("No se encontró el usuario en la base de datos."));
            add(page);
            return;
        }

        imagenOriginal = usuario.getImagen();
        imagenActual = imagenOriginal;

        H1 title = new H1("Mi perfil");
        title.getStyle().set("margin", "0.25rem 0 0.5rem 0");
        page.add(title);

        // ==========================
        // GRID: FOTO + DATOS
        // ==========================
        HorizontalLayout grid = new HorizontalLayout();
        grid.setWidthFull();
        grid.setSpacing(true);
        grid.setPadding(false);
        grid.setAlignItems(Alignment.START);

        VerticalLayout cardFoto = buildCardFoto(usuario);
        VerticalLayout cardDatos = buildCardDatos(usuario);

        grid.add(cardFoto, cardDatos);
        grid.setFlexGrow(1, cardDatos);

        page.add(grid);

        // ==========================
        // SEGURIDAD (correo + password)
        // ==========================
        page.add(buildCardSeguridad(usuario));

        // ==========================
        // BOTONES ÚNICOS (para TODO)
        // ==========================
        HorizontalLayout actions = buildBottomActions();
        page.add(actions);

        // ==========================
        // GERENTE: RESTAURANTE
        // ==========================
        if (usuario instanceof Gerente g) {
            page.add(buildCardRestaurante(g.getRestaurante()));
        }

        add(page);
    }

    // ==========================
    // TOP BAR
    // ==========================
    private Component buildTopBar() {
        Button volver = buildBackButton();
        Button logout = buildLogoutButton(authenticationContext);

        HorizontalLayout top = new HorizontalLayout(volver, logout);
        top.setWidthFull();
        top.setAlignItems(Alignment.CENTER);
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        return top;
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

        back.addClickListener(e -> UI.getCurrent().getPage().getHistory().back());
        return back;
    }

    private Button buildLogoutButton(AuthenticationContext authenticationContext) {
        Button logout = new Button("Cerrar sesión", e -> authenticationContext.logout());
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logout.getStyle().set("cursor", "pointer").set("font-weight", "600");
        return logout;
    }

    // ==========================
    // CARD BASE
    // ==========================
    private VerticalLayout buildCardBase(String title) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 h = new H3(title);
        h.getStyle().set("margin", "0");
        card.add(h, new Hr());
        return card;
    }

    // ==========================
    // FOTO / AVATAR + UPLOAD
    // ==========================
    private VerticalLayout buildCardFoto(Usuario u) {
        VerticalLayout card = buildCardBase("Foto");
        card.setWidth("min(340px, 100%)");

        fotoPreview = new Image();
        fotoPreview.setWidth("180px");
        fotoPreview.setHeight("180px");
        fotoPreview.getStyle()
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("background", "rgba(0,0,0,0.04)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        avatarFallback = new Avatar(safe(u.getNombre()));
        avatarFallback.setWidth("180px");
        avatarFallback.setHeight("180px");

        photoCenter = new HorizontalLayout();
        photoCenter.setWidthFull();
        photoCenter.setJustifyContentMode(JustifyContentMode.CENTER);
        photoCenter.setAlignItems(Alignment.CENTER);

        applyPhotoState(imagenActual);

        Span hint = new Span("Sube una imagen (jpg/png/webp, máx 3MB).");
        hint.getStyle().set("opacity", "0.7").set("font-size", "0.9rem");

        UI ui = UI.getCurrent();

        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveUserImage(
                            new java.io.ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenActual = savedUrl;

                    if (ui != null) ui.access(this::applyPhotoStateNow);
                    else applyPhotoStateNow();
                })
        );

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024);
        upload.setWidthFull();
        upload.getStyle().set("cursor", "pointer");

        Button quitar = new Button("Quitar foto", e -> {
            imagenActual = null;
            applyPhotoStateNow();
        });
        quitar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        quitar.getStyle().set("cursor", "pointer");

        card.add(photoCenter, hint, upload, quitar);
        return card;
    }

    private void applyPhotoStateNow() {
        applyPhotoState(imagenActual);
    }

    private void applyPhotoState(String url) {
        photoCenter.removeAll();

        if (url != null && !url.isBlank()) {
            fotoPreview.setSrc(url);
            fotoPreview.setAlt("Foto de perfil");
            photoCenter.add(fotoPreview);
        } else {
            avatarFallback.setName(safe(usuario.getNombre()));
            photoCenter.add(avatarFallback);
        }
    }

    // ==========================
    // DATOS (nombre/apellidos/direcciones)
    // ==========================
    private VerticalLayout buildCardDatos(Usuario u) {
        VerticalLayout card = buildCardBase("Datos");
        card.setWidthFull();

        FormLayout form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("720px", 2)
        );

        nombre = new TextField("Nombre");
        apellidos = new TextField("Apellidos");
        nombre.setWidthFull();
        apellidos.setWidthFull();

        form.add(nombre, apellidos);

        boolean esCliente = (u instanceof Cliente);

        direccion1 = new TextField("Dirección 1");
        direccion2 = new TextField("Dirección 2");
        direccion1.setWidthFull();
        direccion2.setWidthFull();

        if (esCliente) {
            form.add(direccion1, direccion2);
        }

        // Binder usuario
        binderUsuario.forField(nombre)
                .asRequired("El nombre es obligatorio")
                .bind(Usuario::getNombre, Usuario::setNombre);

        binderUsuario.forField(apellidos)
                .asRequired("Los apellidos son obligatorios")
                .bind(Usuario::getApellidos, Usuario::setApellidos);

        binderUsuario.readBean(u);

        // Binder cliente
        if (esCliente) {
            Cliente c = (Cliente) u;

            binderCliente.forField(direccion1)
                    .asRequired("La dirección 1 es obligatoria")
                    .bind(Cliente::getDireccion1, Cliente::setDireccion1);

            binderCliente.forField(direccion2)
                    .bind(Cliente::getDireccion2, Cliente::setDireccion2);

            binderCliente.readBean(c);
        }

        card.add(form);
        return card;
    }

    // ==========================
    // SEGURIDAD (correo + pass) SIN botones aquí
    // ==========================
    private Component buildCardSeguridad(Usuario u) {
        VerticalLayout card = buildCardBase("Seguridad");
        card.setWidthFull();

        correoEditable = new EmailField("Correo");
        correoEditable.setWidthFull();
        correoEditable.setValue(safe(u.getCorreo()));
        correoEditable.setClearButtonVisible(true);

        Span hintCorreo = new Span("Si cambias el correo tendrás que iniciar sesión de nuevo.");
        hintCorreo.getStyle().set("opacity", "0.7").set("font-size", "0.9rem");

        passActual = new PasswordField("Contraseña actual");
        passNueva = new PasswordField("Nueva contraseña");
        passRepetir = new PasswordField("Repetir nueva contraseña");
        passActual.setWidthFull();
        passNueva.setWidthFull();
        passRepetir.setWidthFull();

        Span hintPass = new Span("Para cambiar contraseña, rellena los 3 campos.");
        hintPass.getStyle().set("opacity", "0.7").set("font-size", "0.9rem");

        FormLayout passForm = new FormLayout(passActual, passNueva, passRepetir);
        passForm.setWidthFull();
        passForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        card.add(correoEditable, hintCorreo, new Hr(), hintPass, passForm);
        return card;
    }

    // ==========================
    // BOTONES ÚNICOS
    // ==========================
    private HorizontalLayout buildBottomActions() {
        guardarUnico = new Button("Guardar cambios");
        guardarUnico.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        guardarUnico.getStyle().set("cursor", "pointer").set("font-weight", "800");

        cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelar.getStyle().set("cursor", "pointer");

        guardarUnico.addClickListener(e -> onGuardarTodo());
        cancelar.addClickListener(e -> onCancelarTodo());

        HorizontalLayout actions = new HorizontalLayout(cancelar, guardarUnico);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setAlignItems(Alignment.CENTER);
        actions.getStyle().set("padding", "0 2px"); // pequeño ajuste visual

        return actions;
    }

    private void onGuardarTodo() {
        // Inputs seguridad
        String nuevoCorreo = correoEditable.getValue() == null ? "" : correoEditable.getValue().trim();
        String correoActual = usuario.getCorreo() == null ? "" : usuario.getCorreo().trim();

        boolean quiereCambiarPass =
                !(isBlank(passActual.getValue()) && isBlank(passNueva.getValue()) && isBlank(passRepetir.getValue()));

        // Validaciones rápidas
        if (nuevoCorreo.isBlank()) {
            Notification.show("El correo no puede estar vacío", 2300, Notification.Position.TOP_CENTER);
            return;
        }

        if (quiereCambiarPass) {
            String a = passActual.getValue();
            String n = passNueva.getValue();
            String r = passRepetir.getValue();

            if (isBlank(a) || isBlank(n) || isBlank(r)) {
                Notification.show("Para cambiar la contraseña, rellena los 3 campos", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            if (!n.equals(r)) {
                Notification.show("La nueva contraseña no coincide", 2500, Notification.Position.TOP_CENTER);
                return;
            }
            if (n.length() < 6) {
                Notification.show("La nueva contraseña debe tener al menos 6 caracteres", 2700, Notification.Position.TOP_CENTER);
                return;
            }
        }

        boolean necesitaLogout = false;

        try {
            // 1) datos + direcciones
            binderUsuario.writeBean(usuario);
            if (usuario instanceof Cliente c) {
                binderCliente.writeBean(c);
            }

            // 2) foto
            usuario.setImagen(imagenActual);

            // Guardar datos base
            usuarioService.guardarCambiosPerfil(usuario);

            // 3) correo (si cambió)
            if (!nuevoCorreo.equalsIgnoreCase(correoActual)) {
                boolean changed = usuarioService.updateCorreo(usuario.getId(), nuevoCorreo);
                if (changed) {
                    necesitaLogout = true;
                    // actualizar en memoria para que “cancelar” no lo rompa
                    usuario.setCorreo(nuevoCorreo);
                    correoActual = nuevoCorreo;
                }
            }

            // 4) contraseña (si pidió)
            if (quiereCambiarPass) {
                usuarioService.changePassword(usuario.getId(), passActual.getValue(), passNueva.getValue());
                necesitaLogout = true;

                passActual.clear();
                passNueva.clear();
                passRepetir.clear();
            }

            // commit foto
            imagenOriginal = usuario.getImagen();

            if (necesitaLogout) {
                Notification.show("Cambios guardados. Vuelve a iniciar sesión.", 2800, Notification.Position.TOP_CENTER);
                authenticationContext.logout();
            } else {
                Notification.show("Perfil actualizado", 1500, Notification.Position.BOTTOM_CENTER);
            }

        } catch (ValidationException ex) {
            Notification.show("Revisa los campos del formulario", 2300, Notification.Position.TOP_CENTER);
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage(), 2700, Notification.Position.TOP_CENTER);
        } catch (Exception ex) {
            Notification.show("Error al guardar: " + ex.getMessage(), 2700, Notification.Position.TOP_CENTER);
        }
    }

    private void onCancelarTodo() {
        // Restaurar binders
        binderUsuario.readBean(usuario);
        if (usuario instanceof Cliente c) {
            binderCliente.readBean(c);
        }

        // Restaurar foto
        imagenActual = imagenOriginal;
        applyPhotoStateNow();

        // Restaurar seguridad (correo)
        correoEditable.setValue(safe(usuario.getCorreo()));

        // limpiar pass
        passActual.clear();
        passNueva.clear();
        passRepetir.clear();

        Notification.show("Cambios descartados", 1200, Notification.Position.BOTTOM_CENTER);
    }

    // ==========================
    // GERENTE: RESTAURANTE
    // ==========================
    private Component buildCardRestaurante(Restaurante r) {
        VerticalLayout card = buildCardBase("Restaurante (Gerente)");
        card.setWidthFull();

        if (r == null) {
            Span s = new Span("No tienes restaurante asignado.");
            s.getStyle().set("opacity", "0.7");
            card.add(s);
            return card;
        }

        card.add(
                new Paragraph("Nombre: " + safe(r.getNombre())),
                new Paragraph("Dirección: " + safe(r.getDireccion())),
                new Paragraph("Teléfono: " + safe(r.getTelefono())),
                new Paragraph("Horario: " + safe(r.getHorario()))
        );

        return card;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "" : s;
    }
}
