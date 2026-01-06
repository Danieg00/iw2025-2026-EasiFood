package com.easifood.app.views.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.VaadinSession;

import java.util.Locale;

public class LanguageSwitchBar extends HorizontalLayout {

    public static final String SESSION_LOCALE_KEY = "app_locale";
    public static final Locale ES = new Locale("es");
    public static final Locale EN = Locale.ENGLISH;

    private final Button btnEs = new Button();
    private final Button btnEn = new Button();

    public LanguageSwitchBar() {
        setPadding(false);
        setSpacing(true);
        getStyle().set("gap", "8px").set("align-items", "center");

        setupButton(btnEs, "/images/flags/es.svg", "ES", ES);
        setupButton(btnEn, "/images/flags/en.svg", "EN", EN);

        add(btnEs, btnEn);

        updateActiveStyles(getEffectiveLocale());
    }

    private void setupButton(Button b, String flagPath, String text, Locale locale) {
        Image flag = new Image(flagPath, text);
        flag.setWidth("18px");
        flag.setHeight("18px");
        flag.getStyle().set("border-radius", "3px"); // opcional

        b.setIcon(flag);
        b.setText(text);

        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        b.getStyle()
                .set("cursor", "pointer")
                .set("border-radius", "999px")
                .set("padding", "6px 10px")
                .set("line-height", "1")
                .set("font-weight", "700");

        b.addClickListener(e -> setLocaleAndReload(locale));
    }

    private void setLocaleAndReload(Locale locale) {
        VaadinSession.getCurrent().setAttribute(SESSION_LOCALE_KEY, locale);

        UI ui = UI.getCurrent();
        ui.setLocale(locale);

        updateActiveStyles(locale);

        // importante para que LoginForm se regenere con el idioma
        ui.getPage().reload();
    }

    private Locale getEffectiveLocale() {
        Object stored = VaadinSession.getCurrent().getAttribute(SESSION_LOCALE_KEY);
        if (stored instanceof Locale l) return normalize(l);

        Locale uiLocale = UI.getCurrent().getLocale();
        return normalize(uiLocale != null ? uiLocale : ES);
    }

    private Locale normalize(Locale l) {
        String lang = (l.getLanguage() == null) ? "" : l.getLanguage().toLowerCase();
        return "en".equals(lang) ? EN : ES;
    }

    private void updateActiveStyles(Locale locale) {
        boolean es = "es".equalsIgnoreCase(locale.getLanguage());
        styleSelected(btnEs, es);
        styleSelected(btnEn, !es);
    }

    private void styleSelected(Button b, boolean selected) {
        if (selected) {
            b.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("box-shadow", "0 0 0 6px var(--lumo-contrast-10pct)")
                    .set("opacity", "1");
        } else {
            b.getStyle()
                    .set("background", "transparent")
                    .set("box-shadow", "none")
                    .set("opacity", "0.85");
        }
    }
}
