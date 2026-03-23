package com.cyberpunk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class CrearTrabajadorRequest {

    @NotNull(message = "El ID de la colonia no puede ser nulo")
    private Long coloniaId;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int mineria;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int agricultura;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int ciencia;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int ingenieria;

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

    public int getMineria() {
        return mineria;
    }

    public void setMineria(int mineria) {
        this.mineria = mineria;
    }

    public int getAgricultura() {
        return agricultura;
    }

    public void setAgricultura(int agricultura) {
        this.agricultura = agricultura;
    }

    public int getCiencia() {
        return ciencia;
    }

    public void setCiencia(int ciencia) {
        this.ciencia = ciencia;
    }

    public int getIngenieria() {
        return ingenieria;
    }

    public void setIngenieria(int ingenieria) {
        this.ingenieria = ingenieria;
    }
}
