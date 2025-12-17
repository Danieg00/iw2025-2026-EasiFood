package com.easifood.app.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;

    @Column(unique = true, nullable = false)
    private String correo;

    private String imagen;

    @Column(nullable = false)
    private String contra;   // contraseña encriptada

    @Column(nullable = false)
    private String role;     // ROLE_CLIENTE, ROLE_GERENTE, etc.

    protected Usuario() {}

    public Usuario(String nombre, String apellidos, String correo,
                   String imagen, String contra, String role) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.imagen = imagen;
        this.contra = contra;
        this.role = role;
    }

    // Getters / setters
    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getContra() { return contra; }
    public void setContra(String contra) { this.contra = contra; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
