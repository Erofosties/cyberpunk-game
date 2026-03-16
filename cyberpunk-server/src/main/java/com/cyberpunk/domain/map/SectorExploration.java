package com.cyberpunk.domain.map;

import jakarta.persistence.*;

import com.cyberpunk.domain.usuario.Usuario;

@Entity
@Table(
    name = "sector_exploration",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "sector_id"})
)
public class SectorExploration {

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

    public SectorExploration() {}

    public SectorExploration(Usuario usuario, MapSector sector) {
        this.usuario = usuario;
        this.sector = sector;
        this.visible = true;
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

    // ================= SETTERS =================

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}