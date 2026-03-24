package com.cyberpunk.dto;

public class PersonajeSectorDTO {

    private final Long id;
    private final String nombre;

    public PersonajeSectorDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
