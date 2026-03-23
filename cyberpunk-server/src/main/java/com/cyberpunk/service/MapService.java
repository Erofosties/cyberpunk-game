package com.cyberpunk.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.cyberpunk.dto.MapSectorDTO;
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