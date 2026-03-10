package com.cyberpunk.domain.personaje;

import jakarta.persistence.*;

@Entity
@Table(name = "trabajadores")
public class Trabajador extends Personaje {

    private int mineria;
    private int agricultura;
    private int ciencia;
    private int ingenieria;

    // Constructor JPA
    public Trabajador() {}

    public Trabajador(
            String nombre,
            int mineria,
            int agricultura,
            int ciencia,
            int ingenieria) {

        super(nombre, 100);

        this.mineria = mineria;
        this.agricultura = agricultura;
        this.ciencia = ciencia;
        this.ingenieria = ingenieria;
    }

    // ================= PRODUCCIÓN =================

    @Override
    public int getProduccion() {

        // producción base basada en habilidades

        int mejorHabilidad = Math.max(
                Math.max(mineria, agricultura),
                Math.max(ciencia, ingenieria)
        );

        return mejorHabilidad;
    }

    // ================= GETTERS =================

    public int getMineria() {
        return mineria;
    }

    public int getAgricultura() {
        return agricultura;
    }

    public int getCiencia() {
        return ciencia;
    }

    public int getIngenieria() {
        return ingenieria;
    }

}