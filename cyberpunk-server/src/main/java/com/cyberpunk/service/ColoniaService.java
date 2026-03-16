package com.cyberpunk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;

@Service
public class ColoniaService {

    private static final int DISTANCIA_MINIMA = 5;

    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository mapSectorRepository;
    private final ExplorationService explorationService;

    public ColoniaService(
            ColoniaRepository coloniaRepository,
            MapSectorRepository mapSectorRepository,
            ExplorationService explorationService) {

        this.coloniaRepository = coloniaRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.explorationService = explorationService;
    }

    @Transactional
    public void desplegarNave(Long coloniaId, int x, int y) {

        Colonia colonia = coloniaRepository
                .findById(coloniaId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        if (colonia.getSectorNave() != null) {
            throw new RuntimeException("La nave ya está desplegada");
        }

        MapSector sector = mapSectorRepository
                .findByXAndY(x, y)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado"));

        if (sector.getOwner() != null) {
            throw new RuntimeException("Sector ocupado por otro jugador");
        }

        // comprobar distancia con otras colonias
        comprobarDistanciaColonias(x, y);

        // asignar propietario
        sector.setOwner(colonia.getUsuario());

        // asignar sector nave
        colonia.setSectorNave(sector);

        mapSectorRepository.save(sector);
        coloniaRepository.save(colonia);

        // revelar sectores iniciales
        explorationService.revelarSectoresIniciales(colonia);
    }

    private void comprobarDistanciaColonias(int x, int y) {

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia otra : colonias) {

            if (otra.getSectorNave() == null)
                continue;

            int dx = Math.abs(otra.getSectorNave().getX() - x);
            int dy = Math.abs(otra.getSectorNave().getY() - y);

            int distancia = Math.max(dx, dy);

            if (distancia < DISTANCIA_MINIMA) {
                throw new RuntimeException("Demasiado cerca de otra colonia");
            }
        }
    }

    public Colonia obtenerColonia(Long id) {

        return coloniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));
    }
}