package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;

import jakarta.persistence.*;

@Entity
@Table(name = "personajes")

@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_personaje")

public abstract class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int vida;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    private Colonia colonia;

    // Constructor obligatorio JPA
    public Personaje() {}

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public String getNombre() { return nombre; }

    public int getVida() { return vida; }

    public Colonia getColonia() { return colonia; }

    // ================= SETTERS =================

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }

    // ================= GAMEPLAY =================

    public abstract int getProduccion();

}