package com.cyberpunk.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.dto.DefensasIntelDTO;
import com.cyberpunk.dto.GuerreroIntelDTO;
import com.cyberpunk.dto.ReconocimientoSectorDTO;
import com.cyberpunk.dto.ResultadoAccionGuerrerosDTO;
import com.cyberpunk.dto.RondaBatallaDTO;
import com.cyberpunk.exception.EntityNotFoundException;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.gameBalance.GameBalance;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.PersonajeRepository;
import com.cyberpunk.repository.SectorExplorationRepository;
import com.cyberpunk.util.TravelCalculator;

@Service
public class GuerreroService {

    private final ColoniaRepository coloniaRepository;
    private final PersonajeRepository personajeRepository;
    private final MapSectorRepository mapSectorRepository;
    private final SectorExplorationRepository sectorExplorationRepository;
    private final MapService mapService;
    private final ExplorationService explorationService;
    private final Random random = new Random();

    public GuerreroService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository,
            MapSectorRepository mapSectorRepository,
            SectorExplorationRepository sectorExplorationRepository,
            MapService mapService,
            ExplorationService explorationService) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.sectorExplorationRepository = sectorExplorationRepository;
        this.mapService = mapService;
        this.explorationService = explorationService;
    }

    @Transactional
    public ResultadoAccionGuerrerosDTO desplegarGuerreros(Long usuarioId, List<Long> guerreroIds, int x, int y) {
        if (usuarioId == null || guerreroIds == null || guerreroIds.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar usuario y al menos un guerrero");
        }

        Colonia coloniaAtacante = coloniaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia atacante no encontrada"));

        List<Guerrero> guerreros = obtenerGuerrerosSeleccionados(coloniaAtacante, guerreroIds);
        var sectorExistente = mapSectorRepository.findByXAndY(x, y);
        boolean explorado = sectorExistente
                .map(sector -> sectorExplorationRepository.existsByUsuarioIdAndSectorId(usuarioId, sector.getId()))
                .orElse(false);
        MapSector sectorDestino = mapService.getOrGenerateSector(x, y);
        int tiempoIda = calcularTiempoIda(guerreros, sectorDestino);
        int tiempoVuelta = calcularTiempoVuelta(coloniaAtacante, sectorDestino);

        if (!explorado) {
            programarMision(guerreros, sectorDestino, tiempoIda, tiempoVuelta, "RECON");

            ResultadoAccionGuerrerosDTO resultado = new ResultadoAccionGuerrerosDTO();
            resultado.setAccion("RECON");
            resultado.setResultado("EN_CAMINO");
            resultado.setMensaje("El reconocimiento se resolverá cuando los guerreros lleguen al sector");
            resultado.setGuerrerosEnviados(guerreros.size());
            resultado.setGuerrerosOperativos(contarOperativos(guerreros));
            resultado.setDefensoresOperativos(0);
            resultado.setTiempoViajeIda(tiempoIda);
            resultado.setTiempoViajeVuelta(tiempoVuelta);
            return resultado;
        }

        boolean enemigo = sectorDestino.getOwner() != null && !sectorDestino.getOwner().getId().equals(usuarioId);

        if (enemigo) {
            programarMision(guerreros, sectorDestino, tiempoIda, tiempoVuelta, "ATTACK");

            ResultadoAccionGuerrerosDTO resultado = new ResultadoAccionGuerrerosDTO();
            resultado.setAccion("ATTACK");
            resultado.setResultado("EN_CAMINO");
            resultado.setMensaje("El ataque se resolverá cuando los guerreros lleguen al sector");
            resultado.setGuerrerosEnviados(guerreros.size());
            resultado.setGuerrerosOperativos(contarOperativos(guerreros));
            resultado.setDefensoresOperativos(0);
            resultado.setTiempoViajeIda(tiempoIda);
            resultado.setTiempoViajeVuelta(tiempoVuelta);
            return resultado;
        }

        estacionarGuerreros(coloniaAtacante, guerreros, sectorDestino, tiempoIda);

        ResultadoAccionGuerrerosDTO resultado = new ResultadoAccionGuerrerosDTO();
        resultado.setAccion("DEFENDER");
        resultado.setResultado("OK");
        resultado.setMensaje("Los guerreros se han desplegado en el sector y ahora lo defenderán al llegar");
        resultado.setGuerrerosEnviados(guerreros.size());
        resultado.setGuerrerosOperativos(contarOperativos(guerreros));
        resultado.setDefensoresOperativos(0);
        resultado.setTiempoViajeIda(tiempoIda);
        resultado.setTiempoViajeVuelta(0);
        return resultado;
    }

    @Transactional
    public void resolverMisionesAlLlegar(Colonia colonia) {
        List<Guerrero> guerrerosConMision = colonia.getPoblacion().stream()
                .filter(personaje -> personaje instanceof Guerrero)
                .map(personaje -> (Guerrero) personaje)
                .filter(Guerrero::puedeActuar)
                .filter(guerrero -> !guerrero.estaEnViaje())
                .filter(guerrero -> guerrero.getMisionPendiente() != null)
                .filter(guerrero -> guerrero.getSectorMision() != null)
                .sorted(Comparator.comparing(Guerrero::getMisionPendiente)
                        .thenComparing(guerrero -> guerrero.getSectorMision().getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (guerrerosConMision.isEmpty()) {
            return;
        }

        Map<String, List<Guerrero>> porMision = guerrerosConMision.stream()
                .collect(Collectors.groupingBy(guerrero -> guerrero.getMisionPendiente() + "#" + guerrero.getSectorMision().getId()));

        for (List<Guerrero> grupo : porMision.values()) {
            Guerrero lider = grupo.get(0);
            MapSector sectorMision = lider.getSectorMision();
            String mision = lider.getMisionPendiente();

            if ("RECON".equals(mision)) {
                realizarReconocimiento(colonia, grupo, sectorMision);
            } else if ("ATTACK".equals(mision)) {
                resolverAtaque(colonia, grupo, sectorMision);
            }

            iniciarRetornoTrasMision(colonia, grupo, sectorMision);
        }

        personajeRepository.saveAll(new ArrayList<>(guerrerosConMision));
    }

    private ResultadoAccionGuerrerosDTO realizarReconocimiento(Colonia coloniaAtacante, List<Guerrero> guerreros, MapSector sectorDestino) {
        int hackeoTotal = guerreros.stream().mapToInt(Guerrero::getHackeo).sum();
        int tirada = random.nextInt(6) + 1;

        int penalizacion = 0;
        Colonia coloniaDefensora = obtenerColoniaDefensora(sectorDestino);
        if (coloniaDefensora != null && defensasEnergizadas(coloniaDefensora, sectorDestino)) {
            penalizacion = getPenalizacionExploracion(coloniaDefensora, sectorDestino);
        }

        int puntuacion = Math.max(0, hackeoTotal + guerreros.size() + tirada - penalizacion);
        int intelNivel = calcularIntelNivel(puntuacion);

        ReconocimientoSectorDTO intel = new ReconocimientoSectorDTO();
        intel.setX(sectorDestino.getX());
        intel.setY(sectorDestino.getY());

        intel.setTerrain(sectorDestino.getTerrain().name());
        intel.setResource(sectorDestino.getResource().name());
        intel.setRichness(sectorDestino.getRichness());
        intel.setOwnerId(sectorDestino.getOwner() != null ? sectorDestino.getOwner().getId() : null);

        String building = sectorDestino.getBuilding() != null ? sectorDestino.getBuilding().name() : null;
        Integer buildingLevel = null;

        if (building == null && coloniaDefensora != null) {
            building = coloniaDefensora.getColaConstruccion().stream()
                    .filter(construccion -> construccion.getSectorDestino() != null)
                    .filter(construccion -> construccion.getSectorDestino().getId() != null)
                    .filter(construccion -> construccion.getSectorDestino().getId().equals(sectorDestino.getId()))
                    .map(ConstruccionEnCurso::getTipo)
                    .findFirst()
                    .orElse(null);

            if (building != null && intelNivel >= SectorExploration.INTEL_TACTICO) {
                buildingLevel = 0;
            }
        } else if (sectorDestino.getBuilding() != null && intelNivel >= SectorExploration.INTEL_TACTICO) {
            buildingLevel = sectorDestino.getBuildingLevel();
        }

        intel.setBuilding(building);
        intel.setBuildingLevel(buildingLevel);

        if (coloniaDefensora != null && intelNivel >= SectorExploration.INTEL_TACTICO) {
            List<Personaje> defensoresSector = obtenerDefensoresSector(coloniaDefensora, sectorDestino);
            int cantidadGuerreros = (int) defensoresSector.stream()
                    .filter(Guerrero.class::isInstance)
                    .count();
            int cantidadTrabajadores = (int) defensoresSector.stream()
                    .filter(Trabajador.class::isInstance)
                    .count();
                int cantidadDefensas = contarDefensasSector(coloniaDefensora, sectorDestino);

            intel.setCantidadGuerreros(cantidadGuerreros);
            intel.setCantidadTrabajadores(cantidadTrabajadores);
            intel.setCantidadDefensas(cantidadDefensas);

                if (intelNivel >= SectorExploration.INTEL_COMPLETO) {
                intel.setDefensas(crearDefensasIntel(coloniaDefensora, sectorDestino));

                List<GuerreroIntelDTO> guerrerosDefensores = defensoresSector.stream()
                        .filter(Guerrero.class::isInstance)
                        .map(Guerrero.class::cast)
                        .map(defensor -> new GuerreroIntelDTO(
                                defensor.getId(),
                                defensor.getNombre(),
                                defensor.getFuerza(),
                                defensor.getDestreza(),
                                defensor.getResistencia(),
                                defensor.getHackeo()))
                        .toList();

                intel.setGuerreros(guerrerosDefensores);
            }
        }

        explorationService.marcarSectorVisibleConIntel(coloniaAtacante.getUsuario(), sectorDestino, intelNivel);

        ResultadoAccionGuerrerosDTO resultado = new ResultadoAccionGuerrerosDTO();
        resultado.setAccion("RECON");
        resultado.setResultado(puntuacion >= 8 ? "OK" : "PARCIAL");
        resultado.setMensaje(puntuacion >= 10
                ? "Reconocimiento excelente: datos tácticos completos obtenidos"
                : puntuacion >= 7
                ? "Reconocimiento correcto: se obtuvo inteligencia táctica parcial"
                : "Reconocimiento básico: solo se obtuvo información estructural del sector");
        resultado.setGuerrerosEnviados(guerreros.size());
        resultado.setGuerrerosOperativos(contarOperativos(guerreros));
        resultado.setDefensoresOperativos(0);
        resultado.setReconocimiento(intel);
        return resultado;
    }

    private int calcularIntelNivel(int puntuacion) {
        if (puntuacion >= 10) {
            return SectorExploration.INTEL_COMPLETO;
        }
        if (puntuacion >= 7) {
            return SectorExploration.INTEL_TACTICO;
        }
        return SectorExploration.INTEL_BASE;
    }

    @SuppressWarnings("null")
    private ResultadoAccionGuerrerosDTO resolverAtaque(Colonia coloniaAtacante, List<Guerrero> atacantes, MapSector sectorDestino) {
        Colonia coloniaDefensora = obtenerColoniaDefensora(sectorDestino);
        if (coloniaDefensora == null) {
            throw new GameRuleViolationException("No existe colonia defensora en ese sector");
        }

        List<Personaje> defensores = obtenerDefensoresSector(coloniaDefensora, sectorDestino);
        boolean objetivoEsNave = sectorDestino.getId().equals(coloniaDefensora.getSectorNave().getId());
        List<RondaBatallaDTO> informeRondas = new ArrayList<>();

        int numeroRonda = 0;
        while (contarOperativos(atacantes) > 0 && hayDefensaOperativa(coloniaDefensora, sectorDestino, defensores) && numeroRonda < 12) {
            RondaBatallaDTO ronda = new RondaBatallaDTO();
            ronda.setRonda(numeroRonda + 1);

            aplicarDanioDefensas(coloniaDefensora, sectorDestino, atacantes, objetivoEsNave);
            int danioDefensas = ejecutarTurnoAtaque(atacantes, defensores, coloniaDefensora, sectorDestino);
            ejecutarTurnoDefensa(defensores, atacantes, coloniaDefensora, sectorDestino);

            ronda.setAtacantesOperativos(contarOperativos(atacantes));
            ronda.setDefensoresOperativos(contarGuerrerosDefensoresOperativos(defensores));
            ronda.setTrabajadoresDefensoresOperativos(contarTrabajadoresDefensoresOperativos(defensores));
            ronda.setDanioDefensas(danioDefensas);
            ronda.setDefensasDestruidas(!defensasActivasEnSector(coloniaDefensora, sectorDestino));
            ronda.setResumen(crearResumenRonda(ronda));
            informeRondas.add(ronda);

            numeroRonda++;
        }

        boolean victoria = contarOperativos(atacantes) > 0 && !hayDefensaOperativa(coloniaDefensora, sectorDestino, defensores);
        Map<String, Integer> botin = new HashMap<>();

        if (victoria) {
            if (objetivoEsNave) {
                botin = saquear(coloniaAtacante, coloniaDefensora, true);
            } else {
                sectorDestino.setOwner(coloniaAtacante.getUsuario());
                mapSectorRepository.save(sectorDestino);
            }
        }

        personajeRepository.saveAll(new ArrayList<>(atacantes));
        personajeRepository.saveAll(defensores);
        coloniaRepository.save(coloniaAtacante);
        coloniaRepository.save(coloniaDefensora);

        ResultadoAccionGuerrerosDTO resultado = new ResultadoAccionGuerrerosDTO();
        resultado.setAccion("ATTACK");
        resultado.setResultado(victoria ? "VICTORIA" : "RETIRADA");
        resultado.setMensaje(victoria
                ? "El ataque ha tenido éxito y se ha realizado saqueo"
                : "Los atacantes no han logrado despejar el sector enemigo");
        resultado.setGuerrerosEnviados(atacantes.size());
        resultado.setGuerrerosOperativos(contarOperativos(atacantes));
        resultado.setDefensoresOperativos(contarGuerrerosDefensoresOperativos(defensores) + contarTrabajadoresDefensoresOperativos(defensores));
        resultado.setBotin(botin);
        resultado.setTotalBotin(botin.values().stream().mapToInt(Integer::intValue).sum());
        resultado.setRondas(informeRondas);
        return resultado;
    }

    private void aplicarDanioDefensas(Colonia coloniaDefensora, MapSector sectorDestino, List<Guerrero> atacantes, boolean objetivoEsNave) {
        if (!defensasEnergizadas(coloniaDefensora, sectorDestino)) {
            return;
        }

        int danio = calcularDanioDefensas(coloniaDefensora, sectorDestino, objetivoEsNave);
        List<Guerrero> vivos = atacantes.stream().filter(Personaje::puedeActuar).collect(Collectors.toList());
        if (danio <= 0 || vivos.isEmpty()) {
            return;
        }

        int impactoPorGuerrero = Math.max(1, danio / vivos.size());
        for (Guerrero guerrero : vivos) {
            guerrero.recibirDanio(impactoPorGuerrero);
        }
    }

    private int ejecutarTurnoAtaque(List<Guerrero> atacantes, List<Personaje> defensores, Colonia coloniaDefensora, MapSector sectorDestino) {
        List<Personaje> objetivos = seleccionarObjetivosParaAtacantes(defensores, coloniaDefensora, sectorDestino);
        if (objetivos.isEmpty()) {
            int fuerzaTotal = atacantes.stream().filter(Personaje::puedeActuar).mapToInt(Guerrero::getFuerza).sum();
            if (defensasActivasEnSector(coloniaDefensora, sectorDestino) && defensasEnergizadas(coloniaDefensora, sectorDestino) && fuerzaTotal > 0) {
                recibirAtaqueDefensas(coloniaDefensora, sectorDestino, fuerzaTotal);
                return fuerzaTotal;
            }
            return 0;
        }

        for (Guerrero atacante : atacantes) {
            if (!atacante.puedeActuar()) {
                continue;
            }

            objetivos = seleccionarObjetivosParaAtacantes(defensores, coloniaDefensora, sectorDestino);
            if (objetivos.isEmpty()) {
                break;
            }

            Personaje objetivo = objetivos.get(random.nextInt(objetivos.size()));
            aplicarGolpe(atacante, objetivo);
        }

        return 0;
    }

    private void ejecutarTurnoDefensa(List<Personaje> defensores, List<Guerrero> atacantes, Colonia coloniaDefensora, MapSector sectorDestino) {
        List<Guerrero> guerrerosDefensores = defensores.stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> personaje instanceof Guerrero)
                .map(personaje -> (Guerrero) personaje)
                .toList();

        if (!guerrerosDefensores.isEmpty()) {
            for (Guerrero guerreroDefensor : guerrerosDefensores) {
                atacarAtacanteAleatorio(guerreroDefensor, atacantes);
            }
            return;
        }

        if (defensasActivasEnSector(coloniaDefensora, sectorDestino) && defensasEnergizadas(coloniaDefensora, sectorDestino)) {
            return;
        }

        for (Personaje defensor : defensores) {
            if (!(defensor instanceof Trabajador trabajador) || !trabajador.puedeActuar()) {
                continue;
            }

            aplicarGolpeTrabajador(atacantes);
        }
    }

    private void atacarAtacanteAleatorio(Guerrero atacante, List<Guerrero> atacantesEnemigos) {
        List<Guerrero> objetivos = atacantesEnemigos.stream().filter(Personaje::puedeActuar).collect(Collectors.toList());
        if (objetivos.isEmpty()) {
            return;
        }

        Guerrero objetivo = objetivos.get(random.nextInt(objetivos.size()));
        aplicarGolpe(atacante, objetivo);
    }

    private void aplicarGolpeTrabajador(List<Guerrero> atacantes) {
        List<Guerrero> objetivos = atacantes.stream().filter(Personaje::puedeActuar).collect(Collectors.toList());
        if (objetivos.isEmpty()) {
            return;
        }

        Guerrero objetivo = objetivos.get(random.nextInt(objetivos.size()));
        aplicarGolpeLigero(objetivo);
    }

    @SuppressWarnings("null")
    private void aplicarGolpeLigero(Personaje defensor) {
        double probEsquivar = GameBalance.PROB_ESQUIVAR_BASE;
        if (defensor instanceof Guerrero guerreroDefensor) {
            probEsquivar += guerreroDefensor.getDestreza() * GameBalance.PROB_POR_DESTREZA;
        }

        if (random.nextDouble() < Math.min(0.45, probEsquivar)) {
            return;
        }

        int danio = 1;
        if (random.nextDouble() < Math.min(0.25, GameBalance.PROB_CRIT_BASE + GameBalance.PROB_POR_DESTREZA)) {
            danio *= 2;
        }

        int mitigacion = 0;
        if (defensor instanceof Guerrero guerreroDefensor) {
            mitigacion = guerreroDefensor.getResistencia() / 2;
        }
        defensor.recibirDanio(Math.max(1, danio - mitigacion));
    }

    @SuppressWarnings("null")
    private void aplicarGolpe(Guerrero atacante, Personaje defensor) {
        double probEsquivar = GameBalance.PROB_ESQUIVAR_BASE;
        if (defensor instanceof Guerrero guerreroDefensor) {
            probEsquivar += guerreroDefensor.getDestreza() * GameBalance.PROB_POR_DESTREZA;
        }

        if (random.nextDouble() < Math.min(0.45, probEsquivar)) {
            return;
        }

        int danio = atacante.getFuerza();
        double probCritico = Math.min(0.45, GameBalance.PROB_CRIT_BASE + (atacante.getDestreza() * GameBalance.PROB_POR_DESTREZA));
        if (random.nextDouble() < probCritico) {
            danio *= 2;
        }

        int mitigacion = 0;
        if (defensor instanceof Guerrero guerreroDefensor) {
            mitigacion = guerreroDefensor.getResistencia() / 2;
        }
        defensor.recibirDanio(Math.max(1, danio - mitigacion));
    }

    private Map<String, Integer> saquear(Colonia atacante, Colonia defensora, boolean objetivoEsNave) {
        int porcentaje = objetivoEsNave ? GameBalance.SAQUEO_NAVE_PORCENTAJE : 0;
        Map<String, Integer> resultado = new HashMap<>();

        if (porcentaje <= 0) {
            return resultado;
        }

        for (var entry : defensora.getRecursos().getTodos().entrySet()) {
            ResourceType recurso = entry.getKey();
            if (recurso == ResourceType.EXPLORACION) {
                continue;
            }

            int cantidad = (entry.getValue() * porcentaje) / 100;
            if (cantidad <= 0) {
                continue;
            }

            defensora.getRecursos().consumir(recurso, cantidad);
            atacante.getRecursos().add(recurso, cantidad);
            resultado.put(recurso.name(), cantidad);
        }

        return resultado;
    }

    @SuppressWarnings("null")
    private void estacionarGuerreros(Colonia colonia, List<Guerrero> guerreros, MapSector sectorDestino, int tiempoIda) {
        for (Guerrero guerrero : guerreros) {
            MapSector origen = guerrero.getSectorActual();
            guerrero.forzarReposo();
            guerrero.setSectorAsignado(sectorDestino);
            guerrero.setVision(GameBalance.VISION_GUERRERO_DESPLEGADO);
            guerrero.iniciarViaje(origen, tiempoIda);
        }

        if (tiempoIda == 0) {
            explorationService.revelarDesdeSector(colonia.getUsuario(), sectorDestino, GameBalance.VISION_GUERRERO_DESPLEGADO);
        }
        personajeRepository.saveAll(new ArrayList<>(guerreros));
        coloniaRepository.save(colonia);
    }

    @SuppressWarnings("null")
    private void iniciarRetornoTrasMision(Colonia colonia, List<Guerrero> guerreros, MapSector sectorMision) {
        for (Guerrero guerrero : guerreros) {
            int ticksVuelta = guerrero.getTicksVueltaMision();
            guerrero.setMisionPendiente(null);
            guerrero.setSectorMision(null);
            guerrero.setTicksVueltaMision(0);
            guerrero.setSectorAsignado(null);
            guerrero.iniciarViaje(sectorMision, ticksVuelta);
        }

        coloniaRepository.save(colonia);
    }

    private void programarMision(List<Guerrero> guerreros, MapSector sectorMision, int tiempoIda, int tiempoVuelta, String tipoMision) {
        for (Guerrero guerrero : guerreros) {
            MapSector origen = guerrero.getSectorActual();
            guerrero.forzarReposo();
            guerrero.setVision(GameBalance.VISION_GUERRERO_DESPLEGADO);
            guerrero.setSectorAsignado(sectorMision);
            guerrero.setMisionPendiente(tipoMision);
            guerrero.setSectorMision(sectorMision);
            guerrero.setTicksVueltaMision(tiempoVuelta);
            guerrero.iniciarViaje(origen, tiempoIda);
        }

        personajeRepository.saveAll(new ArrayList<>(guerreros));
    }

    private List<Guerrero> obtenerGuerrerosSeleccionados(Colonia colonia, List<Long> ids) {
        List<Guerrero> guerreros = colonia.getPoblacion().stream()
                .filter(personaje -> personaje instanceof Guerrero)
                .map(personaje -> (Guerrero) personaje)
                .filter(guerrero -> ids.contains(guerrero.getId()))
                .toList();

        if (guerreros.size() != ids.size()) {
            throw new GameRuleViolationException("Has enviado guerreros que no pertenecen a la colonia");
        }

        if (guerreros.stream().anyMatch(guerrero -> !guerrero.puedeActuar())) {
            throw new GameRuleViolationException("Uno o más guerreros están incapacitados y no pueden actuar");
        }

        if (guerreros.stream().anyMatch(Guerrero::estaEnViaje)) {
            throw new GameRuleViolationException("Uno o más guerreros están viajando y no pueden recibir órdenes");
        }

        if (guerreros.stream().anyMatch(guerrero -> guerrero.getMisionPendiente() != null)) {
            throw new GameRuleViolationException("Uno o más guerreros ya tienen una misión en curso");
        }

        return guerreros;
    }

    private Colonia obtenerColoniaDefensora(MapSector sectorDestino) {
        if (sectorDestino.getOwner() == null) {
            return null;
        }

        return coloniaRepository.findByUsuarioId(sectorDestino.getOwner().getId()).orElse(null);
    }

    private boolean esSectorNave(Colonia colonia, MapSector sector) {
        if (colonia == null) {
            return false;
        }

        MapSector sectorNave = colonia.getSectorNave();
        if (sectorNave == null || sector == null) {
            return false;
        }

        if (sectorNave.getId() != null && sector.getId() != null) {
            return sectorNave.getId().equals(sector.getId());
        }

        return sectorNave.getX() == sector.getX() && sectorNave.getY() == sector.getY();
    }

    private int contarDefensas(Colonia colonia) {
        return colonia.getDefensas().getEscudos()
                + colonia.getDefensas().getTorretasNeocromo()
                + colonia.getDefensas().getCanonesHexalium();
    }

    private int contarDefensasSector(Colonia colonia, MapSector sector) {
        if (esSectorNave(colonia, sector)) {
            return contarDefensas(colonia);
        }

        return sector.getCantidadDefensasSector();
    }

    private DefensasIntelDTO crearDefensasIntel(Colonia colonia, MapSector sector) {
        if (esSectorNave(colonia, sector)) {
            return new DefensasIntelDTO(
                    colonia.getDefensas().getEscudos(),
                    colonia.getDefensas().getTorretasNeocromo(),
                    colonia.getDefensas().getCanonesHexalium());
        }

        if (!sector.tieneDefensaSectorial()) {
            return null;
        }

        return new DefensasIntelDTO(
                sector.getCantidadEscudosSector(),
                sector.getCantidadTorretasSector(),
                sector.getCantidadCanonesSector());
    }

    private int getPenalizacionExploracion(Colonia colonia, MapSector sector) {
        if (esSectorNave(colonia, sector)) {
            return colonia.getDefensas().getPenalizacionExploracion();
        }

        return sector.getPenalizacionExploracionSector();
    }

    private int calcularDanioDefensas(Colonia colonia, MapSector sector, boolean objetivoEsNave) {
        if (esSectorNave(colonia, sector)) {
            return colonia.getDefensas().calcularDanioConstanteAtaque(objetivoEsNave);
        }

        return sector.calcularDanioConstanteDefensaSector();
    }

    private void recibirAtaqueDefensas(Colonia colonia, MapSector sector, int fuerzaTotal) {
        if (esSectorNave(colonia, sector)) {
            colonia.getDefensas().recibirAtaqueEnSector(fuerzaTotal);
            return;
        }

        sector.recibirAtaqueDefensaSector(fuerzaTotal);
        mapSectorRepository.save(sector);
    }

    private boolean defensasActivasEnSector(Colonia colonia, MapSector sector) {
        if (esSectorNave(colonia, sector)) {
            return colonia.getDefensas().defensasActivas();
        }

        return sector.defensasActivasSector();
    }

    private List<Personaje> obtenerDefensoresSector(Colonia coloniaDefensora, MapSector sectorDestino) {
        return coloniaDefensora.getPoblacion().stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> {
                    MapSector actual = personaje.getSectorActual();
                    return actual != null && actual.getId().equals(sectorDestino.getId());
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hayDefensaOperativa(Colonia coloniaDefensora, MapSector sectorDestino, List<Personaje> defensores) {
        boolean defensasActivasConEnergia = defensasActivasEnSector(coloniaDefensora, sectorDestino)
            && defensasEnergizadas(coloniaDefensora, sectorDestino);

        return contarGuerrerosDefensoresOperativos(defensores) > 0
            || (!defensasActivasConEnergia && contarTrabajadoresDefensoresOperativos(defensores) > 0)
            || defensasActivasConEnergia;
    }

    private List<Personaje> seleccionarObjetivosParaAtacantes(List<Personaje> defensores, Colonia coloniaDefensora, MapSector sectorDestino) {
        List<Personaje> guerreros = defensores.stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> personaje instanceof Guerrero)
                .collect(Collectors.toList());

        if (!guerreros.isEmpty()) {
            return guerreros;
        }

        if (defensasActivasEnSector(coloniaDefensora, sectorDestino) && defensasEnergizadas(coloniaDefensora, sectorDestino)) {
            return List.of();
        }

        return defensores.stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> personaje instanceof Trabajador)
                .collect(Collectors.toList());
    }

    private int contarGuerrerosDefensoresOperativos(List<Personaje> defensores) {
        return (int) defensores.stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> personaje instanceof Guerrero)
                .count();
    }

    private int contarTrabajadoresDefensoresOperativos(List<Personaje> defensores) {
        return (int) defensores.stream()
                .filter(Personaje::puedeActuar)
                .filter(personaje -> personaje instanceof Trabajador)
                .count();
    }

    private int calcularTiempoIda(List<Guerrero> guerreros, MapSector destino) {
        return guerreros.stream()
                .mapToInt(guerrero -> TravelCalculator.calcularTicks(guerrero.getSectorActual(), destino))
                .max()
                .orElse(0);
    }

    private int calcularTiempoVuelta(Colonia coloniaAtacante, MapSector origen) {
        return TravelCalculator.calcularTicks(origen, coloniaAtacante.getSectorNave());
    }

    private String crearResumenRonda(RondaBatallaDTO ronda) {
        return "Ronda " + ronda.getRonda()
                + ": atacantes=" + ronda.getAtacantesOperativos()
                + ", guerreros defensores=" + ronda.getDefensoresOperativos()
                + ", trabajadores defensores=" + ronda.getTrabajadoresDefensoresOperativos()
                + ", daño a defensas=" + ronda.getDanioDefensas()
                + ", defensasDestruidas=" + ronda.isDefensasDestruidas();
    }

    private int contarOperativos(List<? extends Personaje> personajes) {
        return (int) personajes.stream().filter(Personaje::puedeActuar).count();
    }

    private boolean defensasEnergizadas(Colonia coloniaDefensora, MapSector sectorDestino) {
        return contarDefensasSector(coloniaDefensora, sectorDestino) > 0
                && coloniaDefensora.getRecursos().getEnergiaDisponible() >= 0;
    }
}