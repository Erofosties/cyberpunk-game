package com.cyberpunk.domain.personaje;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.gameBalance.GameBalance;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_personaje")
public abstract class Personaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int vida;

    private int hambre = 100;

    private int cansancio = 0;

    // NUEVO: sector donde trabaja
    @ManyToOne
    @JoinColumn(name = "sector_id")
    private MapSector sectorAsignado;

    @ManyToOne
    @JoinColumn(name = "construccion_id")
    private ConstruccionEnCurso construccionAsignada;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    public Personaje() {}

    public Personaje(String nombre, int vida) {
        this.nombre = nombre;
        this.vida = vida;
    }

    // ================= TICK BIOLÓGICO =================

    public void tickHambre() {

        hambre -= 1;

        if (hambre < 0)
            hambre = 0;
    }

    public boolean estaDisponible() {

        return sectorAsignado == null && construccionAsignada == null;
    }
    public MapSector getSectorActual() {

        if (sectorAsignado != null) {
            return sectorAsignado;
        }

        if (colonia != null) {
            return colonia.getSectorNave();
        }

        return null;
    }
    public void descansar() {

        if (hambre <= 0)
            return;

        if (cansancio <= 0)
            return;

        reducirCansancio(3);
        reducirHambre(1);
    }

    public void intentarComer(Recursos recursos) {

        if (hambre > 40)
            return;

        if (!estaDisponible())
            return;

        ResourceType comida = recursos.consumirComidaDisponible();

        if (comida == null)
            return;

        int recuperacion = GameBalance.getRecuperacionHambre(comida);

        comer(recuperacion);
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

    // ================= HAMBRE =================

    public void reducirHambre(int cantidad) {

        hambre -= cantidad;

        if (hambre < 0)
            hambre = 0;
    }

    public void comer(int cantidad) {

        hambre += cantidad;

        if (hambre > 100)
            hambre = 100;
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

    public int getHambre() {
        return hambre;
    }

    public int getCansancio() {
        return cansancio;
    }

    public Colonia getColonia() {
        return colonia;
    }

    public MapSector getSectorAsignado() {
        return sectorAsignado;
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
}