package com.cyberpunk.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class DesplegarGuerrerosRequest {

    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotEmpty(message = "Debes enviar al menos un guerrero")
    private List<Long> guerreroIds;

    @NotNull(message = "La coordenada x es obligatoria")
    private Integer x;

    @NotNull(message = "La coordenada y es obligatoria")
    private Integer y;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<Long> getGuerreroIds() { return guerreroIds; }
    public void setGuerreroIds(List<Long> guerreroIds) { this.guerreroIds = guerreroIds; }

    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }

    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
}