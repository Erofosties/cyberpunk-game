package com.cyberpunk.domain.personaje;

import jakarta.persistence.*;

import com.cyberpunk.domain.edificio.Edificio;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="trabajadores")
public class Trabajador extends Personaje {

    // HABILIDADES
    private int mineria;
    private int agricultura;
    private int ciencia;
    private int ingenieria;

    @ManyToOne
    @JoinColumn(name="edificio_id")
    @JsonBackReference
    private Edificio edificio;

    public Trabajador(){}

    public Trabajador(String nombre,int mineria,int agricultura,int ciencia,int ingenieria){
        super(nombre,100);

        this.mineria = mineria;
        this.agricultura = agricultura;
        this.ciencia = ciencia;
        this.ingenieria = ingenieria;
    }

    @Override
    public int getProduccion(){

        if(!puedeActuar()) return 0;

        if(edificio == null) return 0;

        int produccion;

        switch(edificio.getTipo()){

            case MINA_NEOCROMO:
            case MINA_UMBRIUM:
            case MINA_SYNTHERIUM:
            case MINA_HEXALIUM:
            case MINA_VOIDIUM:
                produccion = mineria;
                break;

            case GRANJA_KROMAFRUTA:
            case GRANJA_NEUROTRIGO:
            case GRANJA_ALGACARNE:
            case CRIADERO_RATAX:
            case CULTIVO_FLORSOMNIO:
                produccion = agricultura;
                break;

            case LAB_REFLEXA:
            case LAB_NANOCURA:
            case LAB_SOMNEX:
                produccion = ciencia;
                break;

            default:
                produccion = ingenieria;
        }

        aumentarCansancio(5);
        consumirHambre(2);

        return produccion;
    }

    // ===== GETTERS =====

    public int getMineria(){ return mineria; }

    public int getAgricultura(){ return agricultura; }

    public int getCiencia(){ return ciencia; }

    public int getIngenieria(){ return ingenieria; }

    public Edificio getEdificio(){ return edificio; }

    public void setEdificio(Edificio edificio){
        this.edificio = edificio;
    }
}