package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.tarea.Tarea;
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

    private int hambre = 100;

    private int cansancio = 0;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    @ManyToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tareaActual;

    public Personaje() {}

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
    }

    // ================= CANSANCIO =================

    public void aumentarCansancio(int cantidad) {

        cansancio += cantidad;

        if (cansancio > 100) {
            cansancio = 100;
        }
    }

    public void reducirCansancio(int cantidad) {

        cansancio -= cantidad;

        if (cansancio < 0) {
            cansancio = 0;
        }
    }

    // ================= HAMBRE =================

    public void reducirHambre(int cantidad) {

        hambre -= cantidad;

        if (hambre < 0) {
            hambre = 0;
        }
    }

    public void comer(int cantidad) {

        hambre += cantidad;

        if (hambre > 100) {
            hambre = 100;
        }
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getHambre() {
        return hambre;
    }

    public int getCansancio() {
        return cansancio;
    }

    public Colonia getColonia() {
        return colonia;
    }

    public Tarea getTareaActual() {
        return tareaActual;
    }

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }

    public void setTareaActual(Tarea tareaActual) {
        this.tareaActual = tareaActual;
    }

    // ================= PRODUCCIÓN =================

    public abstract int getProduccion();
}