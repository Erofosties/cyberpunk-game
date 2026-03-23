package com.cyberpunk.domain.personaje;

import java.util.Random;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.gameBalance.GameBalance;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_personaje")
public abstract class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int vida;

    private int vidaMaxima;

    @Column(name = "hambre")
    private int comida = GameBalance.MAX_COMIDA_PERSONAJE;

    private int cansancio = 0;

    private boolean incapacitado = false;

    private int metabolismoAcumulado = 0;

    // NUEVO: sector donde trabaja
    @ManyToOne
    @JoinColumn(name = "sector_id")
    private MapSector sectorAsignado;

    @ManyToOne
    @JoinColumn(name = "construccion_id")
    private ConstruccionEnCurso construccionAsignada;

    @ManyToOne
    @JoinColumn(name = "sector_transito_id")
    private MapSector sectorTransito;

    private int ticksViajeRestantes = 0;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    public Personaje() {}

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vida;
    }

    @SuppressWarnings("unused")
    @PostLoad
    private void inicializarVidaMaximaSiHaceFalta() {
        if (vidaMaxima <= 0) {
            vidaMaxima = inferirVidaMaxima();
            if (vida > vidaMaxima) {
                vida = vidaMaxima;
            }
        }
    }

    // ================= TICK BIOLÓGICO =================

    public void acumularConsumoComida(int puntos) {

        metabolismoAcumulado += puntos;

        int consumo = metabolismoAcumulado / GameBalance.METABOLISMO_PUNTOS_POR_COMIDA;
        metabolismoAcumulado = metabolismoAcumulado % GameBalance.METABOLISMO_PUNTOS_POR_COMIDA;

        if (consumo > 0) {
            consumirComida(consumo);
        }
    }

    public boolean estaDisponible() {

        return puedeActuar() && !estaEnViaje() && sectorAsignado == null && construccionAsignada == null;
    }
    public MapSector getSectorActual() {

        if (estaEnViaje() && sectorTransito != null) {
            return sectorTransito;
        }

        if (sectorAsignado != null) {
            return sectorAsignado;
        }

        if (colonia != null) {
            return colonia.getSectorNave();
        }

        return null;
    }
    public void descansar() {

        if (!puedeActuar())
            return;

        if (comida <= 0)
            return;

        if (cansancio <= 0)
            return;

        reducirCansancio(3);
    }

    public void intentarComer(Recursos recursos) {

        if (comida > GameBalance.UMBRAL_AUTO_CONSUMO_COMIDA)
            return;

        ResourceType tipoComida = recursos.consumirComidaDisponible();

        if (tipoComida == null)
            return;

        int recuperacion = GameBalance.getRecuperacionComida(tipoComida);

        recuperarComida(recuperacion);
    }

    // ================= CANSANCIO =================

    public void aumentarCansancio(int cantidad) {

        cansancio += cantidad;

        if (cansancio > 100)
            cansancio = 100;
    }

    public void reducirCansancio(int cantidad) {

        cansancio -= cantidad;

        if (cansancio < 0)
            cansancio = 0;
    }

    // ================= COMIDA =================

    public void consumirComida(int cantidad) {

        comida -= cantidad;

        if (comida < 0)
            comida = 0;
    }

    public void recuperarComida(int cantidad) {

        comida += cantidad;

        if (comida > GameBalance.MAX_COMIDA_PERSONAJE)
            comida = GameBalance.MAX_COMIDA_PERSONAJE;
    }

    public boolean sinComida() {
        return comida <= 0;
    }

    public void recibirDanio(int cantidad) {

        if (cantidad <= 0 || incapacitado)
            return;

        vida -= cantidad;

        if (vida <= 0) {
            incapacitar();
        }
    }

    public void curar(int cantidad) {

        if (cantidad <= 0)
            return;

        vida += cantidad;

        if (vida > vidaMaxima)
            vida = vidaMaxima;

        if (vida > 0) {
            incapacitado = false;
        }
    }

    public boolean estaHerido() {
        return vida < vidaMaxima;
    }

    public boolean puedeActuar() {
        return !incapacitado && vida > 0;
    }

    public boolean estaEnViaje() {
        return ticksViajeRestantes > 0;
    }

    public void iniciarViaje(MapSector origen, int ticks) {
        this.sectorTransito = origen;
        this.ticksViajeRestantes = Math.max(0, ticks);
    }

    public boolean avanzarViaje() {
        if (ticksViajeRestantes <= 0) {
            return false;
        }

        ticksViajeRestantes--;

        if (ticksViajeRestantes == 0) {
            sectorTransito = null;
            return true;
        }

        return false;
    }

    public void forzarReposo() {
        this.sectorAsignado = null;
        this.construccionAsignada = null;
    }

    private void incapacitar() {
        vida = 0;
        incapacitado = true;
        forzarReposo();
        perderAtributoAleatorio(new Random());
    }

    private int inferirVidaMaxima() {
        if (this instanceof Guerrero) {
            return 120;
        }
        return 100;
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getComida() {
        return comida;
    }

    public int getCansancio() {
        return cansancio;
    }

    public boolean isIncapacitado() {
        return incapacitado;
    }

    public Colonia getColonia() {
        return colonia;
    }

    public MapSector getSectorAsignado() {
        return sectorAsignado;
    }

    public MapSector getSectorTransito() {
        return sectorTransito;
    }

    public int getTicksViajeRestantes() {
        return ticksViajeRestantes;
    }

    public ConstruccionEnCurso getConstruccionAsignada() {
        return construccionAsignada;
    }

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }

    public void setSectorAsignado(MapSector sectorAsignado) {
        this.sectorAsignado = sectorAsignado;
    }

    public void setConstruccionAsignada(ConstruccionEnCurso construccionAsignada) {
        this.construccionAsignada = construccionAsignada;
    }

    // ================= PRODUCCIÓN =================

    public abstract int getProduccion();

    protected abstract void perderAtributoAleatorio(Random random);
}