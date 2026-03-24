package com.cyberpunk.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.MapSector.SectorResource;
import com.cyberpunk.domain.map.MapSector.TerrainType;
import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.dto.DefensasIntelDTO;
import com.cyberpunk.dto.GuerreroIntelDTO;
import com.cyberpunk.dto.MapSectorDTO;
import com.cyberpunk.dto.PersonajeSectorDTO;
import com.cyberpunk.dto.SectorDetalleDTO;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.SectorExplorationRepository;

@Service
public class MapService {

    private final MapSectorRepository mapSectorRepository;
    private final SectorExplorationRepository sectorExplorationRepository;
    private final ColoniaRepository coloniaRepository;

    private final Random random = new Random();

    public MapService(
            MapSectorRepository mapSectorRepository,
            SectorExplorationRepository sectorExplorationRepository,
            ColoniaRepository coloniaRepository) {

        this.mapSectorRepository = mapSectorRepository;
        this.sectorExplorationRepository = sectorExplorationRepository;
        this.coloniaRepository = coloniaRepository;
    }

    public MapSector getOrGenerateSector(int x, int y) {

        return mapSectorRepository.findByXAndY(x, y)
            .orElseGet(() -> {
                try {
                    return generarSector(x, y);
                } catch (Exception e) {
                    // 🔴 retry si otro hilo lo creó
                    return mapSectorRepository.findByXAndY(x, y)
                            .orElseThrow();
                }
            });
    }

    public MapSector getSectorExplorado(Long usuarioId, int x, int y) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId no puede ser null");
        }

        MapSector sector = mapSectorRepository.findByXAndY(x, y)
                .orElseThrow(() -> new GameRuleViolationException("El sector no ha sido explorado por el jugador"));

        boolean explorado = sectorExplorationRepository.existsByUsuarioIdAndSectorId(usuarioId, sector.getId());
        if (!explorado) {
            throw new GameRuleViolationException("El sector no ha sido explorado por el jugador");
        }

        return sector;
    }

    public SectorDetalleDTO getSectorDetalle(Long usuarioId, int x, int y) {
        MapSector sector = getSectorExplorado(usuarioId, x, y);
        Optional<Colonia> coloniaPropietariaOpt = sector.getOwner() == null
            ? Optional.empty()
            : coloniaRepository.findByUsuarioId(sector.getOwner().getId());

        boolean esPropio = sector.getOwner() != null && sector.getOwner().getId().equals(usuarioId);
        int intelNivel = resolverIntelNivel(usuarioId, sector, esPropio);

        String building = sector.getBuilding() != null ? sector.getBuilding().name() : null;
        Integer buildingLevel = (sector.getBuilding() != null && intelNivel >= SectorExploration.INTEL_TACTICO)
            ? sector.getBuildingLevel()
            : null;

        List<PersonajeSectorDTO> trabajadores = List.of();
        List<GuerreroIntelDTO> guerreros = List.of();
        Integer cantidadTrabajadores = null;
        Integer cantidadGuerreros = null;
        Integer cantidadDefensas = null;
        DefensasIntelDTO defensas = null;

        if (coloniaPropietariaOpt.isPresent()) {
            Colonia coloniaPropietaria = coloniaPropietariaOpt.get();
            boolean esSectorNave = esSectorNave(coloniaPropietaria, sector);

            if (building == null) {
            Optional<ConstruccionEnCurso> construccionEnSector = coloniaPropietaria.getColaConstruccion().stream()
                .filter(c -> c.getSectorDestino() != null)
                .filter(c -> c.getSectorDestino().getId() != null)
                .filter(c -> c.getSectorDestino().getId().equals(sector.getId()))
                .findFirst();

            if (construccionEnSector.isPresent()) {
                building = construccionEnSector.get().getTipo();
                buildingLevel = intelNivel >= SectorExploration.INTEL_TACTICO ? 0 : null;
            }
            }

            List<Trabajador> trabajadoresSector = coloniaPropietaria.getPoblacion().stream()
                    .filter(personaje -> personajeAsignadoOEnSector(personaje, sector, coloniaPropietaria))
                    .filter(Trabajador.class::isInstance)
                .map(Trabajador.class::cast)
                    .toList();

            List<Guerrero> guerrerosSector = coloniaPropietaria.getPoblacion().stream()
                    .filter(personaje -> personajeAsignadoOEnSector(personaje, sector, coloniaPropietaria))
                    .filter(Guerrero.class::isInstance)
                .map(Guerrero.class::cast)
                    .toList();

            if (esPropio || intelNivel >= SectorExploration.INTEL_TACTICO) {
            cantidadTrabajadores = trabajadoresSector.size();
            cantidadGuerreros = guerrerosSector.size();
            cantidadDefensas = contarDefensasSector(coloniaPropietaria, sector, esSectorNave);
            }

            if (esPropio || intelNivel >= SectorExploration.INTEL_COMPLETO) {
            defensas = construirDefensasIntel(coloniaPropietaria, sector, esSectorNave);

            trabajadores = trabajadoresSector.stream()
                .map(trabajador -> new PersonajeSectorDTO(trabajador.getId(), trabajador.getNombre()))
                .toList();

            guerreros = guerrerosSector.stream()
                .map(guerrero -> new GuerreroIntelDTO(
                    guerrero.getId(),
                    guerrero.getNombre(),
                    guerrero.getFuerza(),
                    guerrero.getDestreza(),
                    guerrero.getResistencia(),
                    guerrero.getHackeo()))
                .toList();
            }
        }

        return new SectorDetalleDTO(
                sector.getId(),
                sector.getX(),
                sector.getY(),
                sector.getTerrain().name(),
                sector.getResource().name(),
                sector.getRichness(),
                building,
                buildingLevel,
                sector.getOwner() != null ? sector.getOwner().getId() : null,
            cantidadTrabajadores,
            cantidadGuerreros,
            cantidadDefensas,
            defensas,
                trabajadores,
                guerreros);
    }

        private int resolverIntelNivel(Long usuarioId, MapSector sector, boolean esPropio) {
        if (esPropio || sector.getOwner() == null) {
            return SectorExploration.INTEL_COMPLETO;
        }

        return sectorExplorationRepository.findByUsuarioIdAndSectorId(usuarioId, sector.getId())
            .map(exploracion -> Math.max(SectorExploration.INTEL_BASE, exploracion.getIntelNivel()))
            .orElse(SectorExploration.INTEL_BASE);
        }

    private boolean personajeAsignadoOEnSector(Personaje personaje, MapSector sector, Colonia colonia) {
        if (personaje == null || sector == null) {
            return false;
        }

        MapSector sectorAsignado = personaje.getSectorAsignado();
        if (sectorAsignado != null) {
            return mismoSector(sectorAsignado, sector);
        }

        if (!esSectorNave(colonia, sector) || personaje.estaEnViaje()) {
            return false;
        }

        return mismoSector(personaje.getSectorActual(), sector);
    }

    private boolean esSectorNave(Colonia colonia, MapSector sector) {
        return colonia != null && mismoSector(colonia.getSectorNave(), sector);
    }

    private int contarDefensas(Colonia colonia) {
        return colonia.getDefensas().getEscudos()
            + colonia.getDefensas().getTorretasNeocromo()
            + colonia.getDefensas().getCanonesHexalium();
    }

    private int contarDefensasSector(Colonia colonia, MapSector sector, boolean esSectorNave) {
        if (esSectorNave) {
            return contarDefensas(colonia);
        }

        return sector.getCantidadDefensasSector();
    }

    private DefensasIntelDTO construirDefensasIntel(Colonia colonia, MapSector sector, boolean esSectorNave) {
        if (esSectorNave) {
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

    private boolean mismoSector(MapSector primero, MapSector segundo) {
        if (primero == null || segundo == null) {
            return false;
        }

        if (primero.getId() != null && segundo.getId() != null) {
            return primero.getId().equals(segundo.getId());
        }

        return primero.getX() == segundo.getX() && primero.getY() == segundo.getY();
    }

    public String getMapaVisibleComoTexto(Long usuarioId) {

        List<SectorExploration> exploraciones = sectorExplorationRepository.findByUsuarioId(usuarioId);

        // Obtener colonia del usuario para la nave y construcciones
        Colonia colonia = coloniaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        MapSector naveSector = colonia.getSectorNave();

        // Set de sectores con construcción en curso
        Set<MapSector> sectoresConstruccion = colonia.getColaConstruccion().stream()
                .map(ConstruccionEnCurso::getSectorDestino)
                .collect(Collectors.toSet());

        // Encontrar límites del mapa
        int minX = exploraciones.stream().mapToInt(e -> e.getSector().getX()).min().orElse(0);
        int maxX = exploraciones.stream().mapToInt(e -> e.getSector().getX()).max().orElse(0);
        int minY = exploraciones.stream().mapToInt(e -> e.getSector().getY()).min().orElse(0);
        int maxY = exploraciones.stream().mapToInt(e -> e.getSector().getY()).max().orElse(0);

        // Crear mapa de exploraciones
        Map<Integer, Map<Integer, SectorExploration>> mapaExploraciones = new HashMap<>();
        for (SectorExploration exp : exploraciones) {
            MapSector sector = exp.getSector();
            mapaExploraciones.computeIfAbsent(sector.getY(), k -> new HashMap<>()).put(sector.getX(), exp);
        }

        // Generar tabla ASCII
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int x = minX; x <= maxX; x++) {
            sb.append(String.format("%2d", x % 100)); // Últimos 2 dígitos para simplicidad
        }
        sb.append("\n");

        for (int y = minY; y <= maxY; y++) {
            sb.append(String.format("%2d ", y));
            for (int x = minX; x <= maxX; x++) {
                SectorExploration exp = mapaExploraciones.getOrDefault(y, new HashMap<>()).get(x);
                char simbolo = 'X'; // No explorado
                if (exp != null) {
                    if (exp.isVisible()) {
                        simbolo = 'e'; // Explorado
                        if (naveSector != null && naveSector.getX() == x && naveSector.getY() == y) {
                            simbolo = 'N'; // Nave
                        } else if (sectoresConstruccion.contains(exp.getSector())) {
                            simbolo = 'c'; // Construcción en curso
                        } else if (exp.getSector().getBuilding() != null) {
                            TipoEdificio tipo = exp.getSector().getBuilding();
                            if (tipo.name().startsWith("MINA")) {
                                simbolo = 'M';
                            } else if (tipo.name().startsWith("GRANJA")) {
                                simbolo = 'G';
                            } else {
                                simbolo = 'B';
                            }
                        }
                    } else {
                        simbolo = 'f'; // Niebla de guerra
                    }
                }
                sb.append(simbolo);
            }
            sb.append("\n");
        }

        sb.append("\nLeyenda:\n");
        sb.append("X: sector no explorado\n");
        sb.append("f: niebla de guerra (explorado pero no visible)\n");
        sb.append("e: sector explorado\n");
        sb.append("N: nave\n");
        sb.append("c: construcción en curso\n");
        sb.append("M: mina\n");
        sb.append("G: granja\n");
        sb.append("B: otro edificio\n");

        return sb.toString();
    }

    public List<MapSector> getMapaVisible(Long usuarioId) {

        List<SectorExploration> explorados = sectorExplorationRepository
                .findByUsuarioIdAndVisibleTrue(usuarioId);

        return explorados.stream()
                .map(SectorExploration::getSector)
                .toList();
    }
    public List<MapSectorDTO> getMapaJugador(Long usuarioId) {

        List<SectorExploration> explorados =
                sectorExplorationRepository.findByUsuarioId(usuarioId);

        return explorados.stream().map(exp -> {

            MapSector s = exp.getSector();

            return new MapSectorDTO(
                    s.getX(),
                    s.getY(),
                    exp.isVisible(),
                    true,
                    s.getTerrain().name(),
                    s.getResource().name(),
                    s.getRichness(),
                    s.getBuilding() != null ? s.getBuilding().name() : null,
                    s.getBuildingLevel(),
                    s.getOwner() != null ? s.getOwner().getId() : null
            );

        }).toList();
    }
    private MapSector generarSector(int x, int y) {

        TerrainType terrain = generarTerreno();

        SectorResource resource = generarRecurso(terrain);

        double richness = generarRiqueza();

        MapSector sector = new MapSector(x, y, terrain, resource, richness);

        return mapSectorRepository.save(sector);
    }

    private TerrainType generarTerreno() {

        TerrainType[] terrains = TerrainType.values();

        return terrains[random.nextInt(terrains.length)];
    }

    private SectorResource generarRecurso(TerrainType terrain) {

        return switch (terrain) {

            case MONTAÑA -> randomMetal();

            case LLANURA -> randomFood();

            case BOSQUE -> randomFood();

            case DESIERTO -> SectorResource.NONE;

            case PANTANO -> randomChem();
        };
    }

    private SectorResource randomMetal() {

        SectorResource[] metals = {
                SectorResource.NEOCROMO,
                SectorResource.UMBRIUM,
                SectorResource.SYNTHERIUM,
                SectorResource.HEXALIUM,
                SectorResource.VOIDIUM
        };

        return metals[random.nextInt(metals.length)];
    }

    private SectorResource randomFood() {

        SectorResource[] foods = {
                SectorResource.KROMAFRUTA,
                SectorResource.NEUROTRIGO,
                SectorResource.ALGACARNE,
                SectorResource.RATAX,
                SectorResource.FLORSOMNIO
        };

        return foods[random.nextInt(foods.length)];
    }

    private SectorResource randomChem() {

        SectorResource[] chems = {
                SectorResource.REFLEXA,
                SectorResource.NANOCURA,
                SectorResource.SOMNEX
        };

        return chems[random.nextInt(chems.length)];
    }

    private double generarRiqueza() {

        return 0.5 + random.nextDouble() * 1.5;
    }
}