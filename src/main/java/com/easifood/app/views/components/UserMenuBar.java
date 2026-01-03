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

        // ==========================
        // AVATAR
        // ==========================
        Avatar avatar = new Avatar(displayName);
        avatar.setAbbreviation(initials(displayName));
        avatar.setHeight("38px");
        avatar.setWidth("38px");

        // 👉 manita
        avatar.getStyle().set("cursor", "pointer");

        // ==========================
        // MENU BAR
        // ==========================
        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(false);
        menuBar.getStyle()
                .set("padding", "0")
                .set("background", "transparent");

        MenuItem root = menuBar.addItem(avatar);

        // 👉 manita también en el trigger
        root.getElement().getStyle().set("cursor", "pointer");

        SubMenu sub = root.getSubMenu();

        // ==========================
        // ITEMS
        // ==========================
        MenuItem perfil = sub.addItem("Mi perfil", e ->
                UI.getCurrent().navigate("perfil")
        );

        MenuItem logout = sub.addItem("Cerrar sesión", e ->
                authContext.logout()
        );

        // 👉 manita en items
        perfil.getElement().getStyle().set("cursor", "pointer");
        logout.getElement().getStyle().set("cursor", "pointer");

        sub.addSeparator();

        add(menuBar);
    }

    private String resolveDisplayName(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return "Usuario";
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
