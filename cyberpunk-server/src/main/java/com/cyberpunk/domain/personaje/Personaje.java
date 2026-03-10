package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_personaje")
public abstract class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int vida;

    private int hambre = 100;

    private int cansancio = 0;
    @ManyToOne
    @JoinColumn(name = "edificio_id")
    private Edificio edificioAsignado;

    @ManyToOne
    @JoinColumn(name = "construccion_id")
    private ConstruccionEnCurso construccionAsignada;
    
    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

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
    public Edificio getEdificioAsignado() {
        return edificioAsignado;
    }

    public ConstruccionEnCurso getConstruccionAsignada() {
        return construccionAsignada;
    }
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

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }
    public void setEdificioAsignado(Edificio edificioAsignado) {
        this.edificioAsignado = edificioAsignado;
    }

    public void setConstruccionAsignada(ConstruccionEnCurso construccionAsignada) {
        this.construccionAsignada = construccionAsignada;
    }

    // ================= PRODUCCIÓN =================

    public abstract int getProduccion();
}