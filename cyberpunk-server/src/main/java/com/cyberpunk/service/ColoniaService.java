package com.cyberpunk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.exception.EntityNotFoundException;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.gameBalance.GameBalance;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;

@Service
public class ColoniaService {

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
        if (coloniaId == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        Colonia colonia = coloniaRepository
                .findById(coloniaId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));

        if (colonia.getSectorNave() != null) {
            throw new GameRuleViolationException("La nave ya está desplegada");
        }

        MapSector sector = mapSectorRepository
                .findByXAndY(x, y)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado"));

        if (sector.getOwner() != null) {
            throw new GameRuleViolationException("Sector ocupado por otro jugador");
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
    
    public boolean validarDistanciaNuevaColonia(int x, int y) {

        List<Colonia> colonias = coloniaRepository.findBySectorNaveIsNotNull();

        for (Colonia otra : colonias) {

            int dx = Math.abs(otra.getSectorNave().getX() - x);
            int dy = Math.abs(otra.getSectorNave().getY() - y);

            int distancia = Math.max(dx, dy);

            if (distancia < GameBalance.MINIMUM_COLONY_DISTANCE) {
                return false;
            }
        }

        return true;
    }

    private void comprobarDistanciaColonias(int x, int y) {

        if (!validarDistanciaNuevaColonia(x, y)) {
            throw new GameRuleViolationException("Demasiado cerca de otra colonia");
        }
    }

    public Colonia obtenerColonia(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        return coloniaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));
    }
}