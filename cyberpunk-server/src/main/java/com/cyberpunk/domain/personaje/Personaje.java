package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;
import com.fasterxml.jackson.annotation.JsonBackReference;

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

    // NUEVO
    private int hambre;

    private int cansancio;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    public Personaje() {}

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
        this.hambre = 100;
        this.cansancio = 0;
    }

    // ===== SISTEMA DE ESTADO =====

    public void aumentarCansancio(int cantidad){
        cansancio += cantidad;
        if(cansancio > 100) cansancio = 100;
    }

    public void descansar(){
        cansancio -= 10;
        if(cansancio < 0) cansancio = 0;
    }

    public void consumirHambre(int cantidad){
        hambre -= cantidad;
        if(hambre < 0) hambre = 0;
    }

    public void comer(int comida){
        hambre += comida;
        if(hambre > 100) hambre = 100;
    }

    public boolean puedeActuar(){
        return hambre > 0 && cansancio < 100;
    }

    // ===== GETTERS =====

    public Long getId() { return id; }

    public String getNombre() { return nombre; }

    public int getVida() { return vida; }

    public int getHambre() { return hambre; }

    public int getCansancio() { return cansancio; }

    public Colonia getColonia() { return colonia; }

    public void setColonia(Colonia colonia) { this.colonia = colonia; }

    public abstract int getProduccion();
}