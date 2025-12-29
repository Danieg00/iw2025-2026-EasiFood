package com.easifood.app.views.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserMenuBar extends HorizontalLayout {

    public UserMenuBar(AuthenticationContext authContext) {
        setPadding(false);
        setSpacing(false);
        getStyle().set("margin-left", "auto"); // empuja a la derecha

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String displayName = resolveDisplayName(auth);

        Avatar avatar = new Avatar(displayName);
        avatar.setAbbreviation(initials(displayName));
        avatar.setHeight("38px");
        avatar.setWidth("38px");

        // (Opcional) si tienes URL de imagen del usuario:
        // avatar.setImage("https://...");

        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(false);
        menuBar.getStyle().set("padding", "0");
        menuBar.getStyle().set("background", "transparent");

        MenuItem root = menuBar.addItem(avatar);
        SubMenu sub = root.getSubMenu();

        sub.addItem("Mi perfil", e -> UI.getCurrent().navigate("perfil"));

        sub.addSeparator();

        sub.addItem("Cerrar sesión", e -> authContext.logout());

        add(menuBar);
    }

    private String resolveDisplayName(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return "Usuario";
        // Normalmente principal es el username (email). Ajusta si tienes UserDetails propio.
        String principal = String.valueOf(auth.getPrincipal());
        if ("anonymousUser".equals(principal)) return "Usuario";
        return principal;
    }

    private String initials(String name) {
        if (name == null) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) return "U";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
