package com.cyberpunk.domain.colonia;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cyberpunk.domain.defensa.Defensas;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    @ManyToOne
    @JoinColumn(name = "sector_nave_id")
    private MapSector sectorNave;

    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private final List<Personaje> poblacion = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recursos_id")
    private Recursos recursos;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "defensas_id")
    private Defensas defensas;

    @OneToMany(mappedBy = "colonia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private final List<ConstruccionEnCurso> colaConstruccion = new LinkedList<>();
    
    //Edificios de nave
    private int placasSolares = 1;
    private int baterias = 1;

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

    public Recursos getRecursos() { return recursos; }

    public Defensas getDefensas() { return defensas; }

    public List<ConstruccionEnCurso> getColaConstruccion() { return colaConstruccion; }

    public MapSector getSectorNave() { return sectorNave; }

    public int getPlacasSolares() { return placasSolares; }

    public int getBaterias() { return baterias; }

    // ================= RELACIONES =================

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setSectorNave(MapSector sectorNave) {
        this.sectorNave = sectorNave;
    }

    public void setRecursos(Recursos recursos) {
        this.recursos = recursos;
    }

    public void setDefensas(Defensas defensas) {
        this.defensas = defensas;
    }

    public void setPlacasSolares(int placasSolares) {
        this.placasSolares = placasSolares;
    }

    public void setBaterias(int baterias) {
        this.baterias = baterias;
    }

    public void addPersonaje(Personaje p) {
        poblacion.add(p);
        p.setColonia(this);
    }

    public void addConstruccion(ConstruccionEnCurso construccion) {
        colaConstruccion.add(construccion);
        construccion.setColonia(this);
    }
}