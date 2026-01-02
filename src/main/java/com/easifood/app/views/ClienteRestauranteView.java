package com.easifood.app.views;

import com.easifood.app.service.CarritoService;
import com.easifood.app.service.ProductoService;
import com.easifood.app.service.RestauranteService;
import com.easifood.app.views.components.ClienteRestauranteContent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Restaurante")
@Route("cliente-restaurante/:id")
@RolesAllowed("ROLE_CLIENTE")
public class ClienteRestauranteView extends VerticalLayout implements BeforeEnterObserver {

    private final RestauranteService restauranteService;
    private final ProductoService productoService;
    private final CarritoService carritoService;

    public ClienteRestauranteView(RestauranteService restauranteService,
                                  ProductoService productoService,
                                  CarritoService carritoService) {
        this.restauranteService = restauranteService;
        this.productoService = productoService;
        this.carritoService = carritoService;

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

        // Evita duplicados si se re-entra
        getChildren()
                .filter(c -> c instanceof ClienteRestauranteContent)
                .findFirst()
                .ifPresent(this::remove);

        // ✅ Mismo contenido que el popup (sin duplicar código)
        add(new ClienteRestauranteContent(
                id,
                restauranteService,
                productoService,
                carritoService,
                () -> { /* aquí no hace falta refrescar nada */ }
        ));
    }
}
