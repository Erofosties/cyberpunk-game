package com.cyberpunk.domain.personaje;

import jakarta.persistence.*;

@Entity
@Table(name = "guerreros")
@DiscriminatorValue("GUERRERO")
public class Guerrero extends Personaje {

    private int fuerza;
    private int destreza;
    private int resistencia;
    private int hackeo;

    public Guerrero() {}

    public Guerrero(
            String nombre,
            int fuerza,
            int destreza,
            int resistencia,
            int hackeo) {

        super(nombre, 120);

        this.fuerza = fuerza;
        this.destreza = destreza;
        this.resistencia = resistencia;
        this.hackeo = hackeo;
    }

    // ================= PRODUCCIÓN =================

    @Override
    public int getProduccion() {
        return 0;
    }

    // ================= GETTERS =================

    public int getFuerza() {
        return fuerza;
    }

    public int getDestreza() {
        return destreza;
    }

    public int getResistencia() {
        return resistencia;
    }

    public int getHackeo() {
        return hackeo;
    }

}