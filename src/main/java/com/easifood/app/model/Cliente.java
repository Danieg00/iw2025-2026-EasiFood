package com.easifood.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    @Column(nullable = false)
    private String direccion1;

    @Column(nullable = false)
    private String direccion2;

    protected Cliente() {}

    public Cliente(String nombre, String apellidos, String correo,
                   String imagen, String contra,
                   String direccion1, String direccion2) {

        super(nombre, apellidos, correo, imagen, contra, "ROLE_CLIENTE");
        this.direccion1 = direccion1;
        this.direccion2 = direccion2;
    }

    public String getDireccion1() {
        return direccion1;
    }

    public void setDireccion1(String direccion1) {
        this.direccion1 = direccion1;
    }

    public String getDireccion2() {
        return direccion2;
    }

    public void setDireccion2(String direccion2) {
        this.direccion2 = direccion2;
    }
}
