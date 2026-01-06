package com.easifood.app.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Component
public class AppI18NProvider implements I18NProvider {

    public static final Locale ES = new Locale("es", "ES");
    public static final Locale EN = Locale.ENGLISH;

    private static final List<Locale> PROVIDED = List.of(ES, EN);

    @Override
    public List<Locale> getProvidedLocales() {
        return PROVIDED;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) return "";

        Locale loc = (locale == null) ? ES : locale;
        ResourceBundle bundle = ResourceBundle.getBundle("messages", loc);

        String value;
        try {
            value = bundle.getString(key);
        } catch (MissingResourceException ex) {
            // Si falta clave, devolvemos la key (para detectarlo rápido)
            return "!" + key + "!";
        }

        if (params == null || params.length == 0) return value;
        return MessageFormat.format(value, params);
    }
}
