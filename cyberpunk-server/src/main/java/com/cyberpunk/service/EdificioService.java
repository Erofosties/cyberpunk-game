package com.cyberpunk.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.CosteEdificioCalculator;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.ConstruccionRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Service
public class EdificioService {

    private final ColoniaRepository coloniaRepository;
    private final ConstruccionRepository construccionRepository;
    private final PersonajeRepository personajeRepository;
    private final MapSectorRepository mapSectorRepository;

    public EdificioService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository,
            ConstruccionRepository construccionRepository,
            MapSectorRepository mapSectorRepository) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
        this.construccionRepository = construccionRepository;
        this.mapSectorRepository = mapSectorRepository;
    }

    // ================= CONSTRUIR =================

    public void construirEdificio(Long coloniaId, String tipoEdificio, Long sectorId) {

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        MapSector sector = mapSectorRepository.findById(sectorId)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado"));

        if (sector.getBuilding() != null) {
            throw new RuntimeException("El sector ya tiene un edificio");
        }

        TipoEdificio tipo;

        try {
            tipo = TipoEdificio.valueOf(tipoEdificio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de edificio inválido");
        }

        Recursos recursos = colonia.getRecursos();

        Map<ResourceType, Integer> coste =
                CosteEdificioCalculator.calcularCoste(tipo, 0);

        if (!recursos.tieneSuficiente(coste)) {
            throw new RuntimeException("Recursos insuficientes");
        }

        recursos.consumir(coste);

        ConstruccionEnCurso construccion =
                new ConstruccionEnCurso(tipo.name(), sector);

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }

    // ================= ASIGNAR CONSTRUCCIÓN =================

    public void asignarTrabajadorConstruccion(Long personajeId, Long construccionId) {

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        ConstruccionEnCurso construccion = construccionRepository.findById(construccionId)
                .orElseThrow(() -> new RuntimeException("Construccion no encontrada"));

        personaje.setSectorAsignado(null);
        personaje.setConstruccionAsignada(construccion);

        personajeRepository.save(personaje);
    }

    // ================= ASIGNAR SECTOR =================

    public void asignarTrabajadorSector(Long personajeId, Long sectorId) {

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        MapSector sector = mapSectorRepository.findById(sectorId)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado"));

        if (sector.getBuilding() == null) {
            throw new RuntimeException("El sector no tiene edificio");
        }

        personaje.setConstruccionAsignada(null);
        personaje.setSectorAsignado(sector);

        personajeRepository.save(personaje);
    }
}