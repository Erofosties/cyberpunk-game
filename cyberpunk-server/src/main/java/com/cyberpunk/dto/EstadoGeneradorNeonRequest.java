package com.cyberpunk.dto;

import jakarta.validation.constraints.NotNull;

public class EstadoGeneradorNeonRequest {

    @NotNull(message = "El ID de la colonia no puede ser nulo")
    private Long coloniaId;

    @NotNull(message = "El ID del sector no puede ser nulo")
    private Long sectorId;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    public Long getColoniaId() {
        return coloniaId;
    }

    public void setColoniaId(Long coloniaId) {
        this.coloniaId = coloniaId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
