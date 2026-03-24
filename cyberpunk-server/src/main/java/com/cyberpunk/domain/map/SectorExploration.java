package com.cyberpunk.domain.map;

import com.cyberpunk.domain.usuario.Usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "sector_exploration",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "sector_id"})
)
public class SectorExploration {

    public static final int INTEL_BASE = 1;
    public static final int INTEL_TACTICO = 2;
    public static final int INTEL_COMPLETO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private MapSector sector;

    private boolean visible;

    private int intelNivel;

    public SectorExploration() {}

    public SectorExploration(Usuario usuario, MapSector sector) {
        this.usuario = usuario;
        this.sector = sector;
        this.visible = true;
        this.intelNivel = INTEL_COMPLETO;
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public MapSector getSector() {
        return sector;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getIntelNivel() {
        return intelNivel;
    }

    // ================= SETTERS =================

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setIntelNivel(int intelNivel) {
        this.intelNivel = Math.max(INTEL_BASE, Math.min(INTEL_COMPLETO, intelNivel));
    }
}