package com.cyberpunk.dto;

public class RondaBatallaDTO {

    private int ronda;
    private String resumen;
    private int atacantesOperativos;
    private int defensoresOperativos;
    private int trabajadoresDefensoresOperativos;
    private int danioDefensas;
    private boolean defensasDestruidas;

    public int getRonda() { return ronda; }
    public void setRonda(int ronda) { this.ronda = ronda; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public int getAtacantesOperativos() { return atacantesOperativos; }
    public void setAtacantesOperativos(int atacantesOperativos) { this.atacantesOperativos = atacantesOperativos; }

    public int getDefensoresOperativos() { return defensoresOperativos; }
    public void setDefensoresOperativos(int defensoresOperativos) { this.defensoresOperativos = defensoresOperativos; }

    public int getTrabajadoresDefensoresOperativos() { return trabajadoresDefensoresOperativos; }
    public void setTrabajadoresDefensoresOperativos(int trabajadoresDefensoresOperativos) { this.trabajadoresDefensoresOperativos = trabajadoresDefensoresOperativos; }

    public int getDanioDefensas() { return danioDefensas; }
    public void setDanioDefensas(int danioDefensas) { this.danioDefensas = danioDefensas; }

    public boolean isDefensasDestruidas() { return defensasDestruidas; }
    public void setDefensasDestruidas(boolean defensasDestruidas) { this.defensasDestruidas = defensasDestruidas; }
}