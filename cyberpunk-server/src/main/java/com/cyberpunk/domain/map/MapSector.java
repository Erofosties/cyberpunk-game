package com.cyberpunk.domain.map;

import jakarta.persistence.*;

import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.usuario.Usuario;

@Entity
@Table(
    name = "map_sectors",
    uniqueConstraints = @UniqueConstraint(columnNames = {"x", "y"})
)
public class MapSector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;

    @Enumerated(EnumType.STRING)
    private TerrainType terrain;

    @Enumerated(EnumType.STRING)
    private SectorResource sectorResource;

    private double richness;

    // edificio presente en el sector
    @Enumerated(EnumType.STRING)
    private TipoEdificio building;

    // nivel del edificio
    private int buildingLevel = 1;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Usuario owner;

    public MapSector() {}

    public MapSector(int x, int y, TerrainType terrain, SectorResource sectorResource, double richness) {
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.sectorResource = sectorResource;
        this.richness = richness;
    }

    public boolean tieneEdificio() {
        return building != null;
    }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public int getX() { return x; }

    public int getY() { return y; }

    public TerrainType getTerrain() { return terrain; }

    public SectorResource getResource() { return sectorResource; }

    public double getRichness() { return richness; }

    public TipoEdificio getBuilding() { return building; }

    public int getBuildingLevel() { return buildingLevel; }

    public Usuario getOwner() { return owner; }

    // ================= SETTERS =================

    public void setBuilding(TipoEdificio building) {
        this.building = building;
    }
    public void setX(int x ) { this.x=x; }

    public void setY(int y) { this.y=y; }
    
    public void setOwner(Usuario owner) {
        this.owner = owner;
    }

    public void setBuildingLevel(int buildingLevel) {
        this.buildingLevel = buildingLevel;
    }

    // ================= ENUMS =================

    public enum TerrainType {

        LLANURA,
        MONTAÑA,
        BOSQUE,
        DESIERTO,
        PANTANO
    }

    public enum SectorResource {

        NEOCROMO,
        UMBRIUM,
        SYNTHERIUM,
        HEXALIUM,
        VOIDIUM,

        KROMAFRUTA,
        NEUROTRIGO,
        ALGACARNE,
        RATAX,
        FLORSOMNIO,

        REFLEXA,
        NANOCURA,
        SOMNEX,

        NONE
    }
}