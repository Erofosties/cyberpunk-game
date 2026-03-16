package com.cyberpunk.service;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;

@Service
public class SectorBuildService {

    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository sectorRepository;

    public SectorBuildService(
            ColoniaRepository coloniaRepository,
            MapSectorRepository sectorRepository) {

        this.coloniaRepository = coloniaRepository;
        this.sectorRepository = sectorRepository;
    }

    public void construirEnSector(Long coloniaId, int x, int y, String tipoEdificio) {

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        MapSector sector = sectorRepository.findByXAndY(x, y)
                .orElseThrow(() -> new RuntimeException("Sector no existe"));
        if (sector.getOwner() == null ||
        	    !sector.getOwner().getId().equals(colonia.getUsuario().getId())) {

        	    throw new RuntimeException("No puedes construir en un sector que no es tuyo");
        	}
        if (sector.getBuilding() != null) {
            throw new RuntimeException("El sector ya tiene un edificio");
        }

        TipoEdificio tipo;

        try {
            tipo = TipoEdificio.valueOf(tipoEdificio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de edificio inválido");
        }

        ConstruccionEnCurso construccion = new ConstruccionEnCurso(tipo.name(), sector);

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }
}