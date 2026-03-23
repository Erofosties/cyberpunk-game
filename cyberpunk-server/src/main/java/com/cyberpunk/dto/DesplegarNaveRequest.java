package com.cyberpunk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DesplegarNaveRequest {

    @NotNull(message = "La coordenada X no puede ser null")
    @Min(-1000)
    @Max(1000)
    private int x;

    @NotNull(message = "La coordenada Y no puede ser null")
    @Min(-1000)
    @Max(1000)
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}