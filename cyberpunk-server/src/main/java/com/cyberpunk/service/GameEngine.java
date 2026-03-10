package com.cyberpunk.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.tarea.Tarea;
import com.cyberpunk.repository.ColoniaRepository;

@Service
public class GameEngine {

    private final ColoniaRepository coloniaRepository;

    public GameEngine(ColoniaRepository coloniaRepository) {
        this.coloniaRepository = coloniaRepository;
    }

    // ================= TICK DEL JUEGO =================

    @Scheduled(fixedRate = 10000)
    public void actualizarJuego() {

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarTareas(colonia);

            procesarDescanso(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick del juego ejecutado");
    }

    // ================= PROCESAR TAREAS =================

    private void procesarTareas(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            Tarea tarea = personaje.getTareaActual();

            if (tarea == null) {
                continue;
            }

            if (personaje.getCansancio() >= 100) {
                continue;
            }

            switch (tarea.getTipo()) {

                case CONSTRUCCION -> procesarConstruccion(colonia, personaje, tarea);

                case PRODUCCION -> procesarProduccion(colonia, personaje, tarea);

                case EXPLORACION -> procesarExploracion(personaje, tarea);

                case DEFENSA -> procesarDefensa(personaje);

                case DESCANSO -> personaje.reducirCansancio(5);
            }
        }
    }

    // ================= CONSTRUCCIÓN =================

    private void procesarConstruccion(Colonia colonia, Personaje personaje, Tarea tarea) {

        if (!(personaje instanceof Trabajador trabajador)) {
            return;
        }

        int progreso = trabajador.getIngenieria();

        tarea.avanzar(progreso);

        trabajador.aumentarCansancio(2);

        if (tarea.completada()) {

            Edificio edificio = tarea.getEdificio();

            if (!colonia.getEdificios().contains(edificio)) {
                colonia.addEdificio(edificio);
            }

            personaje.setTareaActual(null);
        }
    }

    // ================= PRODUCCIÓN =================

    private void procesarProduccion(Colonia colonia, Personaje personaje, Tarea tarea) {

        if (!(personaje instanceof Trabajador trabajador)) {
            return;
        }

        Edificio edificio = tarea.getEdificio();

        if (edificio == null) {
            return;
        }

        double factorEnergia = colonia.calcularFactorEnergia();

        int produccionTrabajador = trabajador.getProduccion();

        int produccion = edificio.producir(produccionTrabajador, factorEnergia);

        Recursos recursos = colonia.getRecursos();

        if (edificio.getRecursoProduce() != null) {
            recursos.add(edificio.getRecursoProduce(), produccion);
        }

        trabajador.aumentarCansancio(1);
    }

    // ================= EXPLORACIÓN =================

    private void procesarExploracion(Personaje personaje, Tarea tarea) {

        int progreso = 5;

        tarea.avanzar(progreso);

        personaje.aumentarCansancio(1);

        if (tarea.completada()) {

            personaje.setTareaActual(null);
        }
    }

    // ================= DEFENSA =================

    private void procesarDefensa(Personaje personaje) {

        personaje.aumentarCansancio(1);
    }

    // ================= DESCANSO =================

    private void procesarDescanso(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.getTareaActual() == null) {

                personaje.reducirCansancio(2);
            }
        }
    }
}