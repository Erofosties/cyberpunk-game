package com.cyberpunk.domain.edificio;

import jakarta.persistence.*;
import java.util.List;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "edificios")
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoEdificio tipo;

    @Enumerated(EnumType.STRING)
    private ResourceType recursoProduce;

    private int nivel;

    private int vidaEstructural;

    private int consumoEnergiaBase;

    private int produccionEnergiaBase;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    public Edificio() {}

    public Edificio(TipoEdificio tipo) {

        this.tipo = tipo;
        this.nivel = nivelInicialPorTipo(tipo);
        this.vidaEstructural = 100;
        this.recursoProduce = mapTipoToRecurso(tipo);

        configurarEnergia();
    }

    // ================= TIPOS =================

    public enum TipoEdificio {

        MINA_NEOCROMO,
        MINA_UMBRIUM,
        MINA_SYNTHERIUM,
        MINA_HEXALIUM,
        MINA_VOIDIUM,

        GRANJA_KROMAFRUTA,
        GRANJA_NEUROTRIGO,
        GRANJA_ALGACARNE,
        CRIADERO_RATAX,
        CULTIVO_FLORSOMNIO,

        LAB_REFLEXA,
        LAB_NANOCURA,
        LAB_SOMNEX,

        PLACA_SOLAR,
        REACTOR_FUSION,
        GENERADOR_NEON
    }

    // ================= NIVEL INICIAL =================

    private static int nivelInicialPorTipo(TipoEdificio tipo) {

        return switch (tipo) {

            case MINA_NEOCROMO,
                 MINA_UMBRIUM,
                 MINA_SYNTHERIUM,
                 MINA_HEXALIUM,
                 MINA_VOIDIUM -> 0;

            default -> 1;
        };
    }

    // ================= CONFIGURACIÓN ENERGÍA =================

    private void configurarEnergia() {

        switch (tipo) {

            case PLACA_SOLAR -> produccionEnergiaBase = 30;

            case REACTOR_FUSION -> produccionEnergiaBase = 70;

            case GENERADOR_NEON -> produccionEnergiaBase = 50;

            default -> consumoEnergiaBase = 15;
        }
    }

    // ================= MAPEO RECURSOS =================

    private ResourceType mapTipoToRecurso(TipoEdificio tipo) {

        return switch (tipo) {

            case MINA_NEOCROMO -> ResourceType.NEOCROMO;
            case MINA_UMBRIUM -> ResourceType.UMBRIUM;
            case MINA_SYNTHERIUM -> ResourceType.SYNTHERIUM;
            case MINA_HEXALIUM -> ResourceType.HEXALIUM;
            case MINA_VOIDIUM -> ResourceType.VOIDIUM;

            case GRANJA_KROMAFRUTA -> ResourceType.KROMAFRUTA;
            case GRANJA_NEUROTRIGO -> ResourceType.NEUROTRIGO;
            case GRANJA_ALGACARNE -> ResourceType.ALGACARNE;
            case CRIADERO_RATAX -> ResourceType.RATAX;
            case CULTIVO_FLORSOMNIO -> ResourceType.FLORSOMNIO;

            case LAB_REFLEXA -> ResourceType.REFLEXA;
            case LAB_NANOCURA -> ResourceType.NANOCURA;
            case LAB_SOMNEX -> ResourceType.SOMNEX;

            case PLACA_SOLAR,
                 REACTOR_FUSION,
                 GENERADOR_NEON -> ResourceType.ENERGIA;
        };
    }

    // ================= PRODUCCIÓN =================

    public int producir(int produccionTrabajadores, double factorEnergia) {

        if (nivel == 0) return 0;

        if (esEnergetico()) return 0;

        double bonusNivel = nivel * Math.pow(1.1, nivel);

        return (int) Math.round(produccionTrabajadores * bonusNivel * factorEnergia);
    }

    // ================= ENERGÍA =================

    public boolean esEnergetico() {

        return tipo == TipoEdificio.PLACA_SOLAR
                || tipo == TipoEdificio.REACTOR_FUSION
                || tipo == TipoEdificio.GENERADOR_NEON;
    }

    public int getProduccionEnergia(int trabajadoresAsignados) {

        if (!esEnergetico()) return 0;

        double bonus = 1 + (trabajadoresAsignados * 0.15);

        return (int)(
                produccionEnergiaBase
                        * nivel
                        * Math.pow(1.05, nivel)
                        * bonus
        );
    }

    public int getConsumoEnergia() {

        return (int)(consumoEnergiaBase * nivel * Math.pow(1.08, nivel));
    }

    // ================= NIVEL =================

    public void subirNivel() {

        if (nivel < getNivelMaximo()) {
            nivel++;
        }
    }

    public int getNivelMaximo() {

        return switch (tipo) {

            case GRANJA_KROMAFRUTA,
                 GRANJA_NEUROTRIGO,
                 GRANJA_ALGACARNE,
                 CRIADERO_RATAX,
                 CULTIVO_FLORSOMNIO,
                 LAB_REFLEXA,
                 LAB_NANOCURA,
                 LAB_SOMNEX -> 5;

            default -> 50;
        };
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public TipoEdificio getTipo() {
        return tipo;
    }

    public int getNivel() {
        return nivel;
    }

    public ResourceType getRecursoProduce() {
        return recursoProduce;
    }

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }
}