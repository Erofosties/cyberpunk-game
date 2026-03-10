package com.cyberpunk.dto;

public class CrearPersonajeRequest {

    private Long coloniaId;

    private String nombre;

    private String tipo;

    // habilidades trabajador
    private int mineria;
    private int agricultura;
    private int ciencia;
    private int ingenieria;

    // habilidades guerrero
    private int fuerza;
    private int destreza;
    private int resistencia;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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