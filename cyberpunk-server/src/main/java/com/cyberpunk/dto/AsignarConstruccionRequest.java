package com.cyberpunk.dto;

public class AsignarConstruccionRequest {

    private Long personajeId;
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