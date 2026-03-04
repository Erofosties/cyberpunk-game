package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.cyberpunk.domain.edificio.Edificio;

@Entity
@Table(name = "construcciones")
public class ConstruccionEnCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tipo;
    private LocalDateTime tiempoInicio;
    private int duracionSegundos;

    // 🔗 Relación con colonia (lado dueño del mappedBy)
    @ManyToOne
    @JoinColumn(name = "colonia_id", nullable = false)
    private Colonia colonia;

    // 🔗 Edificio que se está mejorando
    @ManyToOne
    @JoinColumn(name = "edificio_id", nullable = false)
    private Edificio edificio;

    private LocalDateTime fechaFin;

    // 🔹 Constructor obligatorio para JPA
    public ConstruccionEnCurso() {}
    public ConstruccionEnCurso(String tipo) {
    	this.tipo = tipo;
    	this.tiempoInicio = LocalDateTime.now();
    	this.duracionSegundos = 60;
    }

    public ConstruccionEnCurso(
            Colonia colonia,
            Edificio edificio,
            int segundosBase,
            int techiesAsignados) {

        this.colonia = colonia;
        this.edificio = edificio;

        double reduccion = 1 - (techiesAsignados * 0.05);
        if (reduccion < 0.5) reduccion = 0.5; // máximo 50% reducción

        int segundosFinal = (int) (segundosBase * reduccion);

        this.fechaFin = LocalDateTime.now().plusSeconds(segundosFinal);
    }

    public boolean finalizada() {
        return LocalDateTime.now().isAfter(
            tiempoInicio.plusSeconds(duracionSegundos)
        );
    }

    public void completar() {
        edificio.subirNivel();
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public Colonia getColonia() { return colonia; }

    public Edificio getEdificio() { return edificio; }

    public LocalDateTime getFechaFin() { return fechaFin; }

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }
    public void setEdificio(Edificio edificio) {
        this.edificio = edificio;
    }
    
}