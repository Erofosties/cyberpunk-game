package com.cyberpunk.service;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.tarea.Tarea;
import com.cyberpunk.dto.AsignarTareaRequest;
import com.cyberpunk.repository.ConstruccionRepository;
import com.cyberpunk.repository.EdificioRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Service
public class TareaService {

    private final PersonajeRepository personajeRepository;
    private final EdificioRepository edificioRepository;
    private final ConstruccionRepository construccionRepository;

    public TareaService(
            PersonajeRepository personajeRepository,
            EdificioRepository edificioRepository,
            ConstruccionRepository construccionRepository) {

        this.personajeRepository = personajeRepository;
        this.edificioRepository = edificioRepository;
        this.construccionRepository = construccionRepository;
    }

    public void asignarTarea(AsignarTareaRequest request) {

        Personaje personaje = personajeRepository
                .findById(request.getPersonajeId())
                .orElseThrow(() -> new RuntimeException("Personaje no encontrado"));

        Tarea tarea;

        Tarea.TipoTarea tipo = Tarea.TipoTarea.valueOf(request.getTipo());

        switch (tipo) {

            case CONSTRUCCION -> {

                if (request.getConstruccionId() == null)
                    throw new RuntimeException("ConstruccionId requerido");

                ConstruccionEnCurso construccion = construccionRepository
                        .findById(request.getConstruccionId())
                        .orElseThrow(() -> new RuntimeException("Construcción no encontrada"));

                tarea = new Tarea(tipo, construccion);
            }

            case PRODUCCION -> {

                if (request.getEdificioId() == null)
                    throw new RuntimeException("EdificioId requerido");

                Edificio edificio = edificioRepository
                        .findById(request.getEdificioId())
                        .orElseThrow(() -> new RuntimeException("Edificio no encontrado"));

                tarea = new Tarea(tipo, edificio);
            }

            default -> {
                throw new RuntimeException("Tipo de tarea no soportado");
            }
        }

        personaje.setTareaActual(tarea);

        personajeRepository.save(personaje);
    }
}