package com.easifood.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 1000)
    private String ingredientes;

    private String imagenUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    private boolean esMenu = false;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "producto_items", // Tabla intermedia que crea Hibernate automáticamente
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Producto> itemsMenu = new ArrayList<>();

    public Producto() {}

    public Producto(String nombre, String descripcion, BigDecimal precio, Restaurante restaurante) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.restaurante = restaurante;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Restaurante getRestaurante() { return restaurante; }
    public void setRestaurante(Restaurante restaurante) { this.restaurante = restaurante; }

    public boolean isEsMenu() { return esMenu; }
    public void setEsMenu(boolean esMenu) { this.esMenu = esMenu; }

    public List<Producto> getItemsMenu() { return itemsMenu; }
    public void setItemsMenu(List<Producto> itemsMenu) { this.itemsMenu = itemsMenu; }
}
