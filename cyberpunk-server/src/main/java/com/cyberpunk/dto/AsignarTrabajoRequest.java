package com.cyberpunk.dto;

public class AsignarTrabajoRequest {

    private Long personajeId;
    private Long edificioId;

    public Long getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Long personajeId) {
        this.personajeId = personajeId;
    }

    public Long getEdificioId() {
        return edificioId;
    }

    public void setEdificioId(Long edificioId) {
        this.edificioId = edificioId;
    }
}