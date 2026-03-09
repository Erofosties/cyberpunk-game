package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.cyberpunk.domain.edificio.Edificio;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "construcciones")
public class ConstruccionEnCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    private LocalDateTime tiempoInicio;

    private int duracionSegundos;

    @ManyToOne
    @JoinColumn(name = "colonia_id", nullable = false)
    @JsonBackReference
    private Colonia colonia;

    @ManyToOne
    @JoinColumn(name = "edificio_id", nullable = false)
    private Edificio edificio;

    private LocalDateTime fechaFin;

    // Constructor obligatorio JPA
    public ConstruccionEnCurso() {}

    public ConstruccionEnCurso(String tipo) {
        this.tipo = tipo;
        this.tiempoInicio = LocalDateTime.now();
        this.duracionSegundos = 60;
        this.fechaFin = tiempoInicio.plusSeconds(duracionSegundos);
    }

    public ConstruccionEnCurso(
            Colonia colonia,
            Edificio edificio,
            int segundosBase,
            int techiesAsignados) {

        this.colonia = colonia;
        this.edificio = edificio;

        double reduccion = 1 - (techiesAsignados * 0.05);
        if (reduccion < 0.5) reduccion = 0.5;

        int segundosFinal = (int) (segundosBase * reduccion);

        this.tiempoInicio = LocalDateTime.now();
        this.duracionSegundos = segundosFinal;
        this.fechaFin = tiempoInicio.plusSeconds(segundosFinal);
    }

    // ================= LÓGICA =================

    public boolean finalizada() {
        return LocalDateTime.now().isAfter(fechaFin);
    }

    public void completar() {
        edificio.subirNivel();
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public Colonia getColonia() { return colonia; }

    public Edificio getEdificio() { return edificio; }

    public LocalDateTime getFechaFin() { return fechaFin; }

    public LocalDateTime getTiempoInicio() { return tiempoInicio; }

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }

    public void setEdificio(Edificio edificio) {
        this.edificio = edificio;
    }
}