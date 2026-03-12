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

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarEnergia(colonia);
            procesarConstrucciones(colonia);
            procesarProduccion(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick ejecutado: colonias procesadas = " + colonias.size());
    }

    // ================= ENERGÍA =================

    private void procesarEnergia(Colonia colonia) {

        int produccion = calcularProduccionEnergia(colonia);
        int consumo = calcularConsumoEnergia(colonia);

        var recursos = colonia.getRecursos();

        int energiaAcumulada = recursos.getEnergiaAcumulada();
        int capacidadBaterias = colonia.calcularCapacidadEnergia();

        int energiaDisponible = produccion - consumo;

        if (energiaDisponible >= 0) {

            energiaAcumulada += energiaDisponible;

            if (energiaAcumulada > capacidadBaterias)
                energiaAcumulada = capacidadBaterias;

        } else {

            int deficit = Math.abs(energiaDisponible);

            if (energiaAcumulada >= deficit) {

                energiaAcumulada -= deficit;

            } else {

                energiaAcumulada = 0;
            }
        }

        recursos.setEnergiaDisponible(Math.max(0, produccion - consumo));
        recursos.setEnergiaAcumulada(energiaAcumulada);
    }

    private int calcularProduccionEnergia(Colonia colonia) {

        int produccion = 0;

        for (Edificio edificio : colonia.getEdificios()) {

            switch (edificio.getTipo()) {

                case PLACA_SOLAR, GENERADOR_NEON ->
                        produccion += edificio.getProduccionEnergia(0);

                case REACTOR_FUSION ->
                        produccion += edificio.getProduccionEnergia(
                                calcularIngenieriaAsignada(colonia, edificio)
                        );

                default -> {}
            }
        }

        return produccion;
    }

    private int calcularConsumoEnergia(Colonia colonia) {

        int consumo = 0;

        for (Edificio edificio : colonia.getEdificios()) {

            if (edificioTieneTrabajadores(colonia, edificio)) {

                consumo += edificio.getConsumoEnergia();
            }
        }

        return consumo;
    }

    // ================= CONSTRUCCIONES =================

    private void procesarConstrucciones(Colonia colonia) {

        List<ConstruccionEnCurso> completadas = new ArrayList<>();

        for (ConstruccionEnCurso construccion : colonia.getColaConstruccion()) {

            int progreso = calcularProgresoConstruccion(colonia, construccion);

            if (progreso > 0) {
                construccion.avanzarConstruccion(progreso);
            }

            if (construccion.completada()) {

               crearEdificio(construccion, colonia);

                liberarTrabajadoresConstruccion(colonia, construccion);

                completadas.add(construccion);
            }
        }

        colonia.getColaConstruccion().removeAll(completadas);
    }

    private int calcularProgresoConstruccion(Colonia colonia, ConstruccionEnCurso construccion) {

        int progreso = 0;

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje instanceof Trabajador trabajador
                    && trabajador.getConstruccionAsignada() != null
                    && trabajador.getConstruccionAsignada().getId().equals(construccion.getId())
                    && trabajador.getCansancio() < 100) {

                progreso += trabajador.getIngenieria();

                trabajador.aumentarCansancio(2);
            }
        }

        return progreso;
    }

    private Edificio crearEdificio(ConstruccionEnCurso construccion, Colonia colonia) {

        Edificio edificio = new Edificio(
                Edificio.TipoEdificio.valueOf(construccion.getTipo())
        );

        edificio.setColonia(colonia);

        edificioRepository.save(edificio);

        colonia.getEdificios().add(edificio);

        return edificio;
    }

    // ================= PRODUCCIÓN =================

    private void procesarProduccion(Colonia colonia) {

        int energiaDisponible = colonia.getRecursos().getEnergiaDisponible();

        for (Edificio edificio : colonia.getEdificios()) {

            List<Trabajador> trabajadores = obtenerTrabajadoresEdificio(colonia, edificio);

            if (trabajadores.isEmpty())
                continue;

            int consumoEnergia = edificio.getConsumoEnergia();

            if (energiaDisponible < consumoEnergia)
                continue;

            energiaDisponible -= consumoEnergia;

            producirRecursos(colonia, edificio, trabajadores);

            aumentarCansancio(trabajadores);
        }

        colonia.getRecursos().setEnergiaDisponible(energiaDisponible);
    }

    private void producirRecursos(Colonia colonia, Edificio edificio, List<Trabajador> trabajadores) {

        int habilidadTotal = trabajadores
                .stream()
                .mapToInt(t -> t.getProduccionParaEdificio(edificio))
                .sum();

        int produccion = edificio.producir(habilidadTotal, 1);

        if (edificio.getRecursoProduce() != null) {

            colonia.getRecursos().add(
                    edificio.getRecursoProduce(),
                    produccion
            );
        }
    }

    // ================= UTILIDADES =================

    private List<Trabajador> obtenerTrabajadoresEdificio(Colonia colonia, Edificio edificio) {

        return colonia.getPoblacion()
                .stream()
                .filter(p -> p instanceof Trabajador)
                .map(p -> (Trabajador) p)
                .filter(t -> t.getEdificioAsignado() != null)
                .filter(t -> t.getEdificioAsignado().getId().equals(edificio.getId()))
                .filter(t -> t.getCansancio() < 100)
                .toList();
    }

    private int calcularIngenieriaAsignada(Colonia colonia, Edificio edificio) {

        return obtenerTrabajadoresEdificio(colonia, edificio)
                .stream()
                .mapToInt(Trabajador::getIngenieria)
                .sum();
    }

    private boolean edificioTieneTrabajadores(Colonia colonia, Edificio edificio) {

        return !obtenerTrabajadoresEdificio(colonia, edificio).isEmpty();
    }

    private void liberarTrabajadoresConstruccion(Colonia colonia, ConstruccionEnCurso construccion) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje instanceof Trabajador trabajador) {

                if (trabajador.getConstruccionAsignada() != null &&
                        trabajador.getConstruccionAsignada().getId().equals(construccion.getId())) {

                    trabajador.setConstruccionAsignada(null);
                }
            }
        }
    }

    private void aumentarCansancio(List<Trabajador> trabajadores) {

        for (Trabajador trabajador : trabajadores) {

            trabajador.aumentarCansancio(1);

            if (trabajador.getCansancio() >= 100) {
                trabajador.setEdificioAsignado(null);
            }
        }
    }
}