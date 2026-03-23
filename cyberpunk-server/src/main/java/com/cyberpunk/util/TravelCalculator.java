package com.cyberpunk.util;

import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.gameBalance.GameBalance;

public final class TravelCalculator {

    private TravelCalculator() {}

    public static int calcularTicks(MapSector origen, MapSector destino) {
        if (origen == null || destino == null) {
            return 1;
        }

        int distancia = Math.max(
                Math.abs(origen.getX() - destino.getX()),
                Math.abs(origen.getY() - destino.getY())
        );

        if (distancia == 0) {
            return 0;
        }

        return Math.max(1, (int) Math.ceil((double) distancia / GameBalance.DISTANCIA_SECTORES_POR_TICK));
    }
}