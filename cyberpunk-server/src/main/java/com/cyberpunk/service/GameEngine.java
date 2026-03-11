package com.cyberpunk.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.EdificioRepository;

@Component
public class GameEngine {

    private final ColoniaRepository coloniaRepository;
    private final EdificioRepository edificioRepository;

    public GameEngine(
            ColoniaRepository coloniaRepository,
            EdificioRepository edificioRepository) {

        this.coloniaRepository = coloniaRepository;
        this.edificioRepository = edificioRepository;
    }

    // ================= TICK =================

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void tick() {

        var colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarEnergia(colonia);

            procesarConstrucciones(colonia);

            procesarProduccion(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick del juego ejecutado");
    }

    // ================= ENERGÍA =================

    private void procesarEnergia(Colonia colonia) {

        int energiaProducida = 0;

        for (Edificio edificio : colonia.getEdificios()) {

            switch (edificio.getTipo()) {

                // PRODUCEN SOLOS
                case PLACA_SOLAR, GENERADOR_NEON -> {

                    energiaProducida += edificio.getProduccionEnergia(0);
                }

                // REQUIERE TRABAJADORES
                case REACTOR_FUSION -> {

                    int trabajadoresIngenieria = 0;

                    for (Personaje personaje : colonia.getPoblacion()) {

                        if (personaje instanceof Trabajador trabajador
                                && trabajador.getEdificioAsignado() != null
                                && trabajador.getEdificioAsignado().getId().equals(edificio.getId())
                                && trabajador.getCansancio() < 100) {

                            trabajadoresIngenieria += trabajador.getIngenieria();
                        }
                    }

                    energiaProducida += edificio.getProduccionEnergia(trabajadoresIngenieria);
                }

                default -> {}
            }
        }

        int energiaActual = colonia.getRecursos().getCantidad(ResourceType.ENERGIA);

        int capacidad = colonia.calcularCapacidadEnergia();

        int nuevaEnergia = energiaActual + energiaProducida;

        if (nuevaEnergia > capacidad) {
            nuevaEnergia = capacidad;
        }

        colonia.getRecursos().setEnergia(nuevaEnergia);
    }

    // ================= CONSTRUCCIONES =================

    private void procesarConstrucciones(Colonia colonia) {

        List<ConstruccionEnCurso> terminadas = new ArrayList<>();

        for (ConstruccionEnCurso construccion : new ArrayList<>(colonia.getColaConstruccion())) {

            int progreso = 0;

            for (Personaje personaje : colonia.getPoblacion()) {

                if (personaje instanceof Trabajador trabajador
                        && trabajador.getConstruccionAsignada() != null
                        && trabajador.getConstruccionAsignada().getId().equals(construccion.getId())
                        && trabajador.getCansancio() < 100) {

                    progreso += trabajador.getIngenieria();

                    trabajador.aumentarCansancio(2);

                    if (trabajador.getCansancio() >= 100) {
                        trabajador.setConstruccionAsignada(null);
                    }
                }
            }

            construccion.avanzarConstruccion(progreso);

            System.out.println("Progreso construccion: " + construccion.getProgreso());

            if (construccion.completada()) {

                Edificio edificio = new Edificio(
                        Edificio.TipoEdificio.valueOf(construccion.getTipo())
                );

                edificio.setColonia(colonia);

                edificioRepository.save(edificio);

                colonia.getEdificios().add(edificio);

                // liberar trabajadores
                for (Personaje personaje : colonia.getPoblacion()) {

                    if (personaje.getConstruccionAsignada() != null
                            && personaje.getConstruccionAsignada().getId().equals(construccion.getId())) {

                        personaje.setConstruccionAsignada(null);
                    }
                }

                terminadas.add(construccion);
            }
        }

        colonia.getColaConstruccion().removeAll(terminadas);
    }

    // ================= PRODUCCIÓN =================

    private void procesarProduccion(Colonia colonia) {

        for (Edificio edificio : colonia.getEdificios()) {

            for (Personaje personaje : colonia.getPoblacion()) {

                if (personaje instanceof Trabajador trabajador
                        && trabajador.getEdificioAsignado() != null
                        && trabajador.getEdificioAsignado().getId().equals(edificio.getId())
                        && trabajador.getCansancio() < 100) {

                    int consumoEnergia = edificio.getConsumoEnergia();

                    int energiaDisponible = colonia.getRecursos().getCantidad(ResourceType.ENERGIA);

                    if (energiaDisponible < consumoEnergia) {
                        continue;
                    }

                    colonia.getRecursos().consumir(ResourceType.ENERGIA, consumoEnergia);

                    int habilidad = trabajador.getProduccionParaEdificio(edificio);

                    int produccion = edificio.producir(habilidad, 1);

                    if (edificio.getRecursoProduce() != null) {

                        colonia.getRecursos().add(
                                edificio.getRecursoProduce(),
                                produccion
                        );
                    }

                    trabajador.aumentarCansancio(1);

                    if (trabajador.getCansancio() >= 100) {
                        trabajador.setEdificioAsignado(null);
                    }
                }
            }
        }
    }
}