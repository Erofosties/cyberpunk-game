package com.cyberpunk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.exception.EntityNotFoundException;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.gameBalance.GameBalance;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.SectorExplorationRepository;

@Service
public class ColoniaService {

    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository mapSectorRepository;
    private final SectorExplorationRepository sectorExplorationRepository;
    private final ExplorationService explorationService;

    public ColoniaService(
            ColoniaRepository coloniaRepository,
            MapSectorRepository mapSectorRepository,
            SectorExplorationRepository sectorExplorationRepository,
            ExplorationService explorationService) {

        this.coloniaRepository = coloniaRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.sectorExplorationRepository = sectorExplorationRepository;
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

        MapSector sector = mapSectorRepository
                .findByXAndY(x, y)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado"));

        if (colonia.getSectorNave() == null) {
            desplegarNaveInicial(colonia, sector);
            return;
        }

        redesplegarNave(colonia, sector);
    }

    private void desplegarNaveInicial(Colonia colonia, MapSector sectorDestino) {
        if (sectorDestino.getOwner() != null) {
            throw new GameRuleViolationException("Sector ocupado por otro jugador");
        }

        comprobarDistanciaColonias(sectorDestino.getX(), sectorDestino.getY());

        sectorDestino.setOwner(colonia.getUsuario());
        colonia.setSectorNave(sectorDestino);

        mapSectorRepository.save(sectorDestino);
        coloniaRepository.save(colonia);

        explorationService.revelarSectoresIniciales(colonia);
    }

    private void redesplegarNave(Colonia colonia, MapSector sectorDestino) {
        MapSector sectorActualNave = colonia.getSectorNave();
        if (sectorActualNave == null) {
            throw new GameRuleViolationException("La colonia no tiene nave desplegada");
        }

        if (sectorActualNave.getId().equals(sectorDestino.getId())) {
            throw new GameRuleViolationException("La nave ya se encuentra en ese sector");
        }

        if (sectorDestino.getOwner() != null) {
            throw new GameRuleViolationException("Solo puedes redesplegar en un sector libre");
        }

        boolean explorado = sectorExplorationRepository.existsByUsuarioIdAndSectorId(
                colonia.getUsuario().getId(),
                sectorDestino.getId());
        if (!explorado) {
            throw new GameRuleViolationException("Solo puedes redesplegar a un sector explorado");
        }

        validarTripulacionEnNave(colonia, sectorActualNave);

        int distancia = Math.max(
                Math.abs(sectorDestino.getX() - sectorActualNave.getX()),
                Math.abs(sectorDestino.getY() - sectorActualNave.getY()));

        int costeEnergia = GameBalance.REDESPLIEGUE_COSTE_BASE
                + (distancia * GameBalance.REDESPLIEGUE_COSTE_POR_DISTANCIA);

        int energiaAcumulada = colonia.getRecursos().getEnergiaAcumulada();
        if (energiaAcumulada < costeEnergia) {
            throw new GameRuleViolationException("Energía acumulada insuficiente para redesplegar la nave");
        }

        colonia.getRecursos().setEnergiaAcumulada(energiaAcumulada - costeEnergia);

        sectorActualNave.setOwner(null);
        sectorDestino.setOwner(colonia.getUsuario());
        colonia.setSectorNave(sectorDestino);

        mapSectorRepository.save(sectorActualNave);
        mapSectorRepository.save(sectorDestino);
        coloniaRepository.save(colonia);

        explorationService.revelarSectoresIniciales(colonia);
    }

    private void validarTripulacionEnNave(Colonia colonia, MapSector sectorNave) {
        List<Personaje> poblacion = colonia.getPoblacion();
        for (Personaje personaje : poblacion) {
            if (personaje.estaEnViaje()) {
                throw new GameRuleViolationException("No puedes redesplegar con personajes en viaje");
            }

            if (personaje.getConstruccionAsignada() != null || personaje.getSectorAsignado() != null) {
                throw new GameRuleViolationException("No puedes redesplegar con personajes asignados fuera de la nave");
            }

            if (personaje instanceof Guerrero guerrero && guerrero.getMisionPendiente() != null) {
                throw new GameRuleViolationException("No puedes redesplegar con guerreros en misión");
            }

            MapSector sectorActual = personaje.getSectorActual();
            if (sectorActual == null || !sectorActual.getId().equals(sectorNave.getId())) {
                throw new GameRuleViolationException("Todos los personajes deben estar en la nave para redesplegar");
            }
        }
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