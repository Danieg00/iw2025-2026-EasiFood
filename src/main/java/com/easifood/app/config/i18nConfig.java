package com.easifood.app.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.ServiceInitEvent;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.Set;

@Configuration
public class i18nConfig implements VaadinServiceInitListener {

    private static final Locale ES = new Locale("es", "ES");
    private static final Locale EN = Locale.ENGLISH;
    private static final Set<Locale> SUPPORTED = Set.of(ES, EN);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();

            Locale browser = ui.getLocale();
            if (browser == null || !SUPPORTED.contains(browser)) {
                ui.setLocale(ES); // ✅ default ES
            }
        });
    }
}
