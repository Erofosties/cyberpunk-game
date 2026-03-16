package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;

import jakarta.persistence.*;

@Entity
@Table(name = "trabajadores")
@DiscriminatorValue("TRABAJADOR")
public class Trabajador extends Personaje {

    private int mineria;
    private int agricultura;
    private int ciencia;
    private int ingenieria;

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

    // ================= PRODUCCIÓN GENERAL =================

    @Override
    public int getProduccion() {

        int mejorHabilidad = Math.max(
                Math.max(mineria, agricultura),
                Math.max(ciencia, ingenieria)
        );

        return mejorHabilidad;
    }

    // ================= PRODUCCIÓN POR EDIFICIO =================

    public int getProduccionParaEdificio(TipoEdificio tipo) {

        if (tipo == null)
            return 0;

        switch (tipo) {

            case MINA_NEOCROMO,
                 MINA_UMBRIUM,
                 MINA_SYNTHERIUM,
                 MINA_HEXALIUM,
                 MINA_VOIDIUM -> {
                return mineria;
            }

            case GRANJA_KROMAFRUTA,
                 GRANJA_NEUROTRIGO,
                 GRANJA_ALGACARNE,
                 CRIADERO_RATAX,
                 CULTIVO_FLORSOMNIO -> {
                return agricultura;
            }

            case LAB_REFLEXA,
                 LAB_NANOCURA,
                 LAB_SOMNEX -> {
                return ciencia;
            }

            default -> {
                return ingenieria;
            }
        }
    }

    // ================= GETTERS =================

    public int getMineria() { return mineria; }

    public int getAgricultura() { return agricultura; }

    public int getCiencia() { return ciencia; }

    public int getIngenieria() { return ingenieria; }
}