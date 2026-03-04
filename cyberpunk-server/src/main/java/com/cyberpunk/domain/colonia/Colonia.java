package com.cyberpunk.domain.colonia;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cyberpunk.domain.defensa.Defensas;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "colonias")
public class Colonia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // 🔗 Relación con Usuario (dueño de la colonia)
    @OneToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    // 🔗 Personajes
    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Personaje> poblacion = new ArrayList<>();

    // 🔗 Edificios
    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Edificio> edificios = new ArrayList<>();

    // 🔗 Recursos (1 a 1)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recursos_id")
    private Recursos recursos;

    // 🔗 Defensas (1 a 1)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "defensas_id")
    private Defensas defensas;

    // Construcciones en curso
    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConstruccionEnCurso> colaConstruccion = new LinkedList<>();

    // Constructor obligatorio para JPA
    public Colonia() {
    }

    public Colonia(String nombre) {
        this.nombre = nombre;
        this.recursos = new Recursos();
        this.defensas = new Defensas();
    }
    
    public void addConstruccion(ConstruccionEnCurso construccion) {
        colaConstruccion.add(construccion);
        construccion.setColonia(this);
    }

    // ================== GETTERS ==================

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Usuario getUsuario() { return usuario; }
    public List<Personaje> getPoblacion() { return poblacion; }
    public List<Edificio> getEdificios() { return edificios; }
    public Recursos getRecursos() { return recursos; }
    public Defensas getDefensas() { return defensas; }
    public List<ConstruccionEnCurso> getColaConstruccion() { return colaConstruccion; }

    // ================== RELACIONES SEGURAS ==================

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
    
    //calculo energetico
    public double calcularFactorEnergia() {

        int produccion = 0;
        int consumo = 0;

        for (Edificio e : edificios) {
            produccion += e.getProduccionEnergia();
            consumo += e.getConsumoEnergia();
        }

        if (consumo == 0)
            return 1.0;

        if (produccion >= consumo)
            return 1.0;

        return (double) produccion / consumo;
    }
}