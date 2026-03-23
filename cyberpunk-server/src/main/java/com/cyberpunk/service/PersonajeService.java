package com.cyberpunk.service;

import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.dto.CrearGuerreroRequest;
import com.cyberpunk.dto.CrearPersonajeRequest;
import com.cyberpunk.dto.CrearTrabajadorRequest;
import com.cyberpunk.exception.EntityNotFoundException;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.gameBalance.GameBalance;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.PersonajeRepository;
import com.cyberpunk.util.SkillValidator;

@Service
public class PersonajeService {

    private final ColoniaRepository coloniaRepository;
    private final PersonajeRepository personajeRepository;
    private final Random random = new Random();

    public PersonajeService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
    }

    public Personaje crearPersonaje(CrearPersonajeRequest r) {
        if (r == null) {
            throw new IllegalArgumentException("La solicitud no puede ser null");
        }
        if (r.getColoniaId() == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        boolean isTrabajador = random.nextBoolean();
        int[] skills = generateRandomSkills();

        if (isTrabajador) {
            CrearTrabajadorRequest req = new CrearTrabajadorRequest();
            req.setColoniaId(r.getColoniaId());
            req.setNombre(r.getNombre());
            req.setMineria(skills[0]);
            req.setAgricultura(skills[1]);
            req.setCiencia(skills[2]);
            req.setIngenieria(skills[3]);

            return crearTrabajador(req);
        } else {
            CrearGuerreroRequest req = new CrearGuerreroRequest();
            req.setColoniaId(r.getColoniaId());
            req.setNombre(r.getNombre());
            req.setFuerza(skills[0]);
            req.setDestreza(skills[1]);
            req.setResistencia(skills[2]);
            req.setHackeo(skills[3]);

            return crearGuerrero(req);
        }
    }

    @SuppressWarnings("null")
    public Personaje crearTrabajador(CrearTrabajadorRequest r) {
        if (r == null) {
            throw new IllegalArgumentException("La solicitud no puede ser null");
        }
        if (r.getColoniaId() == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        Colonia colonia = coloniaRepository
                .findById(r.getColoniaId())
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));

        SkillValidator.validateSkillDistribution(r.getMineria(), r.getAgricultura(), r.getCiencia(), r.getIngenieria());

        Trabajador trabajador = new Trabajador(
                r.getNombre(),
                r.getMineria(),
                r.getAgricultura(),
                r.getCiencia(),
                r.getIngenieria()
        );

        colonia.addPersonaje(trabajador);
        coloniaRepository.save(colonia);

        return trabajador;
    }

    @SuppressWarnings("null")
    public Personaje crearGuerrero(CrearGuerreroRequest r) {
        if (r == null) {
            throw new IllegalArgumentException("La solicitud no puede ser null");
        }
        if (r.getColoniaId() == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        Colonia colonia = coloniaRepository
                .findById(r.getColoniaId())
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));

        SkillValidator.validateSkillDistribution(r.getFuerza(), r.getDestreza(), r.getResistencia(), r.getHackeo());

        Guerrero guerrero = new Guerrero(
                r.getNombre(),
                r.getFuerza(),
                r.getDestreza(),
                r.getResistencia(),
                r.getHackeo()
        );

        colonia.addPersonaje(guerrero);
        coloniaRepository.save(colonia);

        return guerrero;
    }

    private int[] generateRandomSkills() {
        int[] skills = new int[4];
        int remaining = 6;
        for (int i = 0; i < 3; i++) {
            int assign = random.nextInt(remaining + 1);
            skills[i] = assign;
            remaining -= assign;
        }
        skills[3] = remaining;
        // Shuffle para mayor aleatoriedad
        for (int i = 3; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = skills[i];
            skills[i] = skills[j];
            skills[j] = temp;
        }
        return skills;
    }

    // =========================
    // PERSONAJES INICIALES
    // =========================

    public void crearTrabajadorInicial(Colonia colonia, String nombre) {

        Trabajador trabajador = new Trabajador(nombre, 2, 2, 1, 1);

        colonia.addPersonaje(trabajador);

        coloniaRepository.save(colonia);
    }

    public void crearGuerreroInicial(Colonia colonia, String nombre) {

        Guerrero guerrero = new Guerrero(nombre, 2, 2, 1, 1);

        colonia.addPersonaje(guerrero);

        coloniaRepository.save(colonia);
    }

    @Transactional
    public Personaje usarNanocura(Long coloniaId, Long personajeId) {
        Colonia colonia = obtenerColonia(coloniaId);
        Personaje personaje = obtenerPersonaje(colonia, personajeId);

        if (!personaje.estaHerido()) {
            throw new GameRuleViolationException("El personaje no está herido");
        }

        if (colonia.getRecursos().getCantidad(ResourceType.NANOCURA) <= 0) {
            throw new GameRuleViolationException("No quedan nanocuras en la colonia");
        }

        colonia.getRecursos().consumir(ResourceType.NANOCURA, 1);
        personaje.curar(personaje.getVidaMaxima());

        personajeRepository.save(personaje);
        coloniaRepository.save(colonia);
        return personaje;
    }

    @Transactional
    public Personaje usarFlorsomnio(Long coloniaId, Long personajeId) {
        Colonia colonia = obtenerColonia(coloniaId);
        Personaje personaje = obtenerPersonaje(colonia, personajeId);

        if (!personaje.estaHerido()) {
            throw new GameRuleViolationException("El personaje no está herido");
        }

        if (personaje.getComida() <= 60) {
            throw new GameRuleViolationException("El personaje necesita comida superior a 60 para curarse durmiendo");
        }

        if (colonia.getRecursos().getCantidad(ResourceType.FLORSOMNIO) <= 0) {
            throw new GameRuleViolationException("No queda florsomnio disponible");
        }

        colonia.getRecursos().consumir(ResourceType.FLORSOMNIO, 1);
        personaje.forzarReposo();
        personaje.reducirCansancio(GameBalance.DESCANSO_EXTRA_FLORSOMNIO);
        personaje.curar(GameBalance.CURACION_FLORSOMNIO);

        personajeRepository.save(personaje);
        coloniaRepository.save(colonia);
        return personaje;
    }

    private Colonia obtenerColonia(Long coloniaId) {
        if (coloniaId == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }

        return coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));
    }

    private Personaje obtenerPersonaje(Colonia colonia, Long personajeId) {
        return colonia.getPoblacion().stream()
                .filter(personaje -> personaje.getId().equals(personajeId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Personaje no encontrado en la colonia"));
    }
}