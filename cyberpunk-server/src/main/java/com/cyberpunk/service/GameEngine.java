package com.cyberpunk.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.gameBalance.GameBalance;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Component
public class GameEngine {

    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository mapSectorRepository;
    private final PersonajeRepository personajeRepository;
    private final ExplorationService explorationService;
    private final GuerreroService guerreroService;

    public GameEngine(
            ColoniaRepository coloniaRepository,
            MapSectorRepository mapSectorRepository,
            PersonajeRepository personajeRepository,
            ExplorationService explorationService,
            GuerreroService guerreroService) {

        this.coloniaRepository = coloniaRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.personajeRepository = personajeRepository;
        this.explorationService = explorationService;
        this.guerreroService = guerreroService;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void tick() {

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarViajes(colonia);
            guerreroService.resolverMisionesAlLlegar(colonia);

            procesarConstrucciones(colonia);
            procesarProduccion(colonia);

            procesarConsumoComida(colonia);
            procesarAlimentacion(colonia);
            procesarDanioPorInanicion(colonia);
            procesarCuracionNatural(colonia);
            procesarDescanso(colonia);
            procesarExploracion(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick ejecutado: colonias procesadas = " + colonias.size());
    }

    // ================= EXPLORACIÓN =================

    private void procesarExploracion(Colonia colonia) {

        // No ocultamos toda la visibilidad cada tick.
        // Mantener exploración persistente evita que el mapa se convierta en 'f' tras un tick.
        // explorationService.ocultarVisibilidad(usuarioId);

        List<Personaje> personajes = personajeRepository.findByColonia(colonia);

        for (Personaje personaje : personajes) {

            if (personaje instanceof Guerrero guerrero && guerrero.puedeActuar()) {

                if (guerrero.estaEnViaje()) {
                    continue;
                }

                explorationService.revelarAlrededor(guerrero);
            }
        }
    }

    // ================= CONSTRUCCIONES =================

    private void procesarConstrucciones(Colonia colonia) {

        List<ConstruccionEnCurso> completadas = new ArrayList<>();

        for (ConstruccionEnCurso construccion : colonia.getColaConstruccion()) {

            int progreso = calcularProgresoConstruccion(colonia, construccion);

            if (progreso > 0) {
                construccion.avanzarConstruccion(progreso);
            }

            if (construccion.completada()) {

                crearEdificio(construccion, colonia);
                liberarTrabajadoresConstruccion(colonia, construccion);

                completadas.add(construccion);
            }
        }

        colonia.getColaConstruccion().removeAll(completadas);
    }

    private int calcularProgresoConstruccion(Colonia colonia, ConstruccionEnCurso construccion) {

        int progreso = 0;

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje instanceof Trabajador trabajador
                    && trabajador.getConstruccionAsignada() == construccion) {

                if (!trabajador.puedeActuar()) {
                    continue;
                }

                if (trabajador.estaEnViaje()) {
                    continue;
                }

                if (trabajador.getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(trabajador);
                    continue;
                }

                progreso += trabajador.getIngenieria();
                trabajador.aumentarCansancio(2);

                if (trabajador.getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(trabajador);
                }
            }
        }

        return progreso;
    }

    private void crearEdificio(ConstruccionEnCurso construccion, Colonia colonia) {

        MapSector sector = construccion.getSectorDestino();

        if (sector == null)
            return;

        // Protección extra
        if (sector.getBuilding() != null)
            return;

        sector.setBuilding(
                Edificio.TipoEdificio.valueOf(construccion.getTipo())
        );

        sector.setOwner(colonia.getUsuario());

        mapSectorRepository.save(sector);
    }

    // ================= PRODUCCIÓN =================

    private void procesarProduccion(Colonia colonia) {

        List<MapSector> sectoresPropiosConEdificio = obtenerSectoresPropiosConEdificio(colonia);
        int capacidadBaterias = calcularCapacidadBaterias(colonia, sectoresPropiosConEdificio);
        int energiaAcumulada = Math.min(colonia.getRecursos().getEnergiaAcumulada(), capacidadBaterias);

        int energiaGenerada = calcularEnergiaSolar(colonia, sectoresPropiosConEdificio);
        energiaGenerada += calcularEnergiaGeneradorNeon(colonia, sectoresPropiosConEdificio);

        List<Trabajador> trabajadoresReactor = obtenerTrabajadoresReactor(colonia);
        energiaGenerada += producirEnergiaReactor(trabajadoresReactor);

        List<ProduccionPendiente> producciones = new ArrayList<>();
        int demandaEnergiaProduccion = prepararProduccionRecursos(colonia, producciones);
        int demandaEnergiaDefensas = calcularDemandaEnergiaDefensas(colonia);

        int demandaTotal = demandaEnergiaProduccion + demandaEnergiaDefensas;
        int energiaCubierta = energiaGenerada;

        if (demandaTotal > energiaCubierta) {
            int energiaNecesaria = demandaTotal - energiaCubierta;
            int descargaBateria = Math.min(energiaAcumulada, energiaNecesaria);
            energiaAcumulada -= descargaBateria;
            energiaCubierta += descargaBateria;
        }

        double ratioEnergia = demandaTotal <= 0
                ? 1.0
                : Math.min(1.0, (double) energiaCubierta / demandaTotal);

        for (ProduccionPendiente produccion : producciones) {
            int produccionEscalada = (int) Math.floor(produccion.produccionBase() * ratioEnergia);
            if (produccionEscalada > 0 && produccion.recurso() != null) {
                colonia.getRecursos().add(produccion.recurso(), produccionEscalada);
                produccion.trabajador().aumentarCansancio(1);
                if (produccion.trabajador().getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(produccion.trabajador());
                }
            }
        }

        int energiaDisponible = energiaCubierta - demandaTotal;

        if (energiaDisponible > 0 && capacidadBaterias > energiaAcumulada) {
            int capacidadRestante = capacidadBaterias - energiaAcumulada;
            int carga = Math.min(capacidadRestante, energiaDisponible);
            energiaAcumulada += carga;
        }

        colonia.getRecursos().setEnergiaDisponible(energiaDisponible);
        colonia.getRecursos().setEnergiaAcumulada(energiaAcumulada);
    }

    private List<MapSector> obtenerSectoresPropiosConEdificio(Colonia colonia) {
        Long propietarioId = colonia.getUsuario().getId();
        return mapSectorRepository.findAll().stream()
                .filter(sector -> sector.getOwner() != null)
                .filter(sector -> propietarioId.equals(sector.getOwner().getId()))
                .filter(sector -> sector.getBuilding() != null)
                .toList();
    }

    private int calcularCapacidadBaterias(Colonia colonia, List<MapSector> sectoresPropiosConEdificio) {
        long bateriasSector = sectoresPropiosConEdificio.stream()
                .filter(sector -> sector.getBuilding() == Edificio.TipoEdificio.BATERIA_ENERGIA)
                .count();

        int bateriasTotales = colonia.getBaterias() + (int) bateriasSector;
        return Math.max(0, bateriasTotales) * GameBalance.CAPACIDAD_BATERIA;
    }

    private int calcularEnergiaSolar(Colonia colonia, List<MapSector> sectoresPropiosConEdificio) {
        int baseSolar = GameBalance.getProduccionBase(Edificio.TipoEdificio.PLACA_SOLAR);
        int energiaNave = colonia.getPlacasSolares() * baseSolar;

        int energiaSectores = sectoresPropiosConEdificio.stream()
                .filter(sector -> sector.getBuilding() == Edificio.TipoEdificio.PLACA_SOLAR)
                .mapToInt(sector -> baseSolar * Math.max(1, sector.getBuildingLevel()))
                .sum();

        return energiaNave + energiaSectores;
    }

    private int calcularEnergiaGeneradorNeon(Colonia colonia, List<MapSector> sectoresPropiosConEdificio) {
        int energiaGenerada = 0;
        for (MapSector sector : sectoresPropiosConEdificio) {
            if (sector.getBuilding() != Edificio.TipoEdificio.GENERADOR_NEON || !sector.isGeneradorNeonActivo()) {
                continue;
            }

            int objetivo = GameBalance.getProduccionBase(Edificio.TipoEdificio.GENERADOR_NEON)
                    * Math.max(1, sector.getBuildingLevel());
            energiaGenerada += colonia.getRecursos().consumirCombustibleGeneradorNeon(objetivo);
        }

        return energiaGenerada;
    }

    private List<Trabajador> obtenerTrabajadoresReactor(Colonia colonia) {
        List<Trabajador> trabajadores = new ArrayList<>();
        for (Personaje personaje : colonia.getPoblacion()) {
            if (!(personaje instanceof Trabajador trabajador)) {
                continue;
            }

            if (!trabajador.puedeActuar() || trabajador.estaEnViaje()) {
                continue;
            }

            if (trabajador.getConstruccionAsignada() != null || trabajador.getSectorAsignado() == null) {
                continue;
            }

            if (trabajador.getCansancio() >= 100) {
                enviarTrabajadorANavePorAgotamiento(trabajador);
                continue;
            }

            MapSector sector = trabajador.getSectorAsignado();
            if (sector.getBuilding() == Edificio.TipoEdificio.REACTOR_FUSION) {
                trabajadores.add(trabajador);
            }
        }

        return trabajadores;
    }

    private int producirEnergiaReactor(List<Trabajador> trabajadoresReactor) {
        int energia = 0;
        for (Trabajador trabajador : trabajadoresReactor) {
            MapSector sector = trabajador.getSectorAsignado();
            int nivel = Math.max(1, sector.getBuildingLevel());
            int base = GameBalance.getProduccionBase(Edificio.TipoEdificio.REACTOR_FUSION) * nivel;
            int produccion = (int) Math.floor(base * trabajador.getIngenieria() * sector.getRichness());

            if (produccion > 0) {
                energia += produccion;
                trabajador.aumentarCansancio(1);
                if (trabajador.getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(trabajador);
                }
            }
        }

        return energia;
    }

    private int prepararProduccionRecursos(Colonia colonia, List<ProduccionPendiente> producciones) {
        int demandaEnergia = 0;

        for (Personaje personaje : colonia.getPoblacion()) {

            if (!(personaje instanceof Trabajador trabajador)) {
                continue;
            }

            if (!trabajador.puedeActuar() || trabajador.estaEnViaje()) {
                continue;
            }

            if (trabajador.getSectorAsignado() == null || trabajador.getConstruccionAsignada() != null) {
                continue;
            }

            if (trabajador.getCansancio() >= 100) {
                enviarTrabajadorANavePorAgotamiento(trabajador);
                continue;
            }

            MapSector sector = trabajador.getSectorAsignado();
            if (sector.getBuilding() == null) {
                continue;
            }

            if (!esEdificioProductivoDeRecursos(sector.getBuilding())) {
                continue;
            }

            int nivel = Math.max(1, sector.getBuildingLevel());
            int habilidad = trabajador.getProduccionParaEdificio(sector.getBuilding());
            int base = GameBalance.getProduccionBase(sector.getBuilding()) * nivel;
            int produccionBase = (int) Math.floor(base * habilidad * sector.getRichness());

            ResourceType recurso = convertirRecurso(sector.getBuilding());
            if (produccionBase > 0 && recurso != null) {
                int demanda = Math.abs(GameBalance.getEnergiaBase(sector.getBuilding())) * nivel;
                demandaEnergia += demanda;
                producciones.add(new ProduccionPendiente(trabajador, recurso, produccionBase));
            }
        }

        return demandaEnergia;
    }

    private int calcularDemandaEnergiaDefensas(Colonia colonia) {
        int demandaNave = (colonia.getDefensas().getEscudos() * GameBalance.DEFENSA_COSTE_ESCUDO)
                + (colonia.getDefensas().getTorretasNeocromo() * GameBalance.DEFENSA_COSTE_TORRETA)
                + (colonia.getDefensas().getCanonesHexalium() * GameBalance.DEFENSA_COSTE_CANON);

        int demandaSectores = mapSectorRepository.findAll().stream()
                .filter(sector -> sector.getOwner() != null)
                .filter(sector -> sector.getOwner().getId().equals(colonia.getUsuario().getId()))
                .mapToInt(this::calcularDemandaEnergiaDefensaSector)
                .sum();

        return demandaNave + demandaSectores;
    }

    private int calcularDemandaEnergiaDefensaSector(MapSector sector) {
        if (sector.getBuilding() == null) {
            return 0;
        }

        return switch (sector.getBuilding()) {
            case ESCUDO_SECTOR -> sector.getCantidadEscudosSector() * GameBalance.DEFENSA_COSTE_ESCUDO;
            case TORRETA_NEOCROMO -> sector.getCantidadTorretasSector() * GameBalance.DEFENSA_COSTE_TORRETA;
            case CANON_HEXALIUM -> sector.getCantidadCanonesSector() * GameBalance.DEFENSA_COSTE_CANON;
            default -> 0;
        };
    }

    private boolean esEdificioProductivoDeRecursos(Edificio.TipoEdificio tipo) {
        return EnumSet.of(
                Edificio.TipoEdificio.MINA_NEOCROMO,
                Edificio.TipoEdificio.MINA_UMBRIUM,
                Edificio.TipoEdificio.MINA_SYNTHERIUM,
                Edificio.TipoEdificio.MINA_HEXALIUM,
                Edificio.TipoEdificio.MINA_VOIDIUM,
                Edificio.TipoEdificio.GRANJA_KROMAFRUTA,
                Edificio.TipoEdificio.GRANJA_NEUROTRIGO,
                Edificio.TipoEdificio.GRANJA_ALGACARNE,
                Edificio.TipoEdificio.CRIADERO_RATAX,
                Edificio.TipoEdificio.CULTIVO_FLORSOMNIO,
                Edificio.TipoEdificio.LAB_REFLEXA,
                Edificio.TipoEdificio.LAB_NANOCURA,
                Edificio.TipoEdificio.LAB_SOMNEX
        ).contains(tipo);
    }

    private ResourceType convertirRecurso(Edificio.TipoEdificio tipo) {

        return switch (tipo) {

            case MINA_NEOCROMO -> ResourceType.NEOCROMO;
            case MINA_UMBRIUM -> ResourceType.UMBRIUM;
            case MINA_SYNTHERIUM -> ResourceType.SYNTHERIUM;
            case MINA_HEXALIUM -> ResourceType.HEXALIUM;
            case MINA_VOIDIUM -> ResourceType.VOIDIUM;

            case GRANJA_KROMAFRUTA -> ResourceType.KROMAFRUTA;
            case GRANJA_NEUROTRIGO -> ResourceType.NEUROTRIGO;
            case GRANJA_ALGACARNE -> ResourceType.ALGACARNE;
            case CRIADERO_RATAX -> ResourceType.RATAX;
            case CULTIVO_FLORSOMNIO -> ResourceType.FLORSOMNIO;

            case LAB_REFLEXA -> ResourceType.REFLEXA;
            case LAB_NANOCURA -> ResourceType.NANOCURA;
            case LAB_SOMNEX -> ResourceType.SOMNEX;

            default -> null;
        };
    }

    private record ProduccionPendiente(Trabajador trabajador, ResourceType recurso, int produccionBase) {
    }

    // ================= UTILIDADES =================

    private void procesarViajes(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.avanzarViaje();
        }
    }

    private void liberarTrabajadoresConstruccion(Colonia colonia, ConstruccionEnCurso construccion) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje instanceof Trabajador trabajador) {

                if (trabajador.getConstruccionAsignada() == construccion) {
                    MapSector origen = trabajador.getSectorAsignado() != null
                            ? trabajador.getSectorAsignado()
                            : construccion.getSectorDestino();
                    trabajador.setConstruccionAsignada(null);
                    trabajador.setSectorAsignado(null);
                    trabajador.iniciarViaje(origen, com.cyberpunk.util.TravelCalculator.calcularTicks(origen, colonia.getSectorNave()));
                }
            }
        }
    }

    private void enviarTrabajadorANavePorAgotamiento(Trabajador trabajador) {
        MapSector origen = trabajador.getSectorActual();
        trabajador.setConstruccionAsignada(null);
        trabajador.setSectorAsignado(null);
        if (trabajador.getColonia() != null) {
            trabajador.iniciarViaje(origen, com.cyberpunk.util.TravelCalculator.calcularTicks(origen, trabajador.getColonia().getSectorNave()));
        }
    }

    // ================= SISTEMA BIOLÓGICO =================

    private void procesarConsumoComida(Colonia colonia) {
        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.acumularConsumoComida(calcularConsumoComida(personaje));
        }
    }

    private int calcularConsumoComida(Personaje personaje) {

        if (personaje instanceof Trabajador trabajador) {

            if (trabajador.getConstruccionAsignada() != null) {
                return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_TRABAJANDO;
            }

            if (trabajador.getSectorAsignado() != null && trabajador.getSectorAsignado().getBuilding() != null) {
                return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_TRABAJANDO;
            }
        }

        return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_INACTIVO;
    }

    private void procesarAlimentacion(Colonia colonia) {

        var recursos = colonia.getRecursos();

        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.intentarComer(recursos);
        }
    }

    private void procesarDanioPorInanicion(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.sinComida()) {
                personaje.recibirDanio(com.cyberpunk.gameBalance.GameBalance.DANO_POR_INANICION);
            }
        }
    }

    private void procesarCuracionNatural(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.estaHerido() && personaje.getComida() > 60) {
                personaje.curar(com.cyberpunk.gameBalance.GameBalance.CURACION_NATURAL_POR_TICK);
            }
        }
    }

    private void procesarDescanso(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.estaDisponible()) {
                personaje.descansar();
            }
        }
    }
}