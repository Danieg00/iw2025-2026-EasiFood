package com.easifood.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gerentes")
public class Gerente extends Usuario {

    @OneToOne
    @JoinColumn(name = "restaurante_id", unique = true)
    private Restaurante restaurante;

    protected Gerente() {}

    public Gerente(String nombre, String apellidos, String correo, String imagen,
                   String contra, Restaurante restaurante) {

        super(nombre, apellidos, correo, imagen, contra, "ROLE_GERENTE");
        this.restaurante = restaurante;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }
}
