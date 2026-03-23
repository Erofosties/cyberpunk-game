package com.cyberpunk.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.Edificio;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.personaje.Guerrero;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.PersonajeRepository;

@Component
public class GameEngine {

    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository mapSectorRepository;
    private final PersonajeRepository personajeRepository;
    private final ExplorationService explorationService;
    private final GuerreroService guerreroService;

    public GameEngine(
            ColoniaRepository coloniaRepository,
            MapSectorRepository mapSectorRepository,
            PersonajeRepository personajeRepository,
            ExplorationService explorationService,
            GuerreroService guerreroService) {

        this.coloniaRepository = coloniaRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.personajeRepository = personajeRepository;
        this.explorationService = explorationService;
        this.guerreroService = guerreroService;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void tick() {

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            procesarViajes(colonia);
            guerreroService.resolverMisionesAlLlegar(colonia);

            procesarConstrucciones(colonia);
            procesarProduccion(colonia);

            procesarConsumoComida(colonia);
            procesarAlimentacion(colonia);
            procesarDanioPorInanicion(colonia);
            procesarCuracionNatural(colonia);
            procesarDescanso(colonia);
            procesarExploracion(colonia);
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick ejecutado: colonias procesadas = " + colonias.size());
    }

    // ================= EXPLORACIÓN =================

    private void procesarExploracion(Colonia colonia) {

        // No ocultamos toda la visibilidad cada tick.
        // Mantener exploración persistente evita que el mapa se convierta en 'f' tras un tick.
        // explorationService.ocultarVisibilidad(usuarioId);

        List<Personaje> personajes = personajeRepository.findByColonia(colonia);

        for (Personaje personaje : personajes) {

            if (personaje instanceof Guerrero guerrero && guerrero.puedeActuar()) {

                if (guerrero.estaEnViaje()) {
                    continue;
                }

                explorationService.revelarAlrededor(guerrero);
            }
        }
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
                    && trabajador.getConstruccionAsignada() == construccion) {

                if (!trabajador.puedeActuar()) {
                    continue;
                }

                if (trabajador.estaEnViaje()) {
                    continue;
                }

                if (trabajador.getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(trabajador);
                    continue;
                }

                progreso += trabajador.getIngenieria();
                trabajador.aumentarCansancio(2);

                if (trabajador.getCansancio() >= 100) {
                    enviarTrabajadorANavePorAgotamiento(trabajador);
                }
            }
        }

        return progreso;
    }

    private void crearEdificio(ConstruccionEnCurso construccion, Colonia colonia) {

        MapSector sector = construccion.getSectorDestino();

        if (sector == null)
            return;

        // Protección extra
        if (sector.getBuilding() != null)
            return;

        sector.setBuilding(
                Edificio.TipoEdificio.valueOf(construccion.getTipo())
        );

        sector.setOwner(colonia.getUsuario());

        mapSectorRepository.save(sector);
    }

    // ================= PRODUCCIÓN =================

    private void procesarProduccion(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (!(personaje instanceof Trabajador trabajador))
                continue;

            if (!trabajador.puedeActuar())
                continue;

            if (trabajador.estaEnViaje())
                continue;

            if (trabajador.getSectorAsignado() == null)
                continue;

            // 🔴 IMPORTANTE: evitar producir mientras construye
            if (trabajador.getConstruccionAsignada() != null)
                continue;

            if (trabajador.getCansancio() >= 100) {
                enviarTrabajadorANavePorAgotamiento(trabajador);
                continue;
            }

            MapSector sector = trabajador.getSectorAsignado();

            if (sector.getBuilding() == null)
                continue;

            producirEnSector(colonia, trabajador, sector);
        }
    }

    private void producirEnSector(Colonia colonia, Trabajador trabajador, MapSector sector) {

        var tipo = sector.getBuilding();

        int habilidad = trabajador.getProduccionParaEdificio(tipo);
        int base = com.cyberpunk.gameBalance.GameBalance.getProduccionBase(tipo);
        double riqueza = sector.getRichness();

        int produccion = (int) (base * habilidad * riqueza);

        var recurso = convertirRecurso(tipo);

        if (recurso != null) {
            colonia.getRecursos().add(recurso, produccion);
        }

        trabajador.aumentarCansancio(1);

        if (trabajador.getCansancio() >= 100) {
            enviarTrabajadorANavePorAgotamiento(trabajador);
        }
    }

    private com.cyberpunk.domain.recursos.Recursos.ResourceType convertirRecurso(Edificio.TipoEdificio tipo) {

        return switch (tipo) {

            case MINA_NEOCROMO -> com.cyberpunk.domain.recursos.Recursos.ResourceType.NEOCROMO;
            case MINA_UMBRIUM -> com.cyberpunk.domain.recursos.Recursos.ResourceType.UMBRIUM;
            case MINA_SYNTHERIUM -> com.cyberpunk.domain.recursos.Recursos.ResourceType.SYNTHERIUM;
            case MINA_HEXALIUM -> com.cyberpunk.domain.recursos.Recursos.ResourceType.HEXALIUM;
            case MINA_VOIDIUM -> com.cyberpunk.domain.recursos.Recursos.ResourceType.VOIDIUM;

            case GRANJA_KROMAFRUTA -> com.cyberpunk.domain.recursos.Recursos.ResourceType.KROMAFRUTA;
            case GRANJA_NEUROTRIGO -> com.cyberpunk.domain.recursos.Recursos.ResourceType.NEUROTRIGO;
            case GRANJA_ALGACARNE -> com.cyberpunk.domain.recursos.Recursos.ResourceType.ALGACARNE;
            case CRIADERO_RATAX -> com.cyberpunk.domain.recursos.Recursos.ResourceType.RATAX;
            case CULTIVO_FLORSOMNIO -> com.cyberpunk.domain.recursos.Recursos.ResourceType.FLORSOMNIO;

            case LAB_REFLEXA -> com.cyberpunk.domain.recursos.Recursos.ResourceType.REFLEXA;
            case LAB_NANOCURA -> com.cyberpunk.domain.recursos.Recursos.ResourceType.NANOCURA;
            case LAB_SOMNEX -> com.cyberpunk.domain.recursos.Recursos.ResourceType.SOMNEX;

            default -> null;
        };
    }

    // ================= UTILIDADES =================

    private void procesarViajes(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.avanzarViaje();
        }
    }

    private void liberarTrabajadoresConstruccion(Colonia colonia, ConstruccionEnCurso construccion) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje instanceof Trabajador trabajador) {

                if (trabajador.getConstruccionAsignada() == construccion) {
                    MapSector origen = trabajador.getSectorAsignado() != null
                            ? trabajador.getSectorAsignado()
                            : construccion.getSectorDestino();
                    trabajador.setConstruccionAsignada(null);
                    trabajador.setSectorAsignado(null);
                    trabajador.iniciarViaje(origen, com.cyberpunk.util.TravelCalculator.calcularTicks(origen, colonia.getSectorNave()));
                }
            }
        }
    }

    private void enviarTrabajadorANavePorAgotamiento(Trabajador trabajador) {
        MapSector origen = trabajador.getSectorActual();
        trabajador.setConstruccionAsignada(null);
        trabajador.setSectorAsignado(null);
        if (trabajador.getColonia() != null) {
            trabajador.iniciarViaje(origen, com.cyberpunk.util.TravelCalculator.calcularTicks(origen, trabajador.getColonia().getSectorNave()));
        }
    }

    // ================= SISTEMA BIOLÓGICO =================

    private void procesarConsumoComida(Colonia colonia) {
        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.acumularConsumoComida(calcularConsumoComida(personaje));
        }
    }

    private int calcularConsumoComida(Personaje personaje) {

        if (personaje instanceof Trabajador trabajador) {

            if (trabajador.getConstruccionAsignada() != null) {
                return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_TRABAJANDO;
            }

            if (trabajador.getSectorAsignado() != null && trabajador.getSectorAsignado().getBuilding() != null) {
                return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_TRABAJANDO;
            }
        }

        return com.cyberpunk.gameBalance.GameBalance.CONSUMO_COMIDA_INACTIVO;
    }

    private void procesarAlimentacion(Colonia colonia) {

        var recursos = colonia.getRecursos();

        for (Personaje personaje : colonia.getPoblacion()) {
            personaje.intentarComer(recursos);
        }
    }

    private void procesarDanioPorInanicion(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.sinComida()) {
                personaje.recibirDanio(com.cyberpunk.gameBalance.GameBalance.DANO_POR_INANICION);
            }
        }
    }

    private void procesarCuracionNatural(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.estaHerido() && personaje.getComida() > 60) {
                personaje.curar(com.cyberpunk.gameBalance.GameBalance.CURACION_NATURAL_POR_TICK);
            }
        }
    }

    private void procesarDescanso(Colonia colonia) {

        for (Personaje personaje : colonia.getPoblacion()) {

            if (personaje.estaDisponible()) {
                personaje.descansar();
            }
        }
    }
}