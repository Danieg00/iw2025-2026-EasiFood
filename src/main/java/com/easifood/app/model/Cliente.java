package com.easifood.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contra;

    @Column(nullable = false)
    private String direccion1;

    @Column(nullable = false)
    private String direccion2;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    protected Cliente() {}

    /**
     * Constructor útil para registrar un nuevo cliente.
     */
    public Cliente(String nombre, String apellidos, String correo, String contra,
                   String direccion1, String direccion2, String role) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.contra = contra;
        this.direccion1 = direccion1;
        this.direccion2 = direccion2;
        this.role = role;
    }

    // ----------- GETTERS -----------

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContra() {
        return contra;
    }

    public String getDireccion1() {
        return direccion1;
    }

    public String getDireccion2() {
        return direccion2;
    }

    public String getRole() {
        return role;
    }

    // ----------- SETTERS -----------

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContra(String contra) {
        this.contra = contra;
    }

    public void setDireccion1(String direccion1) {
        this.direccion1 = direccion1;
    }

    public void setDireccion2(String direccion2) {
        this.direccion2 = direccion2;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
