package com.easifood.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "empleados")
public class Empleado extends Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //private String nombre;
    private String puesto;
    private BigDecimal salario;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    public Empleado() {}

    /*public Empleado(String nombre, String puesto, BigDecimal salario, Restaurante restaurante) {
        //this.nombre = nombre;
        this.puesto = puesto;
        this.salario = salario;
        this.restaurante = restaurante;
    }*/

    public Empleado(String nombre,
                    String apellidos,
                    String correo,
                    String imagen,
                    String contra,
                    String role,
                    String puesto,
                    BigDecimal salario,
                    Restaurante restaurante) {

        super(nombre, apellidos, correo, imagen, contra, role);
        this.puesto = puesto;
        this.salario = salario;
        this.restaurante = restaurante;
    }

    // Getters y Setters
    public Long getId() { return id; }
    //public String getNombre() { return nombre; }
    //public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public Restaurante getRestaurante() { return restaurante; }
    public void setRestaurante(Restaurante restaurante) { this.restaurante = restaurante; }
}
