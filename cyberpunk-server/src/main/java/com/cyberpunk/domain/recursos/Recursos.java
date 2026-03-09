package com.cyberpunk.domain.recursos;

import jakarta.persistence.*;
import java.util.Map;
import java.util.EnumMap;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.edificio.Edificio;

@Entity
@Table(name = "recursos")
public class Recursos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long ultimoCalculo;

    // METALES
    private int neocromo;
    private int umbrium;
    private int syntherium;
    private int hexalium;
    private int voidium;

    // COMIDA
    private int kromafruta;
    private int algacarne;
    private int neurotrigo;
    private int ratax;
    private int florsomnio;

    // MEDICINAS
    private int reflexa;
    private int nanocura;
    private int somnex;

    // OTROS
    private int energia;
    private int exploracion;

    public Recursos() {
        this.ultimoCalculo = System.currentTimeMillis();
        
        //recursos iniciales
        this.neocromo = 1000;
        this.umbrium = 300;
        this.kromafruta = 1000;
        this.algacarne = 300;
        this.nanocura =5;
    }

    public Long getId() {
        return id;
    }

    // ================= PRODUCCIÓN =================

    public void producirRecursos(Colonia colonia) {

        long ahora = System.currentTimeMillis();
        long segundos = (ahora - ultimoCalculo) / 1000;

        if (segundos <= 0) return;

        double factorEnergia = colonia.calcularFactorEnergia();

        for (Edificio edificio : colonia.getEdificios()) {

            int produccion = edificio.producir(factorEnergia);

            ResourceType recurso = edificio.getRecursoProduce();

            if (recurso != null && produccion > 0) {

                add(recurso, produccion * (int)segundos);

            }
        }

        ultimoCalculo = ahora;
    }

    // ================= MÉTODOS DE JUEGO =================

    public void add(ResourceType type, int amount) {

        if (amount <= 0) return;

        switch (type) {
            case NEOCROMO -> neocromo += amount;
            case UMBRIUM -> umbrium += amount;
            case SYNTHERIUM -> syntherium += amount;
            case HEXALIUM -> hexalium += amount;
            case VOIDIUM -> voidium += amount;
            case KROMAFRUTA -> kromafruta += amount;
            case ALGACARNE -> algacarne += amount;
            case NEUROTRIGO -> neurotrigo += amount;
            case RATAX -> ratax += amount;
            case FLORSOMNIO -> florsomnio += amount;
            case REFLEXA -> reflexa += amount;
            case NANOCURA -> nanocura += amount;
            case SOMNEX -> somnex += amount;
            case ENERGIA -> energia += amount;
            case EXPLORACION -> exploracion += amount;
        }
    }

    public Map<ResourceType, Integer> getTodos() {

        Map<ResourceType, Integer> mapa = new EnumMap<>(ResourceType.class);

        for (ResourceType type : ResourceType.values()) {
            mapa.put(type, getCantidad(type));
        }

        return mapa;
    }

    public int getCantidad(ResourceType type) {

        return switch (type) {
            case NEOCROMO -> neocromo;
            case UMBRIUM -> umbrium;
            case SYNTHERIUM -> syntherium;
            case HEXALIUM -> hexalium;
            case VOIDIUM -> voidium;
            case KROMAFRUTA -> kromafruta;
            case ALGACARNE -> algacarne;
            case NEUROTRIGO -> neurotrigo;
            case RATAX -> ratax;
            case FLORSOMNIO -> florsomnio;
            case REFLEXA -> reflexa;
            case NANOCURA -> nanocura;
            case SOMNEX -> somnex;
            case ENERGIA -> energia;
            case EXPLORACION -> exploracion;
        };
    }

    // ================= COSTES =================

    public boolean tieneSuficiente(Map<ResourceType, Integer> coste) {

        for (var entry : coste.entrySet()) {

            if (getCantidad(entry.getKey()) < entry.getValue()) {

                return false;

            }

        }

        return true;
    }

    public void consumir(Map<ResourceType, Integer> coste) {

        for (var entry : coste.entrySet()) {

            consumir(entry.getKey(), entry.getValue());

        }

    }

    public void consumir(ResourceType type, int amount) {

        switch (type) {

            case NEOCROMO -> neocromo = Math.max(0, neocromo - amount);
            case UMBRIUM -> umbrium = Math.max(0, umbrium - amount);
            case SYNTHERIUM -> syntherium = Math.max(0, syntherium - amount);
            case HEXALIUM -> hexalium = Math.max(0, hexalium - amount);
            case VOIDIUM -> voidium = Math.max(0, voidium - amount);
            case KROMAFRUTA -> kromafruta = Math.max(0, kromafruta - amount);
            case ALGACARNE -> algacarne = Math.max(0, algacarne - amount);
            case NEUROTRIGO -> neurotrigo = Math.max(0, neurotrigo - amount);
            case RATAX -> ratax = Math.max(0, ratax - amount);
            case FLORSOMNIO -> florsomnio = Math.max(0, florsomnio - amount);
            case REFLEXA -> reflexa = Math.max(0, reflexa - amount);
            case NANOCURA -> nanocura = Math.max(0, nanocura - amount);
            case SOMNEX -> somnex = Math.max(0, somnex - amount);
            case ENERGIA -> energia = Math.max(0, energia - amount);
            case EXPLORACION -> exploracion = Math.max(0, exploracion - amount);

        }
    }

    // ================= ENUM =================

    public enum ResourceType {

        NEOCROMO, UMBRIUM, SYNTHERIUM, HEXALIUM, VOIDIUM,
        KROMAFRUTA, ALGACARNE, NEUROTRIGO, RATAX, FLORSOMNIO,
        REFLEXA, NANOCURA, SOMNEX,
        EXPLORACION,
        ENERGIA

    }
}