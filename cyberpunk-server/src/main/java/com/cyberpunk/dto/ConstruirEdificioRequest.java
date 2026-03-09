package com.cyberpunk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConstruirEdificioRequest {

    @NotNull
    private Long coloniaId;

    @NotBlank
    private String tipoEdificio;

    public ConstruirEdificioRequest() {}

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

    @Override
    public String toString() {
        return "ConstruirEdificioRequest{" +
                "coloniaId=" + coloniaId +
                ", tipoEdificio='" + tipoEdificio + '\'' +
                '}';
    }
}