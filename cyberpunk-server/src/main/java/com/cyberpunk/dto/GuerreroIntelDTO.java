package com.cyberpunk.dto;

public class GuerreroIntelDTO {

    private final Long id;
    private final String nombre;
    private final Integer fuerza;
    private final Integer destreza;
    private final Integer resistencia;
    private final Integer hackeo;

    public GuerreroIntelDTO(
            Long id,
            String nombre,
            Integer fuerza,
            Integer destreza,
            Integer resistencia,
            Integer hackeo) {

        this.id = id;
        this.nombre = nombre;
        this.fuerza = fuerza;
        this.destreza = destreza;
        this.resistencia = resistencia;
        this.hackeo = hackeo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getFuerza() {
        return fuerza;
    }

    public Integer getDestreza() {
        return destreza;
    }

    public Integer getResistencia() {
        return resistencia;
    }

    public Integer getHackeo() {
        return hackeo;
    }
}
