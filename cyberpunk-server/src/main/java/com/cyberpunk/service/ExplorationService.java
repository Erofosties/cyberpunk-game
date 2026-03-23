package com.cyberpunk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Verificar adyacencia si ya hay exploraciones visibles
        List<SectorExploration> exploracionesVisibles = explorationRepository
                .findByUsuarioIdAndVisibleTrue(usuario.getId());

        if (!exploracionesVisibles.isEmpty()) {
            boolean adyacente = exploracionesVisibles.stream()
                    .anyMatch(exp -> {
                        int dx = Math.abs(exp.getSector().getX() - x);
                        int dy = Math.abs(exp.getSector().getY() - y);
                        return Math.max(dx, dy) == 1;
                    });

            if (!adyacente) {
                throw new RuntimeException("Solo puedes explorar sectores adyacentes al mapa conocido");
            }
        }

        explorationRepository
                .findByUsuarioAndSector(usuario, sector)
                .ifPresentOrElse(
                        existing -> {
                            if (!existing.isVisible()) {
                                existing.setVisible(true);
                                explorationRepository.save(existing);
                            }
                        },
                        () -> explorationRepository.save(new SectorExploration(usuario, sector))
                );

        return sector;
    }
    @Transactional
    public void ocultarVisibilidad(Long usuarioId) {
        explorationRepository.ocultarTodosPorUsuario(usuarioId);
    }

    // ================= REVELAR ALREDEDOR DE GUERRERO =================

    public void revelarAlrededor(Guerrero guerrero) {

        if (guerrero.getSectorAsignado() == null)
            return;

        MapSector base = guerrero.getSectorAsignado();

        int vision = guerrero.getVision();

        Usuario usuario = guerrero.getColonia().getUsuario();

        revelarDesdeSector(usuario, base, vision);
    }

    public void revelarDesdeSector(Usuario usuario, MapSector base, int vision) {

        if (usuario == null || base == null)
            return;

        int baseX = base.getX();
        int baseY = base.getY();

        for (int dx = -vision; dx <= vision; dx++) {
            for (int dy = -vision; dy <= vision; dy++) {

                int x = baseX + dx;
                int y = baseY + dy;

                MapSector sector = mapService.getOrGenerateSector(x, y);

                explorationRepository
                    .findByUsuarioAndSector(usuario, sector)
                    .ifPresentOrElse(
                        existing -> {
                            if (!existing.isVisible()) {
                                existing.setVisible(true);
                                explorationRepository.save(existing);
                            }
                        },
                        () -> {
                            explorationRepository.save(
                                new SectorExploration(usuario, sector)
                            );
                        }
                    );
            }
        }
    }

    public void marcarSectorVisible(Usuario usuario, MapSector sector) {
        if (usuario == null || sector == null) {
            return;
        }

        explorationRepository
                .findByUsuarioAndSector(usuario, sector)
                .ifPresentOrElse(
                        existing -> {
                            if (!existing.isVisible()) {
                                existing.setVisible(true);
                            }
                            explorationRepository.save(existing);
                        },
                        () -> explorationRepository.save(new SectorExploration(usuario, sector))
                );
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

                SectorExploration exploration = explorationRepository
                        .findByUsuarioAndSector(usuario, sector)
                        .orElseGet(() -> new SectorExploration(usuario, sector));

                exploration.setVisible(true);
                explorationRepository.save(exploration);
            }
        }
    }
}