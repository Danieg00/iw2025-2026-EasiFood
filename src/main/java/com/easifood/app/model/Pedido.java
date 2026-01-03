package com.easifood.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    @ManyToOne
    private Empleado empleado;

    @ManyToOne
    private Usuario cliente;

    private String direccionEntrega;
    private String estado;
    private LocalDateTime fechaCreacion;

    // Constructor JPA (correcto que sea protected)
    protected Pedido() {}

    // ✅ CONSTRUCTOR DE NEGOCIO
    public Pedido(Empleado empleado,
                  Usuario cliente,
                  String direccionEntrega,
                  String estado,
                  LocalDateTime fechaCreacion) {

        this.empleado = empleado;
        this.cliente = cliente;
        this.direccionEntrega = direccionEntrega;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    // getters / setters
    public Usuario getCliente() {
        return cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
/*
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // RELACIONES
    // =========================

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado; // repartidor asignado (nullable)

    // =========================
    // DATOS DEL PEDIDO
    // =========================

    @Column(nullable = false)
    private String estado;
    // Ej: PENDIENTE, EN_PREPARACION, EN_CAMINO, ENTREGADO, CANCELADO

    private String direccionEntrega;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaEntrega;

    // =========================
    // CONSTRUCTORES
    // =========================

    protected Pedido() {}

    public Pedido(Cliente cliente,
                  Restaurante restaurante,
                  String estado,
                  String direccionEntrega) {

        this.cliente = cliente;
        this.restaurante = restaurante;
        this.estado = estado;
        this.direccionEntrega = direccionEntrega;
        this.fechaCreacion = LocalDateTime.now();
    }

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
}*/