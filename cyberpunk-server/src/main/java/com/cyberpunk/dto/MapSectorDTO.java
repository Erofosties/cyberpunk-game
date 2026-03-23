package com.cyberpunk.dto;

public class MapSectorDTO {

    private int x;
    private int y;

    private boolean visible;
    private boolean explorado;

    private String terrain;
    private String resource;

    private Double richness;

    private String building;
    private Integer buildingLevel;

    private Long ownerId;

    public MapSectorDTO() {}

    public MapSectorDTO(
            int x,
            int y,
            boolean visible,
            boolean explorado,
            String terrain,
            String resource,
            Double richness,
            String building,
            Integer buildingLevel,
            Long ownerId) {

        this.x = x;
        this.y = y;
        this.visible = visible;
        this.explorado = explorado;
        this.terrain = terrain;
        this.resource = resource;
        this.richness = richness;
        this.building = building;
        this.buildingLevel = buildingLevel;
        this.ownerId = ownerId;
    }

    // ===== GETTERS =====

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean isVisible() { return visible; }
    public boolean isExplorado() { return explorado; }

    public String getTerrain() { return terrain; }
    public String getResource() { return resource; }

    public Double getRichness() { return richness; }

    public String getBuilding() { return building; }
    public Integer getBuildingLevel() { return buildingLevel; }

    public Long getOwnerId() { return ownerId; }
}