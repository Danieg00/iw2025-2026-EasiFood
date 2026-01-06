package com.easifood.app.views;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.service.EmpleadoService;
import com.easifood.app.service.PedidoService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.List;

@Route("pedido")
@PermitAll
public class PedidoView extends VerticalLayout implements AfterNavigationObserver {

    public PedidoView(PedidoService pedidoService, EmpleadoService empleadoService) {

        setWidth("400px");
        setPadding(true);
        setSpacing(true);

        H1 title = new H1(getTranslation("order.create.title"));
        add(title);

        // Empleado
        ComboBox<Empleado> empleadoCombo = new ComboBox<>(getTranslation("order.create.employee"));
        List<Empleado> empleados = empleadoService.findAll();
        empleadoCombo.setItems(empleados);
        empleadoCombo.setItemLabelGenerator(Empleado::getNombre);
        empleadoCombo.setRequired(true);

        // Dirección
        TextField direccion = new TextField(getTranslation("order.create.deliveryAddress"));
        direccion.setRequired(true);

        // Estado
        ComboBox<String> estadoCombo = new ComboBox<>(getTranslation("order.create.status"));
        estadoCombo.setItems(
                "PENDIENTE",
                "EN_PREPARACION",
                "EN_CAMINO",
                "ENTREGADO"
        );
        estadoCombo.setValue("PENDIENTE");

        // Botón
        Button crear = new Button(getTranslation("order.create.button"));
        crear.getStyle().set("cursor", "pointer");

        crear.addClickListener(e -> {

            if (empleadoCombo.isEmpty() || direccion.isEmpty()) {
                Notification.show(getTranslation("order.create.validation.fillAll"));
                return;
            }

            Pedido pedido = new Pedido(
                    empleadoCombo.getValue(),
                    direccion.getValue(),
                    estadoCombo.getValue(),
                    LocalDateTime.now()
            );

            pedidoService.guardar(pedido);

            Notification.show(getTranslation("order.create.success"));

            direccion.clear();
            estadoCombo.setValue("PENDIENTE");
        });

        add(
                empleadoCombo,
                direccion,
                estadoCombo,
                crear
        );
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        UI.getCurrent().getPage().setTitle(getTranslation("order.create.pageTitle"));
    }
}
