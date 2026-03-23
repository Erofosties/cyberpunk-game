package com.cyberpunk.dto;

import jakarta.validation.constraints.NotNull;

public class AsignarSectorTrabajoRequest {

    @NotNull(message = "El ID del personaje no puede ser nulo")
    private Long personajeId;
    
    @NotNull(message = "El ID del sector no puede ser nulo")
    private Long sectorId;

    public Long getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Long personajeId) {
        this.personajeId = personajeId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }
}