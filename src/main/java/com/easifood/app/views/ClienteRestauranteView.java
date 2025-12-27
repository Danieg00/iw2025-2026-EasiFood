
package com.easifood.app.views;

import com.easifood.app.model.Restaurante;
import com.easifood.app.service.RestauranteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Restaurante")
@Route("cliente-restaurante/:id")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteRestauranteView extends VerticalLayout implements BeforeEnterObserver {

    private final RestauranteService restauranteService;

    public ClienteRestauranteView(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildTopBar());
    }

    private HorizontalLayout buildTopBar() {
        HorizontalLayout top = new HorizontalLayout();
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        Button back = new Button("⬅ Volver", e -> getUI().ifPresent(ui -> ui.navigate("home-cliente")));
        top.add(back);

        return top;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long id = event.getRouteParameters().get("id").map(Long::valueOf).orElse(null);
        if (id == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        Restaurante r = restauranteService.findById(id);
        if (r == null) {
            event.rerouteTo("home-cliente");
            return;
        }

        // Imagen (opción B: URL en BD)
        String url = (r.getImagenUrl() != null && !r.getImagenUrl().isBlank())
                ? r.getImagenUrl()
                : "/images/restaurantes/default.jpg";

        Image img = new Image(url, "Foto " + r.getNombre());
        img.setWidth("100%");
        img.setMaxWidth("720px");
        img.getStyle().set("border-radius", "12px");
        img.getStyle().set("object-fit", "cover");

        add(img);

        H2 title = new H2(r.getNombre());
        title.getStyle().set("margin", "0.2rem 0 0.6rem 0");
        add(title);

        add(new Paragraph("Dirección: " + safe(r.getDireccion())));
        add(new Paragraph("Teléfono: " + safe(r.getTelefono())));
        add(new Paragraph("Horario: " + safe(r.getHorario())));
        add(new Paragraph("Aforo: " + (r.getAforo() != null ? r.getAforo() : "-")));

        add(new Paragraph("Aquí mostraremos los productos/menús del restaurante cuando exista el modelo Producto."));
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
