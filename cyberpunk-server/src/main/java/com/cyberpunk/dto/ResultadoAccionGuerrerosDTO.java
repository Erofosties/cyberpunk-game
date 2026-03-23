package com.cyberpunk.dto;

import java.util.List;
import java.util.Map;

public class ResultadoAccionGuerrerosDTO {

    private String accion;
    private String resultado;
    private String mensaje;
    private int guerrerosEnviados;
    private int guerrerosOperativos;
    private int defensoresOperativos;
    private int tiempoViajeIda;
    private int tiempoViajeVuelta;
    private int totalBotin;
    private Map<String, Integer> botin;
    private ReconocimientoSectorDTO reconocimiento;
    private List<RondaBatallaDTO> rondas;

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getGuerrerosEnviados() { return guerrerosEnviados; }
    public void setGuerrerosEnviados(int guerrerosEnviados) { this.guerrerosEnviados = guerrerosEnviados; }

    public int getGuerrerosOperativos() { return guerrerosOperativos; }
    public void setGuerrerosOperativos(int guerrerosOperativos) { this.guerrerosOperativos = guerrerosOperativos; }

    public int getDefensoresOperativos() { return defensoresOperativos; }
    public void setDefensoresOperativos(int defensoresOperativos) { this.defensoresOperativos = defensoresOperativos; }

    public int getTiempoViajeIda() { return tiempoViajeIda; }
    public void setTiempoViajeIda(int tiempoViajeIda) { this.tiempoViajeIda = tiempoViajeIda; }

    public int getTiempoViajeVuelta() { return tiempoViajeVuelta; }
    public void setTiempoViajeVuelta(int tiempoViajeVuelta) { this.tiempoViajeVuelta = tiempoViajeVuelta; }

    public int getTotalBotin() { return totalBotin; }
    public void setTotalBotin(int totalBotin) { this.totalBotin = totalBotin; }

    public Map<String, Integer> getBotin() { return botin; }
    public void setBotin(Map<String, Integer> botin) { this.botin = botin; }

    public ReconocimientoSectorDTO getReconocimiento() { return reconocimiento; }
    public void setReconocimiento(ReconocimientoSectorDTO reconocimiento) { this.reconocimiento = reconocimiento; }

    public List<RondaBatallaDTO> getRondas() { return rondas; }
    public void setRondas(List<RondaBatallaDTO> rondas) { this.rondas = rondas; }
}