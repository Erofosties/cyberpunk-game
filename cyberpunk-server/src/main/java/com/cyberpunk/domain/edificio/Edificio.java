package com.cyberpunk.domain.edificio;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.personaje.Trabajador.Profession;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;

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
    private Colonia colonia;

    @OneToMany
    @JoinColumn(name = "edificio_id")
    private List<Trabajador> trabajadores = new ArrayList<>();

    public Edificio() {}

    public Edificio(TipoEdificio tipo) {
        this.tipo = tipo;
        this.nivel = nivelInicialPorTipo(tipo);
        this.vidaEstructural = 100;
        this.recursoProduce = mapTipoToRecurso(tipo);
        configurarEnergia();
    }

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

    private void configurarEnergia() {
        switch (tipo) {
            case PLACA_SOLAR -> produccionEnergiaBase = 30;
            case REACTOR_FUSION -> produccionEnergiaBase = 70;
            case GENERADOR_NEON -> produccionEnergiaBase = 50;
            default -> consumoEnergiaBase = 15;
        }
    }

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

    public int producir(double factorEnergia) {

        if (nivel == 0) return 0;
        if (esEdificioEnergetico()) return 0;

        int total = 0;

        for (Trabajador t : trabajadores) {

            int base = t.getProduccion();

            total += profesionCompatible(t.getProfession()) ? base : base / 2;
        }

        double bonusNivel = nivel * Math.pow(1.1, nivel);

        return (int) Math.round(total * bonusNivel * factorEnergia);
    }

    private boolean esEdificioEnergetico() {
        return tipo == TipoEdificio.PLACA_SOLAR
                || tipo == TipoEdificio.REACTOR_FUSION
                || tipo == TipoEdificio.GENERADOR_NEON;
    }

    public int getProduccionEnergia() {

        if (!esEdificioEnergetico()) return 0;

        int techiesAsignados = trabajadores.size();

        double bonusTechies = 1 + (techiesAsignados * 0.15);

        return (int)(
                produccionEnergiaBase
                        * nivel
                        * Math.pow(1.05, nivel)
                        * bonusTechies
        );
    }

    public int getConsumoEnergia() {
        return (int)(consumoEnergiaBase * nivel * Math.pow(1.08, nivel));
    }

    public void subirNivel() {
        if (nivel < getNivelMaximo())
            nivel++;
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

    public boolean addTrabajador(Trabajador t) {

        if (!profesionCompatible(t.getProfession()))
            return false;

        trabajadores.add(t);

        return true;
    }

    public void removeTrabajador(Trabajador t) {
        trabajadores.remove(t);
    }

    public Map<ResourceType, Integer> costeSiguienteNivel() {

        Map<ResourceType, Integer> coste = new EnumMap<>(ResourceType.class);

        int nivelSiguiente = nivel + 1;

        switch (tipo) {

            case MINA_NEOCROMO ->
                    coste.put(ResourceType.NEOCROMO, (int)(100 * Math.pow(1.8, nivelSiguiente)));

            case MINA_UMBRIUM ->
                    coste.put(ResourceType.UMBRIUM, (int)(120 * Math.pow(1.8, nivelSiguiente)));

            case MINA_SYNTHERIUM ->
                    coste.put(ResourceType.SYNTHERIUM, (int)(140 * Math.pow(1.8, nivelSiguiente)));

            case MINA_HEXALIUM ->
                    coste.put(ResourceType.HEXALIUM, (int)(160 * Math.pow(1.8, nivelSiguiente)));

            case MINA_VOIDIUM ->
                    coste.put(ResourceType.VOIDIUM, (int)(200 * Math.pow(1.8, nivelSiguiente)));

            case GRANJA_KROMAFRUTA ->
                    coste.put(ResourceType.KROMAFRUTA, (int)(80 * Math.pow(1.6, nivelSiguiente)));

            case GRANJA_NEUROTRIGO ->
                    coste.put(ResourceType.NEUROTRIGO, (int)(80 * Math.pow(1.6, nivelSiguiente)));

            case GRANJA_ALGACARNE ->
                    coste.put(ResourceType.ALGACARNE, (int)(90 * Math.pow(1.6, nivelSiguiente)));

            case CRIADERO_RATAX ->
                    coste.put(ResourceType.RATAX, (int)(100 * Math.pow(1.6, nivelSiguiente)));

            case CULTIVO_FLORSOMNIO ->
                    coste.put(ResourceType.FLORSOMNIO, (int)(110 * Math.pow(1.6, nivelSiguiente)));

            case LAB_REFLEXA ->
                    coste.put(ResourceType.REFLEXA, (int)(150 * Math.pow(1.7, nivelSiguiente)));

            case LAB_NANOCURA ->
                    coste.put(ResourceType.NANOCURA, (int)(150 * Math.pow(1.7, nivelSiguiente)));

            case LAB_SOMNEX ->
                    coste.put(ResourceType.SOMNEX, (int)(170 * Math.pow(1.7, nivelSiguiente)));

            case PLACA_SOLAR ->
                    coste.put(ResourceType.NEOCROMO, (int)(150 * Math.pow(1.7, nivelSiguiente)));

            case REACTOR_FUSION ->
                    coste.put(ResourceType.SYNTHERIUM, (int)(200 * Math.pow(1.7, nivelSiguiente)));

            case GENERADOR_NEON ->
                    coste.put(ResourceType.HEXALIUM, (int)(180 * Math.pow(1.7, nivelSiguiente)));
        }

        return coste;
    }

    private boolean profesionCompatible(Profession p) {

        return switch (tipo) {

            case MINA_NEOCROMO,
                 MINA_UMBRIUM,
                 MINA_SYNTHERIUM,
                 MINA_HEXALIUM,
                 MINA_VOIDIUM -> p == Profession.GRINDER;

            case GRANJA_KROMAFRUTA,
                 GRANJA_NEUROTRIGO,
                 GRANJA_ALGACARNE,
                 CRIADERO_RATAX,
                 CULTIVO_FLORSOMNIO -> p == Profession.AGROTECH;

            case LAB_REFLEXA,
                 LAB_NANOCURA,
                 LAB_SOMNEX -> p == Profession.FIXER;

            case PLACA_SOLAR,
                 REACTOR_FUSION,
                 GENERADOR_NEON -> p == Profession.TECHIES;

            default -> false;
        };
    }

    public Long getId() { return id; }

    public TipoEdificio getTipo() { return tipo; }

    public int getNivel() { return nivel; }

    public ResourceType getRecursoProduce() { return recursoProduce; }

    public List<Trabajador> getTrabajadores() { return trabajadores; }

    public void setColonia(Colonia colonia) { this.colonia = colonia; }
}