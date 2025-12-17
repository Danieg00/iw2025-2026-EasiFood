package com.easifood.app.views;

import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Login | EasiFood")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView(UsuarioService usuarioService) {

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
        getStyle().set("background-image", "url('/images/food-bg.png')");
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
                e -> openRegisterClienteDialog(usuarioService)
        );

        Button registerGerenteBtn = new Button(
                "Registrarse como Gerente",
                e -> openRegisterGerenteDialog(usuarioService)
        );

        // Layout vertical para los dos botones
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

        // Añadir todo a la vista
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
    // REGISTRO DE CLIENTE
    // ============================================================
    private void openRegisterClienteDialog(UsuarioService usuarioService) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registro de Cliente");
        dialog.setWidth("520px");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("overflow", "visible");

        TextField nombre = new TextField("Nombre");
        TextField apellidos = new TextField("Apellidos");
        TextField correo = new TextField("Correo");
        PasswordField contra = new PasswordField("Contraseña");
        TextField direccion1 = new TextField("Dirección 1");
        TextField direccion2 = new TextField("Dirección 2");

        nombre.setWidthFull();
        apellidos.setWidthFull();
        correo.setWidthFull();
        contra.setWidthFull();
        direccion1.setWidthFull();
        direccion2.setWidthFull();


        VerticalLayout layout = new VerticalLayout(
                nombre, apellidos, correo, contra, direccion1, direccion2
        );

        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("row-gap", "0.4rem");
        layout.setWidthFull();

        Button registrar = new Button("Registrar", event -> {
            try {
                usuarioService.registrarCliente(
                        nombre.getValue(),
                        apellidos.getValue(),
                        correo.getValue(),
                        contra.getValue(),
                        direccion1.getValue(),
                        direccion2.getValue()
                );
                dialog.close();
            } catch (Exception ex) {
                correo.setInvalid(true);
                correo.setErrorMessage("El correo ya está registrado.");
            }
        });

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancelar, registrar);
        dialog.add(layout);
        dialog.open();
    }

    // ============================================================
    // REGISTRO DE GERENTE
    // ============================================================
    private void openRegisterGerenteDialog(UsuarioService usuarioService) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registro de Gerente y Restaurante");
        dialog.setWidth("520px");
        dialog.setHeight("auto");
        dialog.getElement().getStyle().set("overflow", "visible");

        TextField nombre = new TextField("Nombre");
        TextField apellidos = new TextField("Apellidos");
        TextField correo = new TextField("Correo");
        PasswordField contra = new PasswordField("Contraseña");

        TextField nombreRest = new TextField("Nombre del Restaurante");
        TextField direccion = new TextField("Dirección");
        TextField aforo = new TextField("Aforo (número)");
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

        VerticalLayout layout = new VerticalLayout(
                nombre, apellidos, correo, contra,
                nombreRest, direccion, aforo, telefono, horario
        );

        layout.setSpacing(false);
        layout.setPadding(true);
        layout.getStyle().set("row-gap", "0.4rem");
        layout.setWidthFull();


        Button registrar = new Button("Registrar", event -> {
            try {
                Integer aforoInt = null;
                if (!aforo.isEmpty()) {
                    aforoInt = Integer.valueOf(aforo.getValue());
                }

                usuarioService.registrarGerente(
                        nombre.getValue(),
                        apellidos.getValue(),
                        correo.getValue(),
                        contra.getValue(),
                        nombreRest.getValue(),
                        direccion.getValue(),
                        aforoInt,
                        telefono.getValue(),
                        horario.getValue()
                );

                dialog.close();

            } catch (NumberFormatException nfe) {
                aforo.setInvalid(true);
                aforo.setErrorMessage("Debe ser un número.");
            } catch (IllegalArgumentException iae) {
                // Esta sí corresponde al correo duplicado
                correo.setInvalid(true);
                correo.setErrorMessage(iae.getMessage());
            } catch (Exception ex) {
                // Aquí puedes ver el error real mientras desarrollas
                ex.printStackTrace();
                // Si quieres, pon un mensaje genérico:
                // Notification.show("Error al registrar el gerente");
            }
        });

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        dialog.getFooter().add(cancelar, registrar);
        dialog.add(layout);
        dialog.open();
    }
}
