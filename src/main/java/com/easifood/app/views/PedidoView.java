package com.easifood.app.views;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.service.EmpleadoService;
import com.easifood.app.service.PedidoService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;
import java.time.LocalDateTime;
import java.util.List;

@Route("pedido")
@PageTitle("Crear Pedido")
@PermitAll
public class PedidoView extends VerticalLayout {

    public PedidoView(
            PedidoService pedidoService,
            EmpleadoService empleadoService
    ) {

        setWidth("400px");
        setPadding(true);
        setSpacing(true);

        add(new H1("Crear pedido"));

        // Empleado
        ComboBox<Empleado> empleadoCombo = new ComboBox<>("Empleado");
        List<Empleado> empleados = empleadoService.findAll();
        empleadoCombo.setItems(empleados);
        empleadoCombo.setItemLabelGenerator(Empleado::getNombre);
        empleadoCombo.setRequired(true);

        // Dirección
        TextField direccion = new TextField("Dirección de entrega");
        direccion.setRequired(true);

        // Estado
        ComboBox<String> estadoCombo = new ComboBox<>("Estado");
        estadoCombo.setItems(
                "PENDIENTE",
                "EN_PREPARACION",
                "EN_CAMINO",
                "ENTREGADO"
        );
        estadoCombo.setValue("PENDIENTE");

        // Botón
        Button crear = new Button("Crear pedido");

        crear.addClickListener(e -> {

            if (empleadoCombo.isEmpty() || direccion.isEmpty()) {
                Notification.show("Completa todos los campos");
                return;
            }

            Pedido pedido = new Pedido(
                    empleadoCombo.getValue(),
                    direccion.getValue(),
                    estadoCombo.getValue(),
                    LocalDateTime.now()
            );

            /*Pedido pedido = new Pedido();
            pedido.setEmpleado(empleadoCombo.getValue());
            pedido.setDireccionEntrega(direccion.getValue());
            pedido.setEstado(estadoCombo.getValue());
            pedido.setFechaCreacion(LocalDateTime.now());*/

            pedidoService.guardar(pedido);

            Notification.show("Pedido creado correctamente");

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
}
