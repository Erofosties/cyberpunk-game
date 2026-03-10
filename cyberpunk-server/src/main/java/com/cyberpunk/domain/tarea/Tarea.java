package com.cyberpunk.domain.tarea;

import jakarta.persistence.*;

import com.cyberpunk.domain.edificio.Edificio;

@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoTarea tipo;

    @ManyToOne
    @JoinColumn(name = "edificio_id")
    private Edificio edificio;

    private int progreso;

    private int progresoNecesario = 100;

    // ================= ENUM =================

    public enum TipoTarea {
        CONSTRUCCION,
        PRODUCCION,
        EXPLORACION,
        DEFENSA,
        DESCANSO
    }

    // ================= CONSTRUCTORES =================

    public Tarea() {}

    public Tarea(TipoTarea tipo, Edificio edificio) {

        this.tipo = tipo;
        this.edificio = edificio;
        this.progreso = 0;
    }

    // ================= LÓGICA =================

    public void avanzar(int cantidad) {

        progreso += cantidad;
    }

    public boolean completada() {

        return progreso >= progresoNecesario;
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public TipoTarea getTipo() {
        return tipo;
    }

    public Edificio getEdificio() {
        return edificio;
    }

    public int getProgreso() {
        return progreso;
    }

}