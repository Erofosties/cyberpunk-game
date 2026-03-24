package com.cyberpunk.dto;

import java.util.List;

public class ReconocimientoSectorDTO {

    private int x;
    private int y;
    private String terrain;
    private String resource;
    private Double richness;
    private String building;
    private Integer buildingLevel;
    private Long ownerId;
    private Integer cantidadGuerreros;
    private Integer cantidadTrabajadores;
    private Integer cantidadDefensas;
    private DefensasIntelDTO defensas;
    private List<GuerreroIntelDTO> guerreros;

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getTerrain() { return terrain; }
    public void setTerrain(String terrain) { this.terrain = terrain; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public Double getRichness() { return richness; }
    public void setRichness(Double richness) { this.richness = richness; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public Integer getBuildingLevel() { return buildingLevel; }
    public void setBuildingLevel(Integer buildingLevel) { this.buildingLevel = buildingLevel; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Integer getCantidadGuerreros() { return cantidadGuerreros; }
    public void setCantidadGuerreros(Integer cantidadGuerreros) { this.cantidadGuerreros = cantidadGuerreros; }

    public Integer getCantidadTrabajadores() { return cantidadTrabajadores; }
    public void setCantidadTrabajadores(Integer cantidadTrabajadores) { this.cantidadTrabajadores = cantidadTrabajadores; }

    public Integer getCantidadDefensas() { return cantidadDefensas; }
    public void setCantidadDefensas(Integer cantidadDefensas) { this.cantidadDefensas = cantidadDefensas; }

    public DefensasIntelDTO getDefensas() { return defensas; }
    public void setDefensas(DefensasIntelDTO defensas) { this.defensas = defensas; }

    public List<GuerreroIntelDTO> getGuerreros() { return guerreros; }
    public void setGuerreros(List<GuerreroIntelDTO> guerreros) { this.guerreros = guerreros; }
}