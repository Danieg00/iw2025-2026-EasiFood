package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RestauranteService restauranteService;
    private final UsuarioService usuarioService;
    private final CarritoService carritoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            RestauranteService restauranteService,
            UsuarioService usuarioService,
            CarritoService carritoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.restauranteService = restauranteService;
        this.usuarioService = usuarioService;
        this.carritoService = carritoService;
    }

    // =========================
    // MÉTODOS QUE YA TENÍAS
    // =========================

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Pedido findById(Long id) {
        if (id == null) return null;
        return pedidoRepository.findById(id).orElse(null);
    }

    public List<Pedido> pedidosDelEmpleado(Empleado empleado) {
        return pedidoRepository.findByEmpleado(empleado);
    }

    public Pedido guardar(Pedido pedido) {
        if (pedido == null) throw new IllegalArgumentException("Pedido inválido");
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> obtenerPedidosDeHoy(Restaurante restaurante) {
        if (restaurante == null) throw new IllegalArgumentException("Restaurante inválido");

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();     // 00:00:00
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX); // 23:59:59.999...

        return pedidoRepository.findByRestauranteAndFechaCreacionBetween(
                restaurante, inicioDia, finDia
        );
    }

    public BigDecimal calcularVentasHoy(Restaurante restaurante) {
        List<Pedido> pedidosHoy = obtenerPedidosDeHoy(restaurante);

        return pedidosHoy.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =========================
    // CREAR PEDIDO (LLAMAR SOLO TRAS "PAGAR" EN PagoView)
    // =========================

    public Pedido crearPedido(Long restauranteId, String direccionEntrega) {

        if (restauranteId == null) {
            throw new IllegalArgumentException("Restaurante inválido");
        }

        if (direccionEntrega == null || direccionEntrega.isBlank()) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");
        }

        String direccion = direccionEntrega.trim();
        if (direccion.length() < 10) {
            throw new IllegalArgumentException("Dirección demasiado corta (mínimo 10 caracteres)");
        }
        if (direccion.length() > 300) {
            throw new IllegalArgumentException("Dirección demasiado larga (máximo 300 caracteres)");
        }
        boolean tieneLetra = direccion.chars().anyMatch(Character::isLetter);
        if (!tieneLetra) {
            throw new IllegalArgumentException("La dirección debe contener letras");
        }

        Restaurante restaurante = restauranteService.findById(restauranteId);
        if (restaurante == null) {
            throw new IllegalArgumentException("Restaurante no encontrado");
        }

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (u == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        if (!(u instanceof Cliente)) {
            throw new IllegalStateException("Solo un cliente puede crear pedidos");
        }
        Cliente cliente = (Cliente) u;

        // Si llega aquí, asumimos "pago ficticio OK" (lo valida la PagoView)
        BigDecimal total = carritoService.totalPrecio(restauranteId);
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El carrito está vacío");
        }

        Pedido pedido = new Pedido();
        pedido.setRestaurante(restaurante);
        pedido.setCliente(cliente);
        pedido.setEmpleado(null);
        pedido.setDireccionEntrega(direccion);

        // ✅ CLAVE: editable solo si está PENDIENTE
        // Tu PagoView hace el “pago ficticio”, pero el flujo del pedido empieza en PENDIENTE
        pedido.setEstado("PENDIENTE");

        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);

        // ✅ Vaciar carrito solo cuando ya se ha creado el pedido
        carritoService.clear(restauranteId);

        return guardado;
    }

    public Pedido asignarPedidoAEmpleado(Pedido pedido, Empleado empleado) {

        if (pedido == null) {
            throw new IllegalArgumentException("Pedido inválido");
        }

        if (empleado == null) {
            throw new IllegalArgumentException("Empleado inválido");
        }

        // Opcional: impedir reasignación si ya está asignado
        if (pedido.getEmpleado() != null) {
            return pedido;
        }

        pedido.setEmpleado(empleado);
        return pedidoRepository.save(pedido);
    }
    // =========================
    // LISTAR PEDIDOS DEL CLIENTE (TAB "Mis pedidos")
    // =========================
    public List<Pedido> pedidosDeCliente(Cliente cliente) {
        return pedidoRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }

    // =========================
    // DETALLE SEGURO: PEDIDO SOLO SI ES DEL CLIENTE
    // (requiere findByIdAndCliente en PedidoRepository)
    // =========================
    public Optional<Pedido> pedidoDeCliente(Long pedidoId, Cliente cliente) {
        if (pedidoId == null || cliente == null) return Optional.empty();
        return pedidoRepository.findByIdAndCliente(pedidoId, cliente);
    }

    // =========================
    // EDITAR SOLO SI ESTÁ PENDIENTE
    // =========================
    public Pedido actualizarDireccionEntregaSiPendiente(Long pedidoId, Cliente cliente, String nuevaDireccion) {

        Pedido p = pedidoRepository.findByIdAndCliente(pedidoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(p.getEstado())) {
            throw new IllegalStateException("Solo se puede modificar si está en estado PENDIENTE");
        }

        if (nuevaDireccion == null || nuevaDireccion.isBlank()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }

        String direccion = nuevaDireccion.trim();
        if (direccion.length() < 10) {
            throw new IllegalArgumentException("Dirección demasiado corta (mínimo 10 caracteres)");
        }
        if (direccion.length() > 300) {
            throw new IllegalArgumentException("Dirección demasiado larga (máximo 300 caracteres)");
        }
        boolean tieneLetra = direccion.chars().anyMatch(Character::isLetter);
        if (!tieneLetra) {
            throw new IllegalArgumentException("La dirección debe contener letras");
        }

        p.setDireccionEntrega(direccion);
        return pedidoRepository.save(p);
    }

    public void cancelarPedidoSiPendiente(Long pedidoId, Cliente cliente) {

        Pedido p = pedidoRepository.findByIdAndCliente(pedidoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(p.getEstado())) {
            throw new IllegalStateException("Solo se puede cancelar si está en estado PENDIENTE");
        }

        pedidoRepository.delete(p);
    }
}
