package com.easifood.app.service;

import com.easifood.app.model.Cliente;
import com.easifood.app.model.Empleado;
import com.easifood.app.model.Pedido;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.model.Producto;
import com.easifood.app.model.Restaurante;
import com.easifood.app.model.Usuario;
import com.easifood.app.repository.PedidoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RestauranteService restauranteService;
    private final UsuarioService usuarioService;
    private final CarritoService carritoService;
    private final EntityManager em;

    public PedidoService(
            PedidoRepository pedidoRepository,
            RestauranteService restauranteService,
            UsuarioService usuarioService,
            CarritoService carritoService,
            EntityManager em
    ) {
        this.pedidoRepository = pedidoRepository;
        this.restauranteService = restauranteService;
        this.usuarioService = usuarioService;
        this.carritoService = carritoService;
        this.em = em;
    }

    // =====================================================
    // DTO para actualizar líneas desde la vista
    // =====================================================
    public static class LineaUpdate {
        private Long productoId;
        private int cantidad;

        public LineaUpdate() {}

        public LineaUpdate(Long productoId, int cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }

    // =====================================================
    // CONSULTAS BÁSICAS
    // =====================================================

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

    // =====================================================
    // PEDIDOS DE HOY
    // =====================================================

    public List<Pedido> obtenerPedidosDeHoy(Restaurante restaurante) {
        if (restaurante == null) throw new IllegalArgumentException("Restaurante inválido");

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);

        return pedidoRepository.findByRestauranteAndFechaCreacionBetween(
                restaurante, inicioDia, finDia
        );
    }

    public BigDecimal calcularVentasHoy(Restaurante restaurante) {
        return obtenerPedidosDeHoy(restaurante).stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =====================================================
    // CREAR PEDIDO (DESDE PagoView)
    // =====================================================

    @Transactional
    public Pedido crearPedido(Long restauranteId, String direccionEntrega) {

        if (restauranteId == null) throw new IllegalArgumentException("Restaurante inválido");
        if (direccionEntrega == null || direccionEntrega.isBlank())
            throw new IllegalArgumentException("La dirección de entrega es obligatoria");

        String direccion = direccionEntrega.trim();
        if (direccion.length() < 10) throw new IllegalArgumentException("Dirección demasiado corta");
        if (direccion.length() > 300) throw new IllegalArgumentException("Dirección demasiado larga");
        if (!direccion.chars().anyMatch(Character::isLetter))
            throw new IllegalArgumentException("La dirección debe contener letras");

        Restaurante restaurante = restauranteService.findById(restauranteId);
        if (restaurante == null) throw new IllegalArgumentException("Restaurante no encontrado");

        Usuario u = usuarioService.obtenerUsuarioActual();
        if (!(u instanceof Cliente cliente)) throw new IllegalStateException("Solo un cliente puede crear pedidos");

        List<CarritoService.Item> items = carritoService.items(restauranteId);
        if (items == null || items.isEmpty()) throw new IllegalStateException("El carrito está vacío");

        Pedido pedido = new Pedido();
        pedido.setRestaurante(restaurante);
        pedido.setCliente(cliente);
        pedido.setEmpleado(null);
        pedido.setDireccionEntrega(direccion);
        pedido.setEstado("PENDIENTE");
        pedido.setFechaCreacion(LocalDateTime.now());

        for (CarritoService.Item it : items) {
            if (it == null || it.getProducto() == null) continue;
            if (it.getCantidad() <= 0) continue;

            // MUY IMPORTANTE: addLinea debe setear ambos lados (pedido <-> linea)
            pedido.addLinea(it.getProducto(), it.getCantidad());
        }

        pedido.recalcularTotal();

        // ✅ fuerza escritura en BD antes de tocar el carrito
        Pedido guardado = pedidoRepository.saveAndFlush(pedido);

        // ✅ si clear falla, NO queremos rollback del pedido
        try {
            carritoService.clear(restauranteId);
        } catch (Exception ex) {
            // opcional: loggear
            // log.warn("No se pudo limpiar el carrito: {}", ex.getMessage());
        }

        return guardado;
    }


    // =====================================================
    // PEDIDOS DEL CLIENTE
    // =====================================================

    public List<Pedido> pedidosDeCliente(Cliente cliente) {
        return pedidoRepository.findByClienteOrderByFechaCreacionDesc(cliente);
    }

    // ✅ AQUÍ ESTÁ LA CLAVE PARA EL DETALLE
    @Transactional(readOnly = true)
    public Optional<Pedido> pedidoDeCliente(Long pedidoId, Cliente cliente) {
        if (pedidoId == null || cliente == null) return Optional.empty();
        return pedidoRepository.findByIdAndClienteWithLineas(pedidoId, cliente);
    }

    // =====================================================
    // DIRECCIÓN / CANCELAR SI PENDIENTE
    // =====================================================

    @Transactional
    public Pedido actualizarDireccionEntregaSiPendiente(
            Long pedidoId, Cliente cliente, String nuevaDireccion) {

        Pedido p = pedidoRepository.findByIdAndCliente(pedidoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(p.getEstado())) {
            throw new IllegalStateException("Solo se puede modificar si está PENDIENTE");
        }

        if (nuevaDireccion == null || nuevaDireccion.isBlank()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }

        p.setDireccionEntrega(nuevaDireccion.trim());
        pedidoRepository.save(p);

        // ✅ devolver con lineas cargadas para que la vista pueda iterar sin Lazy
        return pedidoRepository.findByIdAndClienteWithLineas(pedidoId, cliente).orElse(p);
    }

    @Transactional
    public void cancelarPedidoSiPendiente(Long pedidoId, Cliente cliente) {

        Pedido p = pedidoRepository.findByIdAndCliente(pedidoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(p.getEstado())) {
            throw new IllegalStateException("Solo se puede cancelar si está PENDIENTE");
        }

        pedidoRepository.delete(p);
    }

    // =====================================================
    // ✅ ACTUALIZAR LÍNEAS (SOLO SI PENDIENTE)
    // =====================================================

    @Transactional
    public Pedido actualizarLineasSiPendiente(Long pedidoId, Cliente cliente, List<LineaUpdate> updates) {

        Pedido p = pedidoRepository.findByIdAndCliente(pedidoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!"PENDIENTE".equalsIgnoreCase(p.getEstado())) {
            throw new IllegalStateException("Solo se puede modificar si está PENDIENTE");
        }

        if (updates == null) updates = List.of();

        // ✅ Mapa de cantidades deseadas (FINAL)
        java.util.Map<Long, Integer> desired = new java.util.HashMap<>();
        for (LineaUpdate up : updates) {
            if (up == null || up.getProductoId() == null) continue;
            desired.put(up.getProductoId(), up.getCantidad());
        }

        // ✅ 1) Eliminar líneas que ya no estén en updates (o qty <= 0)
        for (PedidoProducto linea : new ArrayList<>(p.getLineas())) {
            Long prodId = (linea.getProducto() != null) ? linea.getProducto().getId() : null;
            if (prodId == null) {
                p.removeLinea(linea);
                continue;
            }

            Integer qty = desired.get(prodId);

            // Si no viene en updates => eliminar
            if (qty == null || qty <= 0) {
                p.removeLinea(linea);
            }
        }

        // ✅ 2) Actualizar o crear las líneas que sí estén en updates con qty > 0
        for (var entry : desired.entrySet()) {
            Long prodId = entry.getKey();
            int qty = entry.getValue() != null ? entry.getValue() : 0;

            if (qty <= 0) continue;

            PedidoProducto existente = null;
            for (PedidoProducto l : p.getLineas()) {
                if (l.getProducto() != null && prodId.equals(l.getProducto().getId())) {
                    existente = l;
                    break;
                }
            }

            if (existente != null) {
                existente.setCantidad(qty);
            } else {
                Producto ref = em.getReference(Producto.class, prodId);
                p.addLinea(ref, qty);
            }
        }

        p.recalcularTotal();
        pedidoRepository.save(p);

        // ✅ devolver con lineas cargadas para la vista
        return pedidoRepository.findByIdAndClienteWithLineas(pedidoId, cliente).orElse(p);
    }


    public Pedido asignarPedidoAEmpleado(Pedido pedido, Empleado empleado) {
        if (pedido == null) throw new IllegalArgumentException("Pedido inválido");
        if (empleado == null) throw new IllegalArgumentException("Empleado inválido");

        if (pedido.getEmpleado() != null) {
            return pedido;
        }

        pedido.setEmpleado(empleado);
        return pedidoRepository.save(pedido);
    }
}
