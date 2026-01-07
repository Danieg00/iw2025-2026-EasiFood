package com.easifood.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal total;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false, length = 300)
    private String direccionEntrega;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoProducto> lineas = new ArrayList<>();

    // ✅ Constructor JPA
    public Pedido() {}

    // ✅ Constructor que tú ya usabas (lo dejo)
    public Pedido(Empleado empleado, String direccionEntrega, String estado, LocalDateTime fechaCreacion) {
        this.empleado = empleado;
        this.direccionEntrega = direccionEntrega;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    @PrePersist
    private void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null || estado.isBlank()) estado = "PENDIENTE";
        if (total == null) total = BigDecimal.ZERO;
    }

    // =====================
    // RELACIÓN: helpers
    // =====================
    public void addLinea(Producto producto, int cantidad) {
        if (producto == null) throw new IllegalArgumentException("Producto no puede ser null");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser > 0");

        PedidoProducto linea = new PedidoProducto(this, producto, cantidad);
        this.lineas.add(linea);
    }

    public void addLinea(PedidoProducto linea) {
        if (linea == null) return;
        linea.setPedido(this);
        this.lineas.add(linea);
    }

    public void removeLinea(PedidoProducto linea) {
        if (linea == null) return;
        this.lineas.remove(linea);
        linea.setPedido(null);
    }

    public void clearLineas() {
        for (PedidoProducto l : new ArrayList<>(lineas)) {
            removeLinea(l);
        }
    }

    public void recalcularTotal() {
        this.total = lineas.stream()
                .map(PedidoProducto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // =====================
    // GETTERS
    // =====================
    public Long getId() { return id; }

    public BigDecimal getTotal() { return total != null ? total : BigDecimal.ZERO; }

    public Restaurante getRestaurante() { return restaurante; }

    public Empleado getEmpleado() { return empleado; }

    public Cliente getCliente() { return cliente; }

    public String getDireccionEntrega() { return direccionEntrega; }

    public String getEstado() { return estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public List<PedidoProducto> getLineas() { return lineas; }

    // =====================
    // SETTERS
    // =====================
    public void setTotal(BigDecimal total) { this.total = total; }

    public void setRestaurante(Restaurante restaurante) { this.restaurante = restaurante; }

    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }

    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public void setEstado(String estado) { this.estado = estado; }

    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
