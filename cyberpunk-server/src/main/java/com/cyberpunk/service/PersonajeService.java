package com.cyberpunk.service;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.dto.CrearPersonajeRequest;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Service
public class PersonajeService {

    private final ColoniaRepository coloniaRepository;
    private final PersonajeRepository personajeRepository;

    public PersonajeService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
    }

    public Personaje crearPersonaje(CrearPersonajeRequest r) {

        Colonia colonia = coloniaRepository
                .findById(r.getColoniaId())
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        Personaje personaje;

        if ("TRABAJADOR".equalsIgnoreCase(r.getTipo())) {

            int total =
                    r.getMineria() +
                    r.getAgricultura() +
                    r.getCiencia() +
                    r.getIngenieria();

            if (total != 6) {
                throw new RuntimeException("Debes repartir exactamente 6 puntos");
            }

            personaje = new Trabajador(
                    r.getNombre(),
                    r.getMineria(),
                    r.getAgricultura(),
                    r.getCiencia(),
                    r.getIngenieria()
            );

        } else if ("GUERRERO".equalsIgnoreCase(r.getTipo())) {

            int total =
                    r.getFuerza() +
                    r.getDestreza() +
                    r.getResistencia() +
                    r.getHackeo();

            if (total != 6) {
                throw new RuntimeException("Debes repartir exactamente 6 puntos");
            }

            personaje = new Guerrero(
                    r.getNombre(),
                    r.getFuerza(),
                    r.getDestreza(),
                    r.getResistencia(),
                    r.getHackeo()
            );

        } else {
            throw new RuntimeException("Tipo de personaje no válido");
        }

        colonia.addPersonaje(personaje);
        coloniaRepository.save(colonia);

        return personaje;
    }

    /* PERSONAJES INICIALES */

    public void crearTrabajadorInicial(Colonia colonia, String nombre) {

        Trabajador trabajador = new Trabajador(nombre, 2, 2, 1, 1);

        trabajador.setColonia(colonia);

        personajeRepository.save(trabajador);
    }

    public void crearGuerreroInicial(Colonia colonia, String nombre) {

        Guerrero guerrero = new Guerrero(nombre, 2, 2, 1, 1);

        guerrero.setColonia(colonia);

        personajeRepository.save(guerrero);
    }
}