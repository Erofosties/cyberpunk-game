package com.cyberpunk.domain.colonia;

import com.cyberpunk.domain.map.MapSector;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "construcciones")
public class ConstruccionEnCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    private int progreso;

    private int progresoNecesario;

    @ManyToOne
    @JoinColumn(name = "colonia_id")
    @JsonBackReference
    private Colonia colonia;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private MapSector sectorDestino;

    public ConstruccionEnCurso() {}

    public ConstruccionEnCurso(String tipo, MapSector sector) {
        this.tipo = tipo;
        this.sectorDestino = sector;
        this.progreso = 0;
        this.progresoNecesario = 100;
    }

    public void avanzarConstruccion(int puntos) {

        progreso += puntos;

        if (progreso > progresoNecesario)
            progreso = progresoNecesario;
    }

    public boolean completada() {
        return progreso >= progresoNecesario;
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public int getProgreso() {
        return progreso;
    }

    public int getProgresoNecesario() {
        return progresoNecesario;
    }

    public Colonia getColonia() {
        return colonia;
    }

    public MapSector getSectorDestino() {
        return sectorDestino;
    }

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }

    public void setSectorDestino(MapSector sectorDestino) {
        this.sectorDestino = sectorDestino;
    }
}