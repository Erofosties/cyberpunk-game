package com.cyberpunk.gameBalance;

import java.util.EnumMap;
import java.util.Map;

import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;

public class GameBalance {

    // ===== CONSTANTS =====
    public static final int MINIMUM_COLONY_DISTANCE = 5;
    public static final int TOTAL_CHARACTER_SKILL_POINTS = 6;
    public static final int INITIAL_VISION_RADIUS = 1;
    public static final int INITIAL_SOLAR_PANELS = 1;
    public static final int INITIAL_BATTERIES = 1;
        public static final int MAX_COMIDA_PERSONAJE = 100;
        public static final int METABOLISMO_PUNTOS_POR_COMIDA = 6;
        public static final int DISTANCIA_SECTORES_POR_TICK = 2;
        public static final int CONSUMO_COMIDA_INACTIVO = 1;
        public static final int CONSUMO_COMIDA_TRABAJANDO = 2;
        public static final int UMBRAL_AUTO_CONSUMO_COMIDA = 45;
        public static final int DANO_POR_INANICION = 2;
        public static final int CURACION_NATURAL_POR_TICK = 1;
        public static final int CURACION_FLORSOMNIO = 8;
        public static final int DESCANSO_EXTRA_FLORSOMNIO = 20;
        public static final int VISION_GUERRERO_DESPLEGADO = 1;
        public static final double PROB_CRIT_BASE = 0.05;
        public static final double PROB_ESQUIVAR_BASE = 0.05;
        public static final double PROB_POR_DESTREZA = 0.04;
        public static final int SAQUEO_NAVE_PORCENTAJE = 20;
        public static final int SAQUEO_EDIFICIO_PORCENTAJE = 6;

    private static final Map<TipoEdificio, Map<ResourceType, Integer>> COSTES_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> PRODUCCION_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> ENERGIA_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<TipoEdificio, Integer> TIEMPO_BASE = new EnumMap<>(TipoEdificio.class);
    private static final Map<ResourceType, Integer> COMIDA_RECUPERACION = new EnumMap<>(ResourceType.class);
    
    static {
            	// ===== RECUPERACIÓN DE COMIDA =====

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
                ResourceType.NEOCROMO, 120
        ));

        COSTES_BASE.put(TipoEdificio.MINA_SYNTHERIUM, Map.of(
                ResourceType.NEOCROMO, 140,
                ResourceType.UMBRIUM, 60
        ));

        COSTES_BASE.put(TipoEdificio.MINA_HEXALIUM, Map.of(
                ResourceType.NEOCROMO, 160,
                ResourceType.UMBRIUM, 80,
                ResourceType.SYNTHERIUM, 40
        ));

        COSTES_BASE.put(TipoEdificio.MINA_VOIDIUM, Map.of(
                ResourceType.SYNTHERIUM, 200,
                ResourceType.HEXALIUM, 120
        ));

        PRODUCCION_BASE.put(TipoEdificio.MINA_NEOCROMO, 2);
        PRODUCCION_BASE.put(TipoEdificio.MINA_UMBRIUM, 2);
        PRODUCCION_BASE.put(TipoEdificio.MINA_SYNTHERIUM, 3);
        PRODUCCION_BASE.put(TipoEdificio.MINA_HEXALIUM, 3);
        PRODUCCION_BASE.put(TipoEdificio.MINA_VOIDIUM, 4);

        ENERGIA_BASE.put(TipoEdificio.MINA_NEOCROMO, -3);
        ENERGIA_BASE.put(TipoEdificio.MINA_UMBRIUM, -4);
        ENERGIA_BASE.put(TipoEdificio.MINA_SYNTHERIUM, -5);
        ENERGIA_BASE.put(TipoEdificio.MINA_HEXALIUM, -6);
        ENERGIA_BASE.put(TipoEdificio.MINA_VOIDIUM, -8);

        // ===== GRANJAS =====

        COSTES_BASE.put(TipoEdificio.GRANJA_KROMAFRUTA, Map.of(
                ResourceType.NEOCROMO, 90
        ));

        COSTES_BASE.put(TipoEdificio.GRANJA_NEUROTRIGO, Map.of(
                ResourceType.SYNTHERIUM, 110,
                ResourceType.KROMAFRUTA, 40
        ));

        COSTES_BASE.put(TipoEdificio.GRANJA_ALGACARNE, Map.of(
                ResourceType.NEUROTRIGO, 130,
                ResourceType.UMBRIUM, 50
        ));

        COSTES_BASE.put(TipoEdificio.CRIADERO_RATAX, Map.of(
                ResourceType.ALGACARNE, 150,
                ResourceType.SYNTHERIUM, 60
        ));

        COSTES_BASE.put(TipoEdificio.CULTIVO_FLORSOMNIO, Map.of(
                ResourceType.SYNTHERIUM, 170,
                ResourceType.KROMAFRUTA, 80
        ));

        PRODUCCION_BASE.put(TipoEdificio.GRANJA_KROMAFRUTA, 2);
        PRODUCCION_BASE.put(TipoEdificio.GRANJA_NEUROTRIGO, 2);
        PRODUCCION_BASE.put(TipoEdificio.GRANJA_ALGACARNE, 3);
        PRODUCCION_BASE.put(TipoEdificio.CRIADERO_RATAX, 3);
        PRODUCCION_BASE.put(TipoEdificio.CULTIVO_FLORSOMNIO, 4);

        ENERGIA_BASE.put(TipoEdificio.GRANJA_KROMAFRUTA, -2);
        ENERGIA_BASE.put(TipoEdificio.GRANJA_NEUROTRIGO, -3);
        ENERGIA_BASE.put(TipoEdificio.GRANJA_ALGACARNE, -4);
        ENERGIA_BASE.put(TipoEdificio.CRIADERO_RATAX, -4);
        ENERGIA_BASE.put(TipoEdificio.CULTIVO_FLORSOMNIO, -5);

        // ===== LABORATORIOS =====

        COSTES_BASE.put(TipoEdificio.LAB_REFLEXA, Map.of(
                ResourceType.VOIDIUM, 140,
                ResourceType.HEXALIUM, 50
        ));

        COSTES_BASE.put(TipoEdificio.LAB_NANOCURA, Map.of(
                ResourceType.KROMAFRUTA, 170,
                ResourceType.VOIDIUM, 70
        ));

        COSTES_BASE.put(TipoEdificio.LAB_SOMNEX, Map.of(
                ResourceType.FLORSOMNIO, 200,
                ResourceType.SYNTHERIUM, 90
        ));

        PRODUCCION_BASE.put(TipoEdificio.LAB_REFLEXA, 1);
        PRODUCCION_BASE.put(TipoEdificio.LAB_NANOCURA, 2);
        PRODUCCION_BASE.put(TipoEdificio.LAB_SOMNEX, 3);

        ENERGIA_BASE.put(TipoEdificio.LAB_REFLEXA, -4);
        ENERGIA_BASE.put(TipoEdificio.LAB_NANOCURA, -5);
        ENERGIA_BASE.put(TipoEdificio.LAB_SOMNEX, -6);

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

        PRODUCCION_BASE.put(TipoEdificio.PLACA_SOLAR, 8);
        PRODUCCION_BASE.put(TipoEdificio.GENERADOR_NEON, 15);
        PRODUCCION_BASE.put(TipoEdificio.REACTOR_FUSION, 32);

        // ===== BATERÍA =====

        COSTES_BASE.put(TipoEdificio.BATERIA_ENERGIA, Map.of(
                ResourceType.NEOCROMO, 120,
                ResourceType.UMBRIUM, 60
        ));

        // ===== TIEMPOS =====

        TIEMPO_BASE.put(TipoEdificio.MINA_NEOCROMO, 180);
        TIEMPO_BASE.put(TipoEdificio.MINA_UMBRIUM, 210);
        TIEMPO_BASE.put(TipoEdificio.MINA_SYNTHERIUM, 240);
        TIEMPO_BASE.put(TipoEdificio.MINA_HEXALIUM, 270);
        TIEMPO_BASE.put(TipoEdificio.MINA_VOIDIUM, 300);

        TIEMPO_BASE.put(TipoEdificio.GRANJA_KROMAFRUTA, 150);
        TIEMPO_BASE.put(TipoEdificio.GRANJA_NEUROTRIGO, 180);
        TIEMPO_BASE.put(TipoEdificio.GRANJA_ALGACARNE, 210);
        TIEMPO_BASE.put(TipoEdificio.CRIADERO_RATAX, 240);
        TIEMPO_BASE.put(TipoEdificio.CULTIVO_FLORSOMNIO, 270);

        TIEMPO_BASE.put(TipoEdificio.LAB_REFLEXA, 240);
        TIEMPO_BASE.put(TipoEdificio.LAB_NANOCURA, 300);
        TIEMPO_BASE.put(TipoEdificio.LAB_SOMNEX, 360);

        TIEMPO_BASE.put(TipoEdificio.PLACA_SOLAR, 180);
        TIEMPO_BASE.put(TipoEdificio.GENERADOR_NEON, 240);
        TIEMPO_BASE.put(TipoEdificio.REACTOR_FUSION, 540);

        TIEMPO_BASE.put(TipoEdificio.BATERIA_ENERGIA, 120);
    }

    public static Map<ResourceType, Integer> getCosteBase(TipoEdificio tipo) {
        return COSTES_BASE.get(tipo);
    }
        public static int getRecuperacionComida(ResourceType comida) {
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