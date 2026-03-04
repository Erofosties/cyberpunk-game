package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "personajes")
public abstract class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int vida;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    private Colonia colonia;

    public Personaje() {
    }

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getVida() { return vida; }

    public Colonia getColonia() { return colonia; }
    public void setColonia(Colonia colonia) { this.colonia = colonia; }

    public abstract int getProduccion();
}