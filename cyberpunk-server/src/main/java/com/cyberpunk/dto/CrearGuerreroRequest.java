package com.cyberpunk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class CrearGuerreroRequest {

    @NotNull(message = "El ID de la colonia no puede ser nulo")
    private Long coloniaId;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int fuerza;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int destreza;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int resistencia;

    @Min(value = 0, message = "La habilidad no puede ser negativa")
    @Max(value = 6, message = "La habilidad no puede exceder de 6")
    private int hackeo;

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

    public int getFuerza() {
        return fuerza;
    }

    public void setFuerza(int fuerza) {
        this.fuerza = fuerza;
    }

    public int getDestreza() {
        return destreza;
    }

    public void setDestreza(int destreza) {
        this.destreza = destreza;
    }

    public int getResistencia() {
        return resistencia;
    }

    public void setResistencia(int resistencia) {
        this.resistencia = resistencia;
    }

    public int getHackeo() {
        return hackeo;
    }

    public void setHackeo(int hackeo) {
        this.hackeo = hackeo;
    }
}
