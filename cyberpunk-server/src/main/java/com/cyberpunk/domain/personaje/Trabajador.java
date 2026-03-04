package com.cyberpunk.domain.personaje;

import jakarta.persistence.*;

@Entity
@Table(name="trabajadores")

public class Trabajador extends Personaje {
    public enum Profession {
        GRINDER,
        AGROTECH,
        FIXER,
        NEONIST,
        TECHIES
    } 
    private Profession profession;

    public Trabajador(String nombre,int vida, Profession profession) {
        super(nombre, vida);
        this.profession = profession;
    }

    public Profession getProfession() {
    	return profession;
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

