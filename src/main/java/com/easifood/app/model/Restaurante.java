package com.easifood.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurantes")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private Integer aforo;
    private String telefono;
    private String horario;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @OneToOne(mappedBy = "restaurante")
    private Gerente gerente;

    protected Restaurante() {}

    public Restaurante(String nombre,
                       String direccion,
                       Integer aforo,
                       String telefono,
                       String horario) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.aforo = aforo;
        this.telefono = telefono;
        this.horario = horario;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getAforo() { return aforo; }
    public void setAforo(Integer aforo) { this.aforo = aforo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Gerente getGerente() { return gerente; }
    public void setGerente(Gerente gerente) { this.gerente = gerente; }
}
