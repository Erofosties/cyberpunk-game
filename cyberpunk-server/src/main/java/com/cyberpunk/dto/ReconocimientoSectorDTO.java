package com.cyberpunk.dto;

public class ReconocimientoSectorDTO {

    private int x;
    private int y;
    private String terrain;
    private String resource;
    private Double richness;
    private String building;
    private Integer buildingLevel;
    private Long ownerId;
    private Boolean ocupado;
    private Integer defensoresDetectados;
    private Integer nivelCupulas;

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

    public Boolean getOcupado() { return ocupado; }
    public void setOcupado(Boolean ocupado) { this.ocupado = ocupado; }

    public Integer getDefensoresDetectados() { return defensoresDetectados; }
    public void setDefensoresDetectados(Integer defensoresDetectados) { this.defensoresDetectados = defensoresDetectados; }

    public Integer getNivelCupulas() { return nivelCupulas; }
    public void setNivelCupulas(Integer nivelCupulas) { this.nivelCupulas = nivelCupulas; }
}