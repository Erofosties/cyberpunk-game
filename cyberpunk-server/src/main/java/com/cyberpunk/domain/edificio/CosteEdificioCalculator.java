package com.cyberpunk.domain.edificio;

import java.util.EnumMap;
import java.util.Map;

import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.gameBalance.GameBalance;

public class CosteEdificioCalculator {

    private static final double FACTOR_COSTE = 1.6;
    private static final double FACTOR_TIEMPO = 1.4;

    public static Map<ResourceType, Integer> calcularCoste(
            Edificio.TipoEdificio tipo,
            int nivelActual) {

        Map<ResourceType, Integer> base = GameBalance.getCosteBase(tipo);
        if (base == null) {
            throw new IllegalStateException("No hay coste base configurado para el edificio: " + tipo);
        }

        Map<ResourceType, Integer> costeFinal = new EnumMap<>(ResourceType.class);

        double multiplicador = Math.pow(FACTOR_COSTE, nivelActual);

        for (var entry : base.entrySet()) {

            int valor = (int)(entry.getValue() * multiplicador);

            costeFinal.put(entry.getKey(), valor);
        }

        return costeFinal;
    }

    public static int calcularTiempoConstruccion(
            Edificio.TipoEdificio tipo,
            int nivelActual) {

        int base = GameBalance.getTiempoBase(tipo);

        return (int)(base * Math.pow(FACTOR_TIEMPO, nivelActual));
    }
}