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
        pedido.setEstado("PAGADO"); // ✅ como se crea tras pagar
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);

        // ✅ Vaciar carrito SOLO cuando ya se ha "pagado"
        carritoService.clear(restauranteId);

        return guardado;
    }
}
