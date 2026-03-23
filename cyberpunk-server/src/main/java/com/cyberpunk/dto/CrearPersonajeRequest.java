package com.cyberpunk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CrearPersonajeRequest {

    @NotNull(message = "El ID de la colonia no puede ser nulo")
    private Long coloniaId;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    public Long getColoniaId() {
        return coloniaId;
    }

    public void setColoniaId(Long coloniaId) {
        this.coloniaId = coloniaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}