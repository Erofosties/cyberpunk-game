package com.cyberpunk.domain.edificio;

import java.util.EnumMap;
import java.util.Map;

import com.cyberpunk.domain.recursos.Recursos.ResourceType;

public class CosteEdificioCalculator {

    private static final double FACTOR_COSTE = 1.6;
    private static final double FACTOR_TIEMPO = 1.4;

    public static Map<ResourceType, Integer> calcularCoste(
            Edificio.TipoEdificio tipo,
            int nivelActual) {

        Map<ResourceType, Integer> coste = new EnumMap<>(ResourceType.class);

        int baseMetal = 100;
        int baseSecundario = 60;

        int costeMetal = (int) (baseMetal * Math.pow(FACTOR_COSTE, nivelActual));
        int costeSec = (int) (baseSecundario * Math.pow(FACTOR_COSTE, nivelActual));

        coste.put(ResourceType.NEOCROMO, costeMetal);
        coste.put(ResourceType.UMBRIUM, costeSec);

        return coste;
    }

    public static int calcularTiempoConstruccion(int nivelActual) {

        int baseTiempo = 60;

        return (int) (baseTiempo * Math.pow(FACTOR_TIEMPO, nivelActual));
    }
}