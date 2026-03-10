package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cyberpunk.domain.defensa.Defensas;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.usuario.Usuario;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "colonias")
public class Colonia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Personaje> poblacion = new ArrayList<>();

    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Edificio> edificios = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recursos_id")
    private Recursos recursos;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "defensas_id")
    @JsonManagedReference
    private Defensas defensas;

    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ConstruccionEnCurso> colaConstruccion = new LinkedList<>();

    public Colonia() {
        this.recursos = new Recursos();
        this.defensas = new Defensas();
    }

    public Colonia(String nombre) {
        this.nombre = nombre;
        this.recursos = new Recursos();
        this.defensas = new Defensas();
    }

    // ================= GETTERS =================

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Usuario getUsuario() { return usuario; }
    public List<Personaje> getPoblacion() { return poblacion; }
    public List<Edificio> getEdificios() { return edificios; }
    public Recursos getRecursos() { return recursos; }
    public Defensas getDefensas() { return defensas; }
    public List<ConstruccionEnCurso> getColaConstruccion() { return colaConstruccion; }

    // ================= RELACIONES =================

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void addPersonaje(Personaje p) {
        poblacion.add(p);
        p.setColonia(this);
    }

    public void addEdificio(Edificio e) {
        edificios.add(e);
        e.setColonia(this);
    }

    public void addConstruccion(ConstruccionEnCurso construccion) {
        colaConstruccion.add(construccion);
        construccion.setColonia(this);
    }

    // ================= ENERGÍA =================

    public double calcularFactorEnergia() {

        int produccion = 0;
        int consumo = 0;

        for (Edificio e : edificios) {

            // de momento sin trabajadores asignados
            produccion += e.getProduccionEnergia(0);

            consumo += e.getConsumoEnergia();
        }

        if (consumo == 0) return 1.0;

        if (produccion >= consumo) return 1.0;

        return (double) produccion / consumo;
    }

    // ================= CONSTRUCCIÓN =================

    public void procesarConstrucciones() {

        List<ConstruccionEnCurso> terminadas = new ArrayList<>();

        for (ConstruccionEnCurso c : colaConstruccion) {

            int progreso = 0;

            for (Personaje p : poblacion) {

                if (p instanceof Trabajador t) {

                    if (t.getIngenieria() > 0 && t.getCansancio() < 100) {

                        progreso += t.getIngenieria();

                        t.aumentarCansancio(2);
                    }
                }
            }

            if (progreso > 0) {
                c.avanzarConstruccion(progreso);
            }

            if (c.completada()) {

                TipoEdificio tipo = TipoEdificio.valueOf(c.getTipo());

                Edificio edificio = new Edificio(tipo);

                addEdificio(edificio);

                terminadas.add(c);
            }
        }

        colaConstruccion.removeAll(terminadas);
    }

    // ================= PRODUCCIÓN =================

    public void producirRecursos() {

        double factorEnergia = calcularFactorEnergia();

        for (Edificio e : edificios) {

            int produccionTrabajadores = 0;

            for (Personaje p : poblacion) {

                if (p instanceof Trabajador t) {

                    if (t.getCansancio() >= 100) continue;

                    produccionTrabajadores += t.getProduccion();

                    t.aumentarCansancio(1);
                }
            }

            int produccion = e.producir(produccionTrabajadores, factorEnergia);

            recursos.add(e.getRecursoProduce(), produccion);
        }
    }
}