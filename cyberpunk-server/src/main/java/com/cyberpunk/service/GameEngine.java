package com.cyberpunk.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.repository.ColoniaRepository;

@Component
public class GameEngine {

    private final ColoniaRepository coloniaRepository;

    public GameEngine(ColoniaRepository coloniaRepository) {
        this.coloniaRepository = coloniaRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void tick() {

        var colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarConstrucciones(colonia);

            procesarProduccion(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick del juego ejecutado");
    }

    private void procesarConstrucciones(Colonia colonia) {

    	for (ConstruccionEnCurso construccion : new java.util.ArrayList<>(colonia.getColaConstruccion())) {

            int progreso = 0;

            for (Personaje personaje : colonia.getPoblacion()) {

                if (personaje instanceof Trabajador trabajador) {

                    progreso += trabajador.getIngenieria();
                    trabajador.aumentarCansancio(1);
                }
            }

            construccion.avanzarConstruccion(progreso);

            System.out.println("Progreso construccion: " + construccion.getProgreso());

            if (construccion.completada()) {

                Edificio edificio = new Edificio(
                        Edificio.TipoEdificio.valueOf(construccion.getTipo())
                );

                colonia.addEdificio(edificio);

                colonia.getColaConstruccion().remove(construccion);

                break;
            }
        }
    }

    private void procesarProduccion(Colonia colonia) {

        double factorEnergia = colonia.calcularFactorEnergia();

        for (Edificio edificio : colonia.getEdificios()) {

            for (Personaje personaje : colonia.getPoblacion()) {

                if (personaje instanceof Trabajador trabajador) {

                    int habilidad = trabajador.getProduccionParaEdificio(edificio);

                    int produccion = edificio.producir(habilidad, factorEnergia);

                    if (edificio.getRecursoProduce() != null) {

                        colonia.getRecursos().add(
                                edificio.getRecursoProduce(),
                                produccion
                        );
                    }

                    trabajador.aumentarCansancio(1);
                }
            }
        }
    }
}