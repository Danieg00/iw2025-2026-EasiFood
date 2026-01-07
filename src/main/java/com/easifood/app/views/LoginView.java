package com.easifood.app.views;

import com.easifood.app.service.FileStorageService;
import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocapitalize;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.regex.Pattern;

@PageTitle("Login | EasiFood")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    // Regex simples
    private static final Pattern PHONE = Pattern.compile("^[0-9]{9,15}$");
    private static final Pattern PASS = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    public LoginView(UsuarioService usuarioService, FileStorageService fileStorageService) {

        // ===============================
        // CONFIGURACIÓN DEL LAYOUT
        // ===============================
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // ===============================
        // FONDO
        // ===============================
        getStyle().set("background-image", "url('/images/login/food-bg.png')");
        getStyle().set("background-size", "cover");
        getStyle().set("background-position", "center");
        getStyle().set("background-repeat", "no-repeat");

        // Overlay blanco translúcido
        Div overlay = new Div();
        overlay.getStyle().set("position", "absolute");
        overlay.getStyle().set("top", "0");
        overlay.getStyle().set("left", "0");
        overlay.getStyle().set("width", "100%");
        overlay.getStyle().set("height", "100%");
        overlay.getStyle().set("background-color", "rgba(255,255,255,0.55)");
        overlay.getStyle().set("z-index", "1");

        // Contenedor principal
        Div wrapper = new Div();
        wrapper.getStyle().set("position", "relative");
        wrapper.getStyle().set("z-index", "2");

        // ===============================
        // LOGIN FORM
        // ===============================
        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setI18n(getSpanishI18n());

        // Título
        H1 title = new H1("EasiFood");
        title.getStyle().set("text-align", "center");
        title.setWidthFull();

        // ===============================
        // BOTONES DE REGISTRO
        // ===============================
        Button registerClienteBtn = new Button(
                "Registrarse como Cliente",
                e -> openRegisterClienteDialog(usuarioService, fileStorageService)
        );
        registerClienteBtn.getStyle().set("cursor", "pointer");

        Button registerGerenteBtn = new Button(
                "Registrarse como Gerente",
                e -> openRegisterGerenteDialog(usuarioService, fileStorageService)
        );
        registerGerenteBtn.getStyle().set("cursor", "pointer");

        VerticalLayout btnLayout = new VerticalLayout(registerClienteBtn, registerGerenteBtn);
        btnLayout.setPadding(false);
        btnLayout.setSpacing(true);
        btnLayout.setAlignItems(Alignment.CENTER);
        btnLayout.setWidthFull();

        // ===============================
        // TARJETA (CARD)
        // ===============================
        Div card = new Div();
        card.getStyle().set("background-color", "rgba(255,255,255,0.9)");
        card.getStyle().set("padding", "2rem");
        card.getStyle().set("border-radius", "16px");
        card.getStyle().set("box-shadow", "0 8px 25px rgba(0,0,0,0.15)");
        card.getStyle().set("min-width", "320px");
        card.getStyle().set("max-width", "500px");

        VerticalLayout cardLayout = new VerticalLayout(title, loginForm, btnLayout);
        cardLayout.setPadding(false);
        cardLayout.setSpacing(true);
        cardLayout.setAlignItems(Alignment.STRETCH);

        card.add(cardLayout);
        wrapper.add(card);

        add(overlay, wrapper);
    }

    // ============================================================
    // MANEJO DE /login?error
    // ============================================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean hasError = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error");

        if (hasError) {
            loginForm.setError(true);
        }
    }

    // ============================================================
    // LOGIN I18N (ESPAÑOL)
    // ============================================================
    private LoginI18n getSpanishI18n() {

        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Iniciar sesión");
        form.setUsername("Correo electrónico");
        form.setPassword("Contraseña");
        form.setSubmit("Entrar");
        form.setForgotPassword("");

        i18n.setForm(form);

        LoginI18n.ErrorMessage error = i18n.getErrorMessage();
        error.setTitle("Error");
        error.setMessage("Correo o contraseña incorrectos.");
        i18n.setErrorMessage(error);

        return i18n;
    }

    // ============================================================
    // DTOs para Binder (solo para validar UI)
    // ============================================================
    private static class ClienteReg {
        String nombre, apellidos, correo, contra, direccion1, direccion2;
    }

    private static class GerenteReg {
        String nombre, apellidos, correo, contra;
        String nombreRest, direccion, telefono, horario;
        Integer aforo;
    }

    // ============================================================
    // REGISTRO DE CLIENTE (VALIDADO)
    // ============================================================
    private void openRegisterClienteDialog(UsuarioService usuarioService,
                                           FileStorageService fileStorageService) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registro de Cliente");
        dialog.setWidth("520px");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("overflow", "visible");

        TextField nombre = new TextField("Nombre");
        TextField apellidos = new TextField("Apellidos");
        EmailField correo = new EmailField("Correo");
        PasswordField contra = new PasswordField("Contraseña");
        TextField direccion1 = new TextField("Dirección 1");
        TextField direccion2 = new TextField("Dirección 2");

        nombre.setWidthFull();
        apellidos.setWidthFull();
        correo.setWidthFull();
        contra.setWidthFull();
        direccion1.setWidthFull();
        direccion2.setWidthFull();

        nombre.setAutocapitalize(Autocapitalize.WORDS);
        apellidos.setAutocapitalize(Autocapitalize.WORDS);

        correo.setClearButtonVisible(true);
        correo.setValueChangeMode(ValueChangeMode.EAGER);

        // Foto de perfil (opcional)
        Image preview = new Image();
        preview.setVisible(false);
        preview.setWidth("110px");
        preview.setHeight("110px");
        preview.getStyle().set("border-radius", "50%").set("object-fit", "cover");
        preview.getStyle().set("display", "block");

        final String[] imagenUrl = { null };

        Upload upload = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveUserImage(
                            new ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenUrl[0] = savedUrl;

                    preview.setSrc(savedUrl);
                    preview.setVisible(true);
                    preview.getElement().callJsFunction("requestContentUpdate");
                })
        );

        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(3 * 1024 * 1024);
        upload.setDropLabel(new Span("Arrastra tu foto aquí o pulsa para seleccionar"));
        upload.getStyle().set("cursor", "pointer");

        // Binder + validaciones
        ClienteReg bean = new ClienteReg();
        Binder<ClienteReg> binder = new Binder<>(ClienteReg.class);

        binder.forField(nombre)
                .asRequired("El nombre es obligatorio")
                .withValidator(v -> v != null && v.trim().length() >= 2, "Mínimo 2 caracteres")
                .bind(b -> b.nombre, (b, v) -> b.nombre = v);

        binder.forField(apellidos)
                .asRequired("Los apellidos son obligatorios")
                .withValidator(v -> v != null && v.trim().length() >= 2, "Mínimo 2 caracteres")
                .bind(b -> b.apellidos, (b, v) -> b.apellidos = v);

        binder.forField(correo)
                .asRequired("El correo es obligatorio")
                .withValidator(new EmailValidator("Correo inválido"))
                .bind(b -> b.correo, (b, v) -> b.correo = v);

        correo.addValueChangeListener(e -> binder.validate());

        binder.forField(contra)
                .asRequired("La contraseña es obligatoria")
                .withValidator(v -> v != null && PASS.matcher(v).matches(),
                        "Mín. 6 caracteres y debe incluir letras y números")
                .bind(b -> b.contra, (b, v) -> b.contra = v);

        binder.forField(direccion1)
                .asRequired("La dirección 1 es obligatoria")
                .withValidator(v -> v != null && v.trim().length() >= 5, "Mínimo 5 caracteres")
                .bind(b -> b.direccion1, (b, v) -> b.direccion1 = v);

        binder.forField(direccion2)
                .bind(b -> b.direccion2, (b, v) -> b.direccion2 = v);

        Span requiredNote = new Span("Los campos marcados como obligatorios deben rellenarse");
        requiredNote.getStyle().set("font-size", "0.85rem");
        requiredNote.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout layout = new VerticalLayout(
                nombre, apellidos, correo, contra, direccion1, direccion2,
                requiredNote,
                new Span("Foto de perfil (opcional)"),
                upload,
                preview
        );
        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("row-gap", "0.4rem");
        layout.setWidthFull();

        Button registrar = new Button("Registrar");
        registrar.getStyle().set("cursor", "pointer");
        registrar.setEnabled(false);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        final boolean[] uploading = { false };
        Runnable refresh = () -> registrar.setEnabled(binder.isValid() && !uploading[0]);

        binder.addStatusChangeListener(e -> refresh.run());

        upload.getElement().addEventListener("upload-start", e -> {
            uploading[0] = true;
            refresh.run();
        });
        upload.getElement().addEventListener("upload-success", e -> {
            uploading[0] = false;
            refresh.run();
        });
        upload.getElement().addEventListener("upload-error", e -> {
            uploading[0] = false;
            refresh.run();
        });
        upload.getElement().addEventListener("upload-abort", e -> {
            uploading[0] = false;
            refresh.run();
        });
        upload.getElement().addEventListener("file-remove", e -> {
            uploading[0] = false;
            refresh.run();
            preview.setVisible(false);
            imagenUrl[0] = null;
        });

        refresh.run();

        registrar.addClickListener(e -> {
            try {
                binder.writeBean(bean);

                usuarioService.registrarCliente(
                        bean.nombre.trim(),
                        bean.apellidos.trim(),
                        bean.correo.trim().toLowerCase(Locale.ROOT),
                        bean.contra,
                        bean.direccion1.trim(),
                        (bean.direccion2 == null ? "" : bean.direccion2.trim()),
                        imagenUrl[0]
                );

                Notification.show("Registrado correctamente. Ya puedes iniciar sesión.",
                        2200, Notification.Position.TOP_CENTER);

                dialog.close();

            } catch (ValidationException vex) {
                Notification.show("Revisa los campos", 2000, Notification.Position.TOP_CENTER);
            } catch (IllegalArgumentException iae) {
                correo.setInvalid(true);
                correo.setErrorMessage(iae.getMessage());
            } catch (Exception ex) {
                Notification.show("Error al registrar", 2500, Notification.Position.TOP_CENTER);
            }
        });

        dialog.getFooter().add(cancelar, registrar);
        dialog.add(layout);
        dialog.open();

        binder.readBean(bean);
        binder.validate();
        refresh.run();
    }

    // ============================================================
    // REGISTRO DE GERENTE (VALIDADO) + 2 UPLOADS
    // ============================================================
    private void openRegisterGerenteDialog(UsuarioService usuarioService,
                                           FileStorageService fileStorageService) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registro de Gerente y Restaurante");
        dialog.setWidth("560px");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("overflow", "visible");

        TextField nombre = new TextField("Nombre");
        TextField apellidos = new TextField("Apellidos");
        EmailField correo = new EmailField("Correo");
        PasswordField contra = new PasswordField("Contraseña");

        TextField nombreRest = new TextField("Nombre del Restaurante");
        TextField direccion = new TextField("Dirección");
        IntegerField aforo = new IntegerField("Aforo");
        TextField telefono = new TextField("Teléfono");
        TextField horario = new TextField("Horario");

        nombre.setWidthFull();
        apellidos.setWidthFull();
        correo.setWidthFull();
        contra.setWidthFull();
        nombreRest.setWidthFull();
        direccion.setWidthFull();
        aforo.setWidthFull();
        telefono.setWidthFull();
        horario.setWidthFull();

        nombre.setAutocapitalize(Autocapitalize.WORDS);
        apellidos.setAutocapitalize(Autocapitalize.WORDS);

        correo.setClearButtonVisible(true);
        correo.setValueChangeMode(ValueChangeMode.EAGER);

        aforo.setMin(1);
        aforo.setStepButtonsVisible(true);
        aforo.setHelperText("Número positivo");

        // ==========================
        // Imagen RESTAURANTE (NUEVO) - debajo de nombreRest
        // ==========================
        Image previewRest = new Image();
        previewRest.setVisible(false);
        previewRest.setWidthFull();
        previewRest.setMaxHeight("180px");
        previewRest.getStyle().set("border-radius", "12px").set("object-fit", "cover");
        previewRest.getStyle().set("display", "block");

        final String[] imagenRestUrl = { null };

        Upload uploadRest = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveRestaurantImage(
                            new ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenRestUrl[0] = savedUrl;

                    previewRest.setSrc(savedUrl);
                    previewRest.setVisible(true);
                    previewRest.getElement().callJsFunction("requestContentUpdate");
                })
        );

        uploadRest.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        uploadRest.setMaxFiles(1);
        uploadRest.setMaxFileSize(3 * 1024 * 1024);
        uploadRest.setDropLabel(new Span("Arrastra la imagen del restaurante aquí o pulsa para seleccionar"));
        uploadRest.getStyle().set("cursor", "pointer");

        // ==========================
        // Foto PERFIL GERENTE (la que ya tenías)
        // ==========================
        Image previewGerente = new Image();
        previewGerente.setVisible(false);
        previewGerente.setWidth("110px");
        previewGerente.setHeight("110px");
        previewGerente.getStyle().set("border-radius", "50%").set("object-fit", "cover");
        previewGerente.getStyle().set("display", "block");

        final String[] imagenGerenteUrl = { null };

        Upload uploadGerente = new Upload(
                UploadHandler.inMemory((metadata, bytes) -> {
                    if (bytes == null || bytes.length == 0) return;

                    String savedUrl = fileStorageService.saveUserImage(
                            new ByteArrayInputStream(bytes),
                            metadata.fileName()
                    );

                    imagenGerenteUrl[0] = savedUrl;

                    previewGerente.setSrc(savedUrl);
                    previewGerente.setVisible(true);
                    previewGerente.getElement().callJsFunction("requestContentUpdate");
                })
        );

        uploadGerente.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        uploadGerente.setMaxFiles(1);
        uploadGerente.setMaxFileSize(3 * 1024 * 1024);
        uploadGerente.setDropLabel(new Span("Arrastra tu foto aquí o pulsa para seleccionar"));
        uploadGerente.getStyle().set("cursor", "pointer");

        // Binder + validaciones
        GerenteReg bean = new GerenteReg();
        Binder<GerenteReg> binder = new Binder<>(GerenteReg.class);

        binder.forField(nombre)
                .asRequired("El nombre es obligatorio")
                .withValidator(v -> v != null && v.trim().length() >= 2, "Mínimo 2 caracteres")
                .bind(b -> b.nombre, (b, v) -> b.nombre = v);

        binder.forField(apellidos)
                .asRequired("Los apellidos son obligatorios")
                .withValidator(v -> v != null && v.trim().length() >= 2, "Mínimo 2 caracteres")
                .bind(b -> b.apellidos, (b, v) -> b.apellidos = v);

        binder.forField(correo)
                .asRequired("El correo es obligatorio")
                .withValidator(new EmailValidator("Correo inválido"))
                .bind(b -> b.correo, (b, v) -> b.correo = v);

        correo.addValueChangeListener(e -> binder.validate());

        binder.forField(contra)
                .asRequired("La contraseña es obligatoria")
                .withValidator(v -> v != null && PASS.matcher(v).matches(),
                        "Mín. 6 caracteres y debe incluir letras y números")
                .bind(b -> b.contra, (b, v) -> b.contra = v);

        binder.forField(nombreRest)
                .asRequired("El nombre del restaurante es obligatorio")
                .withValidator(v -> v != null && v.trim().length() >= 2, "Mínimo 2 caracteres")
                .bind(b -> b.nombreRest, (b, v) -> b.nombreRest = v);

        binder.forField(direccion)
                .asRequired("La dirección es obligatoria")
                .withValidator(v -> v != null && v.trim().length() >= 5, "Mínimo 5 caracteres")
                .bind(b -> b.direccion, (b, v) -> b.direccion = v);

        binder.forField(aforo)
                .asRequired("El aforo es obligatorio")
                .withValidator(v -> v != null && v >= 1, "Debe ser un número positivo")
                .bind(b -> b.aforo, (b, v) -> b.aforo = v);

        binder.forField(telefono)
                .asRequired("El teléfono es obligatorio")
                .withValidator(v -> v != null && PHONE.matcher(v.trim()).matches(), "Solo números (9-15 dígitos)")
                .bind(b -> b.telefono, (b, v) -> b.telefono = v);

        binder.forField(horario)
                .asRequired("El horario es obligatorio")
                .withValidator(v -> v != null && v.trim().length() >= 4, "Escribe un horario válido")
                .bind(b -> b.horario, (b, v) -> b.horario = v);

        Span requiredNote = new Span("Los campos marcados como obligatorios deben rellenarse");
        requiredNote.getStyle().set("font-size", "0.85rem");
        requiredNote.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // 👇 IMPORTANTE: imagen restaurante justo debajo de nombreRest
        VerticalLayout layout = new VerticalLayout(
                nombre, apellidos, correo, contra,
                nombreRest,

                new Span("Imagen del restaurante (opcional)"),
                uploadRest,
                previewRest,

                direccion, aforo, telefono, horario,

                new Span("Foto de perfil del gerente (opcional)"),
                uploadGerente,
                previewGerente,

                requiredNote
        );

        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("row-gap", "0.4rem");
        layout.setWidthFull();

        Button registrar = new Button("Registrar");
        registrar.getStyle().set("cursor", "pointer");
        registrar.setEnabled(false);

        Button cancelar = new Button("Cancelar", e -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        // Estado de uploads (2 uploads)
        final int[] uploadingCount = { 0 };
        Runnable refresh = () -> registrar.setEnabled(binder.isValid() && uploadingCount[0] == 0);

        binder.addStatusChangeListener(e -> refresh.run());

        Runnable inc = () -> { uploadingCount[0]++; refresh.run(); };
        Runnable dec = () -> { uploadingCount[0] = Math.max(0, uploadingCount[0] - 1); refresh.run(); };

        // --- Restaurante
        uploadRest.getElement().addEventListener("upload-start", e -> inc.run());
        uploadRest.getElement().addEventListener("upload-success", e -> dec.run());
        uploadRest.getElement().addEventListener("upload-error", e -> dec.run());
        uploadRest.getElement().addEventListener("upload-abort", e -> dec.run());
        uploadRest.getElement().addEventListener("file-remove", e -> {
            imagenRestUrl[0] = null;
            previewRest.setVisible(false);
            refresh.run();
        });

        // --- Gerente
        uploadGerente.getElement().addEventListener("upload-start", e -> inc.run());
        uploadGerente.getElement().addEventListener("upload-success", e -> dec.run());
        uploadGerente.getElement().addEventListener("upload-error", e -> dec.run());
        uploadGerente.getElement().addEventListener("upload-abort", e -> dec.run());
        uploadGerente.getElement().addEventListener("file-remove", e -> {
            imagenGerenteUrl[0] = null;
            previewGerente.setVisible(false);
            refresh.run();
        });

        refresh.run();

        registrar.addClickListener(e -> {
            try {
                binder.writeBean(bean);

                // ✅ NUEVO: pasamos 2 imágenes
                usuarioService.registrarGerente(
                        bean.nombre.trim(),
                        bean.apellidos.trim(),
                        bean.correo.trim().toLowerCase(Locale.ROOT),
                        bean.contra,
                        bean.nombreRest.trim(),
                        bean.direccion.trim(),
                        bean.aforo,
                        bean.telefono.trim(),
                        bean.horario.trim(),
                        imagenGerenteUrl[0],
                        imagenRestUrl[0]
                );

                Notification.show("Registrado correctamente. Ya puedes iniciar sesión.",
                        2200, Notification.Position.TOP_CENTER);

                dialog.close();

            } catch (ValidationException vex) {
                Notification.show("Revisa los campos", 2000, Notification.Position.TOP_CENTER);
            } catch (IllegalArgumentException iae) {
                correo.setInvalid(true);
                correo.setErrorMessage(iae.getMessage());
            } catch (Exception ex) {
                Notification.show("Error al registrar", 2500, Notification.Position.TOP_CENTER);
            }
        });

        dialog.getFooter().add(cancelar, registrar);
        dialog.add(layout);
        dialog.open();

        binder.readBean(bean);
        binder.validate();
        refresh.run();
    }
}
