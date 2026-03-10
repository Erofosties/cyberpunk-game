package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;
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

    public ConstruccionEnCurso() {}

    public ConstruccionEnCurso(String tipo) {

        this.tipo = tipo;

        this.progreso = 0;

        this.progresoNecesario = 100;
    }

    // ================= LÓGICA =================

    public void avanzarConstruccion(int puntos) {

        progreso += puntos;

        if (progreso > progresoNecesario) {
            progreso = progresoNecesario;
        }
    }

    public boolean completada() {

        return progreso >= progresoNecesario;
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public String getTipo() { return tipo; }

    public int getProgreso() { return progreso; }

    public int getProgresoNecesario() { return progresoNecesario; }

    public Colonia getColonia() { return colonia; }

    // ================= SETTERS =================

    public void setColonia(Colonia colonia) {
        this.colonia = colonia;
    }
}