package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;

import com.cyberpunk.domain.map.MapSector;
import com.fasterxml.jackson.annotation.JsonBackReference;

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

    public Long getId() { return id; }

    public String getTipo() { return tipo; }

    public MapSector getSectorDestino() { return sectorDestino; }

    public void setColonia(Colonia colonia) { this.colonia = colonia; }
}