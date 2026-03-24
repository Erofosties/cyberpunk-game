package com.cyberpunk.dto;

import java.util.List;

public class SectorDetalleDTO {

    private final Long sectorId;
    private final int x;
    private final int y;
    private final String terrain;
    private final String resource;
    private final double richness;
    private final String building;
    private final Integer buildingLevel;
    private final Long ownerId;
    private final Integer cantidadTrabajadores;
    private final Integer cantidadGuerreros;
    private final Integer cantidadDefensas;
    private final DefensasIntelDTO defensas;
    private final List<PersonajeSectorDTO> trabajadores;
    private final List<GuerreroIntelDTO> guerreros;

    public SectorDetalleDTO(
            Long sectorId,
            int x,
            int y,
            String terrain,
            String resource,
            double richness,
            String building,
            Integer buildingLevel,
            Long ownerId,
            Integer cantidadTrabajadores,
            Integer cantidadGuerreros,
            Integer cantidadDefensas,
            DefensasIntelDTO defensas,
            List<PersonajeSectorDTO> trabajadores,
            List<GuerreroIntelDTO> guerreros) {

        this.sectorId = sectorId;
        this.x = x;
        this.y = y;
        this.terrain = terrain;
        this.resource = resource;
        this.richness = richness;
        this.building = building;
        this.buildingLevel = buildingLevel;
        this.ownerId = ownerId;
        this.cantidadTrabajadores = cantidadTrabajadores;
        this.cantidadGuerreros = cantidadGuerreros;
        this.cantidadDefensas = cantidadDefensas;
        this.defensas = defensas;
        this.trabajadores = trabajadores;
        this.guerreros = guerreros;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getTerrain() {
        return terrain;
    }

    public String getResource() {
        return resource;
    }

    public double getRichness() {
        return richness;
    }

    public String getBuilding() {
        return building;
    }

    public Integer getBuildingLevel() {
        return buildingLevel;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Integer getCantidadTrabajadores() {
        return cantidadTrabajadores;
    }

    public Integer getCantidadGuerreros() {
        return cantidadGuerreros;
    }

    public Integer getCantidadDefensas() {
        return cantidadDefensas;
    }

    public DefensasIntelDTO getDefensas() {
        return defensas;
    }

    public List<PersonajeSectorDTO> getTrabajadores() {
        return trabajadores;
    }

    public List<GuerreroIntelDTO> getGuerreros() {
        return guerreros;
    }
}
