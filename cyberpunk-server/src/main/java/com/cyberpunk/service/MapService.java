package com.cyberpunk.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.MapSector.SectorResource;
import com.cyberpunk.domain.map.MapSector.TerrainType;
import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.SectorExplorationRepository;

@Service
public class MapService {

    private final MapSectorRepository mapSectorRepository;
    private final SectorExplorationRepository sectorExplorationRepository;

    private final Random random = new Random();

    public MapService(
            MapSectorRepository mapSectorRepository,
            SectorExplorationRepository sectorExplorationRepository) {

        this.mapSectorRepository = mapSectorRepository;
        this.sectorExplorationRepository = sectorExplorationRepository;
    }

    public MapSector getOrGenerateSector(int x, int y) {

        return mapSectorRepository
                .findByXAndY(x, y)
                .orElseGet(() -> generarSector(x, y));
    }
    public List<MapSector> getMapaVisible(Long usuarioId) {

        List<SectorExploration> explorados = sectorExplorationRepository
                .findByUsuarioIdAndVisibleTrue(usuarioId);

        return explorados.stream()
                .map(SectorExploration::getSector)
                .toList();
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