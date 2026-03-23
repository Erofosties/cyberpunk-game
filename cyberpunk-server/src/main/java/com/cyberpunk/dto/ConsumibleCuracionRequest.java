package com.cyberpunk.dto;

import jakarta.validation.constraints.NotNull;

public class ConsumibleCuracionRequest {

    @NotNull(message = "La colonia es obligatoria")
    private Long coloniaId;

    public Long getColoniaId() { return coloniaId; }
    public void setColoniaId(Long coloniaId) { this.coloniaId = coloniaId; }
}