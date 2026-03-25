package com.cyberpunk.domain.map;

import java.util.ArrayList;
import java.util.List;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.usuario.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    @Enumerated(EnumType.STRING)
    private TipoEdificio building;

    private Boolean generadorNeonActivo;

    // 🔴 CORREGIDO
    private int buildingLevel = 0;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Usuario owner;

    @OneToMany(mappedBy = "sectorNave")
    @JsonIgnore
    private final List<Colonia> coloniasConNave = new ArrayList<>();

    public MapSector() {}

    public MapSector(int x, int y, TerrainType terrain, SectorResource sectorResource, double richness) {
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.sectorResource = sectorResource;
        this.richness = richness;
        this.buildingLevel = 0;
    }

    public boolean tieneEdificio() {
        return building != null;
    }

    public boolean tieneDefensaSectorial() {
        return building == TipoEdificio.ESCUDO_SECTOR
                || building == TipoEdificio.TORRETA_NEOCROMO
                || building == TipoEdificio.CANON_HEXALIUM;
    }

    @JsonIgnore
    public int getCantidadEscudosSector() {
        return building == TipoEdificio.ESCUDO_SECTOR ? Math.max(1, buildingLevel) : 0;
    }

    @JsonIgnore
    public int getCantidadTorretasSector() {
        return building == TipoEdificio.TORRETA_NEOCROMO ? Math.max(1, buildingLevel) : 0;
    }

    @JsonIgnore
    public int getCantidadCanonesSector() {
        return building == TipoEdificio.CANON_HEXALIUM ? Math.max(1, buildingLevel) : 0;
    }

    @JsonIgnore
    public int getCantidadDefensasSector() {
        return getCantidadEscudosSector() + getCantidadTorretasSector() + getCantidadCanonesSector();
    }

    @JsonIgnore
    public int getPenalizacionExploracionSector() {
        return getCantidadEscudosSector() * 2;
    }

    @JsonProperty("cantidadEscudosSector")
    public int getCantidadEscudosSectorVista() {
        Colonia coloniaNave = getColoniaConNave();
        if (coloniaNave != null) {
            return coloniaNave.getDefensas().getEscudos();
        }

        return getCantidadEscudosSector();
    }

    @JsonProperty("cantidadTorretasSector")
    public int getCantidadTorretasSectorVista() {
        Colonia coloniaNave = getColoniaConNave();
        if (coloniaNave != null) {
            return coloniaNave.getDefensas().getTorretasNeocromo();
        }

        return getCantidadTorretasSector();
    }

    @JsonProperty("cantidadCanonesSector")
    public int getCantidadCanonesSectorVista() {
        Colonia coloniaNave = getColoniaConNave();
        if (coloniaNave != null) {
            return coloniaNave.getDefensas().getCanonesHexalium();
        }

        return getCantidadCanonesSector();
    }

    @JsonProperty("cantidadDefensasSector")
    public int getCantidadDefensasSectorVista() {
        Colonia coloniaNave = getColoniaConNave();
        if (coloniaNave != null) {
            return coloniaNave.getDefensas().getEscudos()
                    + coloniaNave.getDefensas().getTorretasNeocromo()
                    + coloniaNave.getDefensas().getCanonesHexalium();
        }

        return getCantidadDefensasSector();
    }

    @JsonProperty("penalizacionExploracionSector")
    public int getPenalizacionExploracionSectorVista() {
        Colonia coloniaNave = getColoniaConNave();
        if (coloniaNave != null) {
            return coloniaNave.getDefensas().getPenalizacionExploracion();
        }

        return getPenalizacionExploracionSector();
    }

    public int calcularDanioConstanteDefensaSector() {
        int base = (getCantidadTorretasSector() * 3) + (getCantidadCanonesSector() * 5);
        return Math.max(0, base / 2);
    }

    public boolean defensasActivasSector() {
        return getCantidadTorretasSector() > 0 || getCantidadCanonesSector() > 0;
    }

    public void recibirAtaqueDefensaSector(int fuerzaEnemiga) {
        if (!tieneDefensaSectorial() || fuerzaEnemiga <= 0) {
            return;
        }

        int impacto = Math.max(1, fuerzaEnemiga / 20);
        buildingLevel = Math.max(0, buildingLevel - impacto);

        if (buildingLevel == 0) {
            building = null;
            generadorNeonActivo = null;
        }
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

    @JsonIgnore
    public boolean isGeneradorNeonActivo() { return Boolean.TRUE.equals(generadorNeonActivo); }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getGeneradorNeonActivo() {
        if (building != TipoEdificio.GENERADOR_NEON) {
            return null;
        }
        return generadorNeonActivo;
    }

    public Usuario getOwner() { return owner; }

    // ================= SETTERS =================

    public void setBuilding(TipoEdificio building) {
        this.building = building;
        this.buildingLevel = 1; // 🔴 importante
        this.generadorNeonActivo = building == TipoEdificio.GENERADOR_NEON ? Boolean.TRUE : null;
    }

    public void setX(int x ) { this.x=x; }

    public void setY(int y) { this.y=y; }

    public void setOwner(Usuario owner) {
        this.owner = owner;
    }

    public void addColoniaConNave(Colonia colonia) {
        if (colonia == null) {
            return;
        }

        if (!coloniasConNave.contains(colonia)) {
            coloniasConNave.add(colonia);
        }
    }

    public void removeColoniaConNave(Colonia colonia) {
        if (colonia == null) {
            return;
        }

        coloniasConNave.remove(colonia);
    }

    public void setBuildingLevel(int buildingLevel) {
        this.buildingLevel = buildingLevel;
    }

    public void setGeneradorNeonActivo(boolean generadorNeonActivo) {
        if (building != TipoEdificio.GENERADOR_NEON) {
            this.generadorNeonActivo = null;
            return;
        }
        this.generadorNeonActivo = generadorNeonActivo;
    }

    private Colonia getColoniaConNave() {
        if (coloniasConNave.isEmpty()) {
            return null;
        }

        return coloniasConNave.get(0);
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