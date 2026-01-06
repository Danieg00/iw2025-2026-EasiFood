package com.easifood.app.views;

import com.easifood.app.service.FileStorageService;
import com.easifood.app.service.UsuarioService;
import com.easifood.app.views.components.LanguageSwitchBar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
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
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.streams.UploadHandler;

import java.io.ByteArrayInputStream;
import java.util.Locale;
import java.util.regex.Pattern;

@PageTitle("Login | EasiFood")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private final LoginForm loginForm;

    // Regex simples
    private static final Pattern PHONE = Pattern.compile("^[0-9]{9,15}$");
    private static final Pattern PASS = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");

    public LoginView(UsuarioService usuarioService, FileStorageService fileStorageService) {

        // ✅ Asegurar locale desde sesión si existe (clave)
        applySessionLocaleIfPresent();

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

        // ✅ Barra idioma fija en la esquina real de la pantalla
        LanguageSwitchBar langBar = new LanguageSwitchBar();
        Div langWrap = new Div(langBar);

        langWrap.getStyle()
                .set("position", "fixed")     // 👈 clave (no depende del wrapper)
                .set("top", "10px")           // más pegado
                .set("right", "10px")         // más pegado
                .set("z-index", "9999");      // por encima del overlay y todo

        add(langWrap);

        // ===============================
        // LOGIN FORM
        // ===============================
        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.setI18n(buildLoginI18n());

        // Título
        H1 title = new H1(getTranslation("app.title"));
        title.getStyle().set("text-align", "center");
        title.setWidthFull();

        // ===============================
        // BOTONES DE REGISTRO
        // ===============================
        Button registerClienteBtn = new Button(
                getTranslation("login.register.client"),
                e -> openRegisterClienteDialog(usuarioService, fileStorageService)
        );
        registerClienteBtn.getStyle().set("cursor", "pointer");

        Button registerGerenteBtn = new Button(
                getTranslation("login.register.manager"),
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
        card.getStyle().set("max-width", "520px");

        // ✅ sin lang dentro (para que NO se duplique)
        VerticalLayout cardLayout = new VerticalLayout(title, loginForm, btnLayout);
        cardLayout.setPadding(false);
        cardLayout.setSpacing(true);
        cardLayout.setAlignItems(Alignment.STRETCH);

        card.add(cardLayout);
        wrapper.add(card);

        add(overlay, wrapper);
    }

    // ✅ Título dinámico (si cambias locale y recargas, se verá bien)
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("login.pageTitle"));
    }

    private void applySessionLocaleIfPresent() {
        Object stored = VaadinSession.getCurrent().getAttribute(LanguageSwitchBar.SESSION_LOCALE_KEY);
        if (stored instanceof Locale l) {
            UI.getCurrent().setLocale(l);
        } else {
            // default ES
            UI.getCurrent().setLocale(LanguageSwitchBar.ES);
        }
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
    // LOGIN I18N (desde messages_*.properties)
    // ============================================================
    private LoginI18n buildLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle(getTranslation("login.form.title"));
        form.setUsername(getTranslation("login.form.username"));
        form.setPassword(getTranslation("login.form.password"));
        form.setSubmit(getTranslation("login.form.submit"));
        form.setForgotPassword("");
        i18n.setForm(form);

        LoginI18n.ErrorMessage error = i18n.getErrorMessage();
        error.setTitle(getTranslation("login.error.title"));
        error.setMessage(getTranslation("login.error.message"));
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
    // REGISTRO DE CLIENTE
    // ============================================================
    private void openRegisterClienteDialog(UsuarioService usuarioService,
                                           FileStorageService fileStorageService) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("register.client.dialogTitle"));
        dialog.setWidth("520px");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("overflow", "visible");

        TextField nombre = new TextField(getTranslation("register.field.firstName"));
        TextField apellidos = new TextField(getTranslation("register.field.lastName"));
        EmailField correo = new EmailField(getTranslation("register.field.email"));
        PasswordField contra = new PasswordField(getTranslation("register.field.password"));
        TextField direccion1 = new TextField(getTranslation("register.client.field.address1"));
        TextField direccion2 = new TextField(getTranslation("register.client.field.address2"));

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
        upload.setDropLabel(new Span(getTranslation("register.upload.profile.dropLabel")));
        upload.getStyle().set("cursor", "pointer");

        ClienteReg bean = new ClienteReg();
        Binder<ClienteReg> binder = new Binder<>(ClienteReg.class);

        binder.forField(nombre)
                .asRequired(getTranslation("register.validation.firstName.required"))
                .withValidator(v -> v != null && v.trim().length() >= 2, getTranslation("register.validation.min2"))
                .bind(b -> b.nombre, (b, v) -> b.nombre = v);

        binder.forField(apellidos)
                .asRequired(getTranslation("register.validation.lastName.required"))
                .withValidator(v -> v != null && v.trim().length() >= 2, getTranslation("register.validation.min2"))
                .bind(b -> b.apellidos, (b, v) -> b.apellidos = v);

        binder.forField(correo)
                .asRequired(getTranslation("register.validation.email.required"))
                .withValidator(new EmailValidator(getTranslation("register.validation.email.invalid")))
                .bind(b -> b.correo, (b, v) -> b.correo = v);

        correo.addValueChangeListener(e -> binder.validate());

        binder.forField(contra)
                .asRequired(getTranslation("register.validation.password.required"))
                .withValidator(v -> v != null && PASS.matcher(v).matches(),
                        getTranslation("register.validation.password.rules"))
                .bind(b -> b.contra, (b, v) -> b.contra = v);

        binder.forField(direccion1)
                .asRequired(getTranslation("register.validation.address1.required"))
                .withValidator(v -> v != null && v.trim().length() >= 5, getTranslation("register.validation.min5"))
                .bind(b -> b.direccion1, (b, v) -> b.direccion1 = v);

        binder.forField(direccion2)
                .bind(b -> b.direccion2, (b, v) -> b.direccion2 = v);

        Span requiredNote = new Span(getTranslation("register.requiredNote"));
        requiredNote.getStyle().set("font-size", "0.85rem");
        requiredNote.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout layout = new VerticalLayout(
                nombre, apellidos, correo, contra, direccion1, direccion2,
                requiredNote,
                new Span(getTranslation("register.upload.profile.labelOptional")),
                upload,
                preview
        );
        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("row-gap", "0.4rem");
        layout.setWidthFull();

        Button registrar = new Button(getTranslation("common.save"));
        registrar.getStyle().set("cursor", "pointer");
        registrar.setEnabled(false);

        Button cancelar = new Button(getTranslation("common.cancel"), e -> dialog.close());
        cancelar.getStyle().set("cursor", "pointer");

        final boolean[] uploading = { false };
        Runnable refresh = () -> registrar.setEnabled(binder.isValid() && !uploading[0]);

        binder.addStatusChangeListener(e -> refresh.run());

        upload.getElement().addEventListener("upload-start", e -> { uploading[0] = true; refresh.run(); });
        upload.getElement().addEventListener("upload-success", e -> { uploading[0] = false; refresh.run(); });
        upload.getElement().addEventListener("upload-error", e -> { uploading[0] = false; refresh.run(); });
        upload.getElement().addEventListener("upload-abort", e -> { uploading[0] = false; refresh.run(); });
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

                Notification.show(getTranslation("register.success"),
                        2200, Notification.Position.TOP_CENTER);

                dialog.close();

            } catch (ValidationException vex) {
                Notification.show(getTranslation("register.fixFields"), 2000, Notification.Position.TOP_CENTER);
            } catch (IllegalArgumentException iae) {
                correo.setInvalid(true);
                correo.setErrorMessage(iae.getMessage());
            } catch (Exception ex) {
                Notification.show(getTranslation("register.error"), 2500, Notification.Position.TOP_CENTER);
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
    // REGISTRO DE GERENTE
    // ============================================================
    private void openRegisterGerenteDialog(UsuarioService usuarioService,
                                           FileStorageService fileStorageService) {

        // (tu método entero igual que lo tienes ahora)
        // 👇 NO te lo repito para no hacer esto enorme; no hace falta tocarlo para arreglar la duplicación.
        // Si quieres, te lo devuelvo completo también.
        throw new UnsupportedOperationException("Pega aquí tu método openRegisterGerenteDialog tal cual (sin cambios).");
    }
}
