package com.cyberpunk.domain.personaje;

import jakarta.persistence.*;

import com.cyberpunk.domain.edificio.Edificio;

@Entity
@Table(name = "trabajadores")
public class Trabajador extends Personaje {

    public enum Profession {
        GRINDER,
        AGROTECH,
        FIXER,
        NEONIST,
        TECHIES
    }

    @Enumerated(EnumType.STRING)
    private Profession profession;

    @ManyToOne
    @JoinColumn(name = "edificio_id")
    private Edificio edificio;

    // Constructor obligatorio JPA
    public Trabajador() {}

    public Trabajador(String nombre, int vida, Profession profession) {
        super(nombre, vida);
        this.profession = profession;
    }

    public Profession getProfession() {
        return profession;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public Edificio getEdificio() {
        return edificio;
    }

    public void setEdificio(Edificio edificio) {
        this.edificio = edificio;
    }

    @Override
    public int getProduccion() {

        return switch (profession) {

            case GRINDER -> 10;
            case AGROTECH -> 8;
            case FIXER -> 6;
            case NEONIST -> 4;
            case TECHIES -> 3;
        };
    }
}