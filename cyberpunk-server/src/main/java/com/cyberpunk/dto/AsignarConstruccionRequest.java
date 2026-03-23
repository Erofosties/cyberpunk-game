package com.cyberpunk.dto;

import jakarta.validation.constraints.NotNull;

public class AsignarConstruccionRequest {

    @NotNull(message = "El ID del personaje no puede ser nulo")
    private Long personajeId;
    
    @NotNull(message = "El ID de la construcción no puede ser nulo")
    private Long construccionId;

    public Long getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Long personajeId) {
        this.personajeId = personajeId;
    }

    public Long getConstruccionId() {
        return construccionId;
    }

    public void setConstruccionId(Long construccionId) {
        this.construccionId = construccionId;
    }
}