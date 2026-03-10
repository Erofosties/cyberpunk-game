package com.cyberpunk.dto;

public class AsignarTareaRequest {

    private Long personajeId;

    private String tipo;

    private Long edificioId;

    private Long construccionId;

    public Long getPersonajeId() { return personajeId; }

    public void setPersonajeId(Long personajeId) { this.personajeId = personajeId; }

    public String getTipo() { return tipo; }

    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getEdificioId() { return edificioId; }

    public void setEdificioId(Long edificioId) { this.edificioId = edificioId; }

    public Long getConstruccionId() { return construccionId; }

    public void setConstruccionId(Long construccionId) { this.construccionId = construccionId; }
}