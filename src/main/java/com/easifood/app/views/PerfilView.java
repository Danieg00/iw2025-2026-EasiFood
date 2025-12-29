package com.easifood.app.views;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Usuario;
import com.easifood.app.service.UsuarioService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Mi perfil")
@Route("perfil")
@RolesAllowed({"ROLE_CLIENTE", "ROLE_GERENTE"})
public class PerfilView extends VerticalLayout {

    public PerfilView(UsuarioService usuarioService, AuthenticationContext authenticationContext) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        // Top bar
        Button volver = new Button("⬅ Volver", e -> UI.getCurrent().getPage().getHistory().back());
        Button logout = new Button("Cerrar sesión", e -> authenticationContext.logout());

        HorizontalLayout top = new HorizontalLayout(volver, logout);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(top);

        add(new H1("Mi perfil"));

        String correo = authenticationContext.getPrincipalName().orElse(null);

        if (correo == null) {
            add(new Paragraph("No se pudo obtener el usuario autenticado."));
            return;
        }

        Usuario u = usuarioService.findByCorreo(correo);

        if (u == null) {
            add(new Paragraph("No se encontró el usuario en la base de datos."));
            return;
        }

        // Foto / Avatar
        add(buildFoto(u));

        // Datos básicos
        add(new H3("Datos"));
        add(new Paragraph("Nombre: " + safe(u.getNombre())));
        add(new Paragraph("Apellidos: " + safe(u.getApellidos())));
        add(new Paragraph("Correo: " + safe(u.getCorreo())));
        add(new Paragraph("Rol: " + safe(u.getRole())));

        // Datos extra si es cliente
        if (u instanceof Cliente c) {
            add(new H3("Direcciones"));
            add(new Paragraph("Dirección 1: " + safe(c.getDireccion1())));
            add(new Paragraph("Dirección 2: " + safe(c.getDireccion2())));
        }
    }

    private VerticalLayout buildFoto(Usuario u) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setSpacing(false);
        box.setAlignItems(Alignment.CENTER);

        String img = u.getImagen();

        if (img != null && !img.isBlank()) {
            Image foto = new Image(img, "Foto de perfil");
            foto.setWidth("140px");
            foto.setHeight("140px");
            foto.getStyle()
                    .set("border-radius", "50%")
                    .set("object-fit", "cover")
                    .set("box-shadow", "var(--lumo-box-shadow-s)");
            box.add(foto);
        } else {
            Avatar avatar = new Avatar(safe(u.getNombre()));
            avatar.setWidth("140px");
            avatar.setHeight("140px");
            box.add(avatar);
        }

        return box;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
