package com.cyberpunk.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.CosteEdificioCalculator;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.ConstruccionRepository;
import com.cyberpunk.repository.EdificioRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Service
public class EdificioService {

    private final ColoniaRepository coloniaRepository;
    private final ConstruccionRepository construccionRepository;
    private final EdificioRepository edificioRepository;
    private final PersonajeRepository personajeRepository;

    public EdificioService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository,
            ConstruccionRepository construccionRepository,
            EdificioRepository edificioRepository) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
        this.construccionRepository = construccionRepository;
        this.edificioRepository = edificioRepository;
    }

    public void construirEdificio(Long coloniaId, String tipoEdificio) {

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

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

        ConstruccionEnCurso construccion = new ConstruccionEnCurso(tipo.name());

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }

    // ================= ASIGNAR CONSTRUCCIÓN =================

    public void asignarTrabajadorConstruccion(Long personajeId, Long construccionId) {

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        ConstruccionEnCurso construccion = construccionRepository.findById(construccionId)
                .orElseThrow(() -> new RuntimeException("Construccion no encontrada"));

        // liberar trabajo anterior
        personaje.setEdificioAsignado(null);
        personaje.setConstruccionAsignada(null);

        personaje.setConstruccionAsignada(construccion);

        personajeRepository.save(personaje);
    }

    // ================= ASIGNAR EDIFICIO =================

    public void asignarTrabajadorEdificio(Long personajeId, Long edificioId) {

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        Edificio edificio = edificioRepository.findById(edificioId)
                .orElseThrow(() -> new RuntimeException("Edificio no encontrado"));

        // ❌ BLOQUEAR EDIFICIOS QUE NO ACEPTAN TRABAJADORES
        if (edificio.getTipo() == TipoEdificio.PLACA_SOLAR
                || edificio.getTipo() == TipoEdificio.GENERADOR_NEON
                || edificio.getTipo() == TipoEdificio.BATERIA_ENERGIA) {

            throw new RuntimeException("No se pueden asignar trabajadores a este edificio");
        }

        personaje.setEdificioAsignado(edificio);

        personajeRepository.save(personaje);
    }
}