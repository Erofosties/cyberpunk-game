package com.cyberpunk.gameBalance;

import java.util.Map;
import java.util.EnumMap;

import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;

public class GameBalance {

    private static final Map<TipoEdificio, Map<ResourceType, Integer>> COSTES_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> PRODUCCION_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> ENERGIA_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> TIEMPO_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<ResourceType, Integer> COMIDA_RECUPERACION = new EnumMap<>(ResourceType.class);
    
    static {
    	// ===== RECUPERACIÓN DE HAMBRE =====

    	COMIDA_RECUPERACION.put(ResourceType.KROMAFRUTA, 15);
    	COMIDA_RECUPERACION.put(ResourceType.ALGACARNE, 20);
    	COMIDA_RECUPERACION.put(ResourceType.NEUROTRIGO, 25);
    	COMIDA_RECUPERACION.put(ResourceType.RATAX, 30);
    	COMIDA_RECUPERACION.put(ResourceType.FLORSOMNIO, 35);

        // ===== MINAS =====

        COSTES_BASE.put(TipoEdificio.MINA_NEOCROMO, Map.of(
                ResourceType.NEOCROMO, 100
        ));

        COSTES_BASE.put(TipoEdificio.MINA_UMBRIUM, Map.of(
                ResourceType.NEOCROMO, 120,
                ResourceType.UMBRIUM, 40
        ));

        COSTES_BASE.put(TipoEdificio.MINA_SYNTHERIUM, Map.of(
                ResourceType.NEOCROMO, 140,
                ResourceType.UMBRIUM, 60
        ));

        COSTES_BASE.put(TipoEdificio.MINA_HEXALIUM, Map.of(
                ResourceType.NEOCROMO, 160,
                ResourceType.SYNTHERIUM, 80
        ));

        COSTES_BASE.put(TipoEdificio.MINA_VOIDIUM, Map.of(
                ResourceType.NEOCROMO, 200,
                ResourceType.HEXALIUM, 120
        ));

        PRODUCCION_BASE.put(TipoEdificio.MINA_NEOCROMO, 3);
        PRODUCCION_BASE.put(TipoEdificio.MINA_UMBRIUM, 3);
        PRODUCCION_BASE.put(TipoEdificio.MINA_SYNTHERIUM, 4);
        PRODUCCION_BASE.put(TipoEdificio.MINA_HEXALIUM, 4);
        PRODUCCION_BASE.put(TipoEdificio.MINA_VOIDIUM, 5);

        ENERGIA_BASE.put(TipoEdificio.MINA_NEOCROMO, -3);
        ENERGIA_BASE.put(TipoEdificio.MINA_UMBRIUM, -4);
        ENERGIA_BASE.put(TipoEdificio.MINA_SYNTHERIUM, -5);
        ENERGIA_BASE.put(TipoEdificio.MINA_HEXALIUM, -6);
        ENERGIA_BASE.put(TipoEdificio.MINA_VOIDIUM, -8);

        // ===== ENERGÍA =====

        COSTES_BASE.put(TipoEdificio.PLACA_SOLAR, Map.of(
                ResourceType.NEOCROMO, 150,
                ResourceType.UMBRIUM, 60
        ));

        COSTES_BASE.put(TipoEdificio.GENERADOR_NEON, Map.of(
                ResourceType.NEOCROMO, 180,
                ResourceType.HEXALIUM, 80
        ));

        COSTES_BASE.put(TipoEdificio.REACTOR_FUSION, Map.of(
                ResourceType.NEOCROMO, 250,
                ResourceType.VOIDIUM, 150
        ));

        PRODUCCION_BASE.put(TipoEdificio.PLACA_SOLAR, 10);
        PRODUCCION_BASE.put(TipoEdificio.GENERADOR_NEON, 18);
        PRODUCCION_BASE.put(TipoEdificio.REACTOR_FUSION, 40);

        // ===== BATERÍA =====

        COSTES_BASE.put(TipoEdificio.BATERIA_ENERGIA, Map.of(
                ResourceType.NEOCROMO, 120,
                ResourceType.UMBRIUM, 60
        ));

        // ===== TIEMPOS =====

        TIEMPO_BASE.put(TipoEdificio.MINA_NEOCROMO, 60);
        TIEMPO_BASE.put(TipoEdificio.MINA_UMBRIUM, 60);
        TIEMPO_BASE.put(TipoEdificio.MINA_SYNTHERIUM, 70);
        TIEMPO_BASE.put(TipoEdificio.MINA_HEXALIUM, 80);
        TIEMPO_BASE.put(TipoEdificio.MINA_VOIDIUM, 90);

        TIEMPO_BASE.put(TipoEdificio.PLACA_SOLAR, 80);
        TIEMPO_BASE.put(TipoEdificio.GENERADOR_NEON, 100);
        TIEMPO_BASE.put(TipoEdificio.REACTOR_FUSION, 180);

        TIEMPO_BASE.put(TipoEdificio.BATERIA_ENERGIA, 40);
    }

    public static Map<ResourceType, Integer> getCosteBase(TipoEdificio tipo) {
        return COSTES_BASE.get(tipo);
    }
    public static int getRecuperacionHambre(ResourceType comida) {
        return COMIDA_RECUPERACION.getOrDefault(comida, 0);
    }
    public static int getProduccionBase(TipoEdificio tipo) {
        return PRODUCCION_BASE.getOrDefault(tipo, 0);
    }

    public static int getEnergiaBase(TipoEdificio tipo) {
        return ENERGIA_BASE.getOrDefault(tipo, 0);
    }

    public static int getTiempoBase(TipoEdificio tipo) {
        return TIEMPO_BASE.getOrDefault(tipo, 60);
    }
}