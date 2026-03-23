package com.cyberpunk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class ConstruirEdificioRequest {

    @NotNull(message = "El ID de la colonia no puede ser nulo")
    private Long coloniaId;
    
    @NotBlank(message = "El tipo de edificio no puede estar vacío")
    private String tipoEdificio;
    
    @NotNull(message = "El ID del sector no puede ser nulo")
    private Long sectorId;

    public Long getColoniaId() {
        return coloniaId;
    }

    public void setColoniaId(Long coloniaId) {
        this.coloniaId = coloniaId;
    }

    public String getTipoEdificio() {
        return tipoEdificio;
    }

    public void setTipoEdificio(String tipoEdificio) {
        this.tipoEdificio = tipoEdificio;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }
}