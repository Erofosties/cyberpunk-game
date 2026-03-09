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

        int base = switch (tipo) {

            case MINA_NEOCROMO -> 100;
            case MINA_UMBRIUM -> 120;
            case MINA_SYNTHERIUM -> 140;
            case MINA_HEXALIUM -> 160;
            case MINA_VOIDIUM -> 200;

            case GRANJA_KROMAFRUTA -> 80;
            case GRANJA_NEUROTRIGO -> 80;
            case GRANJA_ALGACARNE -> 90;
            case CRIADERO_RATAX -> 100;
            case CULTIVO_FLORSOMNIO -> 110;

            case LAB_REFLEXA -> 150;
            case LAB_NANOCURA -> 150;
            case LAB_SOMNEX -> 170;

            case PLACA_SOLAR -> 150;
            case REACTOR_FUSION -> 200;
            case GENERADOR_NEON -> 180;
        };

        int costeFinal = (int)(base * Math.pow(FACTOR_COSTE, nivelActual));

        coste.put(ResourceType.NEOCROMO, costeFinal);

        return coste;
    }

    public static int calcularTiempoConstruccion(int nivelActual) {

        int baseTiempo = 60;

        return (int)(baseTiempo * Math.pow(FACTOR_TIEMPO, nivelActual));
    }
}