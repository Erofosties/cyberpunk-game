package com.cyberpunk.service;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.repository.SectorExplorationRepository;

@Service
public class ExplorationService {

    private final SectorExplorationRepository explorationRepository;
    private final MapService mapService;

    public ExplorationService(
            SectorExplorationRepository explorationRepository,
            MapService mapService) {

        this.explorationRepository = explorationRepository;
        this.mapService = mapService;
    }

    // ================= EXPLORAR SECTOR =================

    public MapSector explorarSector(Usuario usuario, int x, int y) {

        MapSector sector = mapService.getOrGenerateSector(x, y);

        explorationRepository
                .findByUsuarioAndSector(usuario, sector)
                .orElseGet(() -> {

                    SectorExploration exploration =
                            new SectorExploration(usuario, sector);

                    return explorationRepository.save(exploration);
                });

        return sector;
    }

    // ================= REVELAR ALREDEDOR DE GUERRERO =================

    public void revelarAlrededor(Guerrero guerrero) {

        if (guerrero.getSectorAsignado() == null)
            return;

        MapSector base = guerrero.getSectorAsignado();

        int baseX = base.getX();
        int baseY = base.getY();

        int vision = guerrero.getVision();

        Usuario usuario = guerrero.getColonia().getUsuario();

        for (int dx = -vision; dx <= vision; dx++) {

            for (int dy = -vision; dy <= vision; dy++) {

                int x = baseX + dx;
                int y = baseY + dy;

                MapSector sector = mapService.getOrGenerateSector(x, y);

                boolean explored =
                        explorationRepository.existsByUsuarioAndSector(usuario, sector);

                if (!explored) {

                    SectorExploration exploration =
                            new SectorExploration(usuario, sector);

                    explorationRepository.save(exploration);
                }
            }
        }
    }

    // ================= REVELAR AL DESPLEGAR NAVE =================

    public void revelarSectoresIniciales(Colonia colonia) {

        MapSector nave = colonia.getSectorNave();

        int baseX = nave.getX();
        int baseY = nave.getY();

        Usuario usuario = colonia.getUsuario();

        for (int dx = -1; dx <= 1; dx++) {

            for (int dy = -1; dy <= 1; dy++) {

                int x = baseX + dx;
                int y = baseY + dy;

                MapSector sector = mapService.getOrGenerateSector(x, y);

                boolean explored =
                        explorationRepository.existsByUsuarioAndSector(usuario, sector);

                if (!explored) {

                    SectorExploration exploration =
                            new SectorExploration(usuario, sector);

                    explorationRepository.save(exploration);
                }
            }
        }
    }
}