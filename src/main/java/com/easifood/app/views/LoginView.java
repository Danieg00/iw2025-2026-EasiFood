package com.easifood.app.views;

import com.easifood.app.service.ClienteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | EasiFood")
@AnonymousAllowed // permite entrar sin estar autenticado
public class LoginView extends VerticalLayout {

    public LoginView(ClienteService clienteService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("EasiFood - Acceso");

        // Formulario de login de Vaadin (autenticación genérica)
        LoginForm loginForm = new LoginForm();
        // IMPORTANTE: debe coincidir con el endpoint de Spring Security (/login)
        loginForm.setAction("login");

        // Por claridad de UX, indicamos que el "username" es el correo
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.getElement().setProperty("usernameLabel", "Correo");
        loginForm.getElement().setProperty("passwordLabel", "Contraseña");

        // Botón para abrir el diálogo de registro de Cliente
        Button registerButton = new Button("Registrarse como cliente",
                event -> openRegisterDialog(clienteService));

        add(title, loginForm, registerButton);
    }

    private void openRegisterDialog(ClienteService clienteService) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Registro de cliente");

        // Campos del Cliente
        TextField nombre = new TextField("Nombre");
        nombre.setRequired(true);

        TextField apellidos = new TextField("Apellidos");
        apellidos.setRequired(true);

        EmailField correo = new EmailField("Correo electrónico");
        correo.setRequiredIndicatorVisible(true);
        correo.setErrorMessage("Introduce un correo válido");

        PasswordField contra = new PasswordField("Contraseña");
        contra.setRequired(true);

        TextField direccion1 = new TextField("Dirección (línea 1)");
        direccion1.setRequired(true);

        TextField direccion2 = new TextField("Dirección (línea 2)");
        direccion2.setRequired(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(
                nombre,
                apellidos,
                correo,
                contra,
                direccion1,
                direccion2
        );

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        Button crear = new Button("Crear cuenta", e -> {
            try {
                // Validaciones mínimas
                if (nombre.isEmpty() || apellidos.isEmpty() ||
                        correo.isEmpty() || contra.isEmpty() ||
                        direccion1.isEmpty() || direccion2.isEmpty()) {
                    Notification.show("Rellena todos los campos", 3000, Notification.Position.MIDDLE);
                    return;
                }
                if (correo.isInvalid()) {
                    Notification.show("El correo no es válido", 3000, Notification.Position.MIDDLE);
                    return;
                }

                // Registramos el cliente usando el servicio (que encripta la contraseña y guarda en BD)
                clienteService.register(
                        nombre.getValue(),
                        apellidos.getValue(),
                        correo.getValue(),
                        contra.getValue(),
                        direccion1.getValue(),
                        direccion2.getValue()
                );

                Notification.show("Cliente registrado. Ahora puedes iniciar sesión.", 3000, Notification.Position.MIDDLE);
                dialog.close();

            } catch (IllegalArgumentException ex) {
                // Por ejemplo: correo duplicado
                Notification.show("Error: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Error inesperado al registrar el cliente", 4000, Notification.Position.MIDDLE);
            }
        });

        dialog.add(formLayout);
        dialog.getFooter().add(cancelar, crear);
        dialog.setWidth("400px");
        dialog.open();
    }
}
