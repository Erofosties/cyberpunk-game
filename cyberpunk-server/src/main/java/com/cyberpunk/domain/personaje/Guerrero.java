package com.cyberpunk.domain.personaje;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.*;

@Entity
@Table(name = "guerreros")
public class Guerrero extends Personaje {

    public enum Tipo {
        CHOPPER,
        RUNNER,
        GUNNER,
        TANKER,
        GLITCHER,
        TROYWORM
    }

    private static final Map<Tipo, Stats> STATS_BASE = Map.of(

        Tipo.CHOPPER, new Stats(120,3,3,3,0,
                new String[]{"Hierroto","Oxigeno","Mordaz","Rajas","Navaja","Mandibula"}),

        Tipo.RUNNER, new Stats(50,1,4,1,3,
                new String[]{"DustRunner","GravBlink","SolarSkim","SporeHop"}),

        Tipo.GUNNER, new Stats(100,6,3,3,0,
                new String[]{"DisparoFulgor","CañonNeón","RayoLetal"}),

        Tipo.TANKER, new Stats(250,4,3,5,0,
                new String[]{"MuroDeAcero","Blindado","Coloso"}),

        Tipo.GLITCHER, new Stats(100,1,3,3,8,
                new String[]{"HackerMan","SoyProgramador","HacheTemele"}),

        Tipo.TROYWORM, new Stats(80,4,7,3,3,
                new String[]{"Gusano","Backdoor","Spaguetti"})
    );

    private static class Stats {

        int vida;
        int fuerza;
        int destreza;
        int resistencia;
        int hackeo;

        String[] nombres;

        Stats(int vida,int fuerza,int destreza,int resistencia,int hackeo,String[] nombres){
            this.vida = vida;
            this.fuerza = fuerza;
            this.destreza = destreza;
            this.resistencia = resistencia;
            this.hackeo = hackeo;
            this.nombres = nombres;
        }
    }

    // ================= ATRIBUTOS =================

    private int fuerza;
    private int destreza;
    private int resistencia;
    private int hackeo;

    private boolean disponible;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private int unidadesExploradas;

    // Constructor obligatorio JPA
    public Guerrero() {}

    public Guerrero(Tipo tipo) {

        super(selectNombre(tipo), STATS_BASE.get(tipo).vida);

        Stats s = STATS_BASE.get(tipo);

        this.tipo = tipo;

        this.fuerza = s.fuerza;
        this.resistencia = s.resistencia;
        this.destreza = s.destreza;
        this.hackeo = s.hackeo;

        this.disponible = true;
    }

    // ================= GENERACIÓN NOMBRE =================

    private static String selectNombre(Tipo tipo) {

        String[] nombres = STATS_BASE.get(tipo).nombres;

        return nombres[ThreadLocalRandom.current().nextInt(nombres.length)];
    }

    // ================= PRODUCCIÓN =================

    @Override
    public int getProduccion() {
        return 0;
    }

    // ================= GETTERS =================

    public Tipo getTipo() { return tipo; }

    public int getFuerza() { return fuerza; }

    public int getResistencia() { return resistencia; }

    public int getDestreza() { return destreza; }

    public int getHackeo() { return hackeo; }

    public boolean isDisponible() { return disponible; }

    // ================= MODIFICADORES =================

    public void modificarFuerza(int mod) { this.fuerza += mod; }

    public void modificarResistencia(int mod) { this.resistencia += mod; }

    public void modificarDestreza(int mod) { this.destreza += mod; }

    public void modificarHackeo(int mod) { this.hackeo += mod; }

    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    // ================= EXPLORACIÓN =================

    public int explorar(int tiempo) {

        if (tipo != Tipo.RUNNER) {
            throw new UnsupportedOperationException("Solo los Runners pueden explorar");
        }

        unidadesExploradas = tiempo * (destreza + ThreadLocalRandom.current().nextInt(2));

        return unidadesExploradas;
    }
}