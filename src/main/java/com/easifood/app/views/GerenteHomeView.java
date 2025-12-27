package com.easifood.app.views;

import com.easifood.app.model.Empleado;
import com.easifood.app.model.Gerente;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.EmpleadoRepository;
import com.easifood.app.repository.UsuarioRepository;
import com.easifood.app.repository.RestauranteRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@PageTitle("Área Gerente")
@Route("home-gerente")
//@RolesAllowed("ROLE_GERENTE")
@AnonymousAllowed
public class GerenteHomeView extends VerticalLayout {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;

    private FlexLayout cardsContainer;
    private Restaurante restauranteActual;

    public GerenteHomeView(EmpleadoRepository empleadoRepository, UsuarioRepository usuarioRepository, RestauranteRepository restauranteRepository) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        cargarDatosDelGerente();

        H1 titulo = new H1("Gestión de Empleados - " +
                (restauranteActual != null ? restauranteActual.getNombre() : "Sin Restaurante"));

        Button btnNuevo = new Button("Nuevo Empleado", e -> abrirEditor(new Empleado()));
        btnNuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cardsContainer = new FlexLayout();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.setJustifyContentMode(JustifyContentMode.START);
        cardsContainer.getStyle().set("gap", "20px");
        cardsContainer.setWidthFull();

        refrescarEmpleados();

        add(titulo, btnNuevo, cardsContainer);
    }

    //Recupera el Usuario logueado, verifica que sea Gerente y obtiene su Restaurante.
    private void cargarDatosDelGerente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //Datos de prueba falsos para hacer pruebas
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Notification.show("MODO PRUEBA: Cargando primer restaurante de la BD", 3000, Notification.Position.TOP_CENTER);

            // Carga el primer restaurante que encuentre en tu base de datos
            this.restauranteActual = restauranteRepository.findAll().stream().findFirst().orElse(null);
            return;
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);

        if (usuario instanceof Gerente) {
            this.restauranteActual = ((Gerente) usuario).getRestaurante();
        }
    }

    //Busca los empleados en BD y regenera las tarjetas.
    private void refrescarEmpleados() {
        cardsContainer.removeAll();

        if (restauranteActual == null) {
            cardsContainer.add(new Span("No tienes un restaurante asignado."));
            return;
        }

        List<Empleado> empleados = empleadoRepository.findByRestaurante(restauranteActual);

        for (Empleado emp : empleados) {
            cardsContainer.add(crearCardEmpleado(emp));
        }
    }

    //Crea el componente visual "Card" para un empleado.
    private Component crearCardEmpleado(Empleado empleado) {
        // Datos
        H3 nombre = new H3(empleado.getNombre());
        nombre.getStyle().set("margin", "0");

        Span puesto = new Span("Puesto: " + empleado.getPuesto());
        Span salario = new Span("Salario: " + empleado.getSalario() + " €");

        puesto.getStyle().set("color", "gray");
        salario.getStyle().set("font-weight", "bold");

        // Botón Editar
        Button btnEditar = new Button("Editar");
        btnEditar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnEditar.addClickListener(e -> abrirEditor(empleado));

        // Layout vertical dentro de la tarjeta
        VerticalLayout content = new VerticalLayout(nombre, puesto, salario, btnEditar);
        content.setSpacing(false);
        content.setPadding(false);

        // Contenedor principal de la tarjeta (Estilos solicitados)
        Div card = new Div(content);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 6px 20px rgba(0,0,0,0.15)")
                .set("width", "300px"); // Ancho fijo o min-width para las tarjetas

        return card;
    }

    //Abre un Diálogo con un Formulario (Grid de campos) para editar/crear.
    private void abrirEditor(Empleado empleado) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(empleado.getId() == null ? "Nuevo Empleado" : "Editar Empleado");

        // Formulario
        FormLayout formLayout = new FormLayout();
        TextField nombreField = new TextField("Nombre");
        TextField puestoField = new TextField("Puesto");
        BigDecimalField salarioField = new BigDecimalField("Salario");

        formLayout.add(nombreField, puestoField, salarioField);

        // Binder para vincular los campos con el objeto
        Binder<Empleado> binder = new Binder<>(Empleado.class);
        //binder.bindInstanceFields(this); // Asume que los nombres de vars coinciden (nombreField -> no, requiere manual o naming strategy)
        binder.forField(nombreField).bind(Empleado::getNombre, Empleado::setNombre);

        // Vinculación manual para seguridad
        binder.forField(nombreField).bind(Empleado::getNombre, Empleado::setNombre);
        binder.forField(puestoField).bind(Empleado::getPuesto, Empleado::setPuesto);
        binder.forField(salarioField).bind(Empleado::getSalario, Empleado::setSalario);

        binder.readBean(empleado);

        // Botones del diálogo
        Button guardar = new Button("Guardar", e -> {
            try {
                binder.writeBean(empleado);
                empleado.setRestaurante(restauranteActual); // Asegurar relación
                empleadoRepository.save(empleado);

                refrescarEmpleados(); // Actualizar la vista de tarjetas
                dialog.close();
                Notification.show("Empleado guardado exitosamente");
            } catch (Exception ex) {
                Notification.show("Error al guardar datos");
            }
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(cancelar, guardar);
        dialog.open();
    }
}