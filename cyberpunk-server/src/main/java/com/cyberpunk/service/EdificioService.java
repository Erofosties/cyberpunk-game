package com.cyberpunk.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.CosteEdificioCalculator;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.MapSector.SectorResource;
import com.cyberpunk.domain.map.MapSector.TerrainType;
import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.domain.personaje.Trabajador;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.exception.EntityNotFoundException;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.ConstruccionRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.PersonajeRepository;
import com.cyberpunk.repository.SectorExplorationRepository;
import com.cyberpunk.util.TravelCalculator;

import jakarta.transaction.Transactional;

@Service
public class EdificioService {

    private final ColoniaRepository coloniaRepository;
    private final ConstruccionRepository construccionRepository;
    private final PersonajeRepository personajeRepository;
    private final MapSectorRepository mapSectorRepository;
    private final SectorExplorationRepository sectorExplorationRepository;

    public EdificioService(
            ColoniaRepository coloniaRepository,
            PersonajeRepository personajeRepository,
            ConstruccionRepository construccionRepository,
            MapSectorRepository mapSectorRepository,
            SectorExplorationRepository sectorExplorationRepository) {

        this.coloniaRepository = coloniaRepository;
        this.personajeRepository = personajeRepository;
        this.construccionRepository = construccionRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.sectorExplorationRepository = sectorExplorationRepository;
    }

    // ================= CONSTRUIR =================

    @Transactional
    @SuppressWarnings("null")
    public void construirEdificio(Long coloniaId, String tipoEdificio, Long sectorId) {
        if (coloniaId == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }
        if (tipoEdificio == null || tipoEdificio.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de edificio no puede ser null o vacío");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser null");
        }

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));

        MapSector sector = mapSectorRepository.findById(sectorId)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado"));

        // ✅ Verificar que el sector haya sido explorado por este usuario
        boolean explorado = sectorExplorationRepository.existsByUsuarioIdAndSectorId(
            colonia.getUsuario().getId(),
            sector.getId());

        if (!explorado) {
            throw new RuntimeException("El sector debe estar explorado antes de construir");
        }

        // ✅ REGLA CORRECTA DE OWNERSHIP
        if (sector.getOwner() != null &&
                !sector.getOwner().getId().equals(colonia.getUsuario().getId())) {

            throw new RuntimeException("No puedes construir en un sector enemigo");
        }

        // ❌ ya tiene edificio
        if (sector.getBuilding() != null) {
            throw new RuntimeException("El sector ya tiene un edificio");
        }

        TipoEdificio tipo;

        try {
            tipo = TipoEdificio.valueOf(tipoEdificio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de edificio inválido");
        }

        // ✅ VALIDAR COMPATIBILIDAD RECURSO-EDIFICIO
        validarCompatibilidadRecursoEdificio(sector, tipo);

        // ✅ COSTE
        Recursos recursos = colonia.getRecursos();

        Map<ResourceType, Integer> coste =
                CosteEdificioCalculator.calcularCoste(tipo, 0);

        if (!recursos.tieneSuficiente(coste)) {
            throw new RuntimeException("Recursos insuficientes");
        }

        recursos.consumir(coste);

        // ✅ CLAIM DEL SECTOR (CLAVE GAMEPLAY)
        sector.setOwner(colonia.getUsuario());

        // ✅ CREAR CONSTRUCCIÓN
        ConstruccionEnCurso construccion =
                new ConstruccionEnCurso(tipo.name(), sector);

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }

    // ================= ASIGNAR CONSTRUCCIÓN =================

    @SuppressWarnings("null")
    public void asignarTrabajadorConstruccion(Long personajeId, Long construccionId) {
        if (personajeId == null) {
            throw new IllegalArgumentException("El ID del personaje no puede ser null");
        }
        if (construccionId == null) {
            throw new IllegalArgumentException("El ID de la construcción no puede ser null");
        }

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new EntityNotFoundException("Personaje no encontrado"));

        if (!(personaje instanceof Trabajador)) {
            throw new GameRuleViolationException("Solo los trabajadores pueden ser asignados a una construcción");
        }

        if (!personaje.puedeActuar()) {
            throw new GameRuleViolationException("El personaje está incapacitado y no puede trabajar");
        }

        ConstruccionEnCurso construccion = construccionRepository.findById(construccionId)
                .orElseThrow(() -> new EntityNotFoundException("Construcción no encontrada"));

        MapSector origen = personaje.getSectorActual();
        personaje.setSectorAsignado(construccion.getSectorDestino());
        personaje.setConstruccionAsignada(construccion);
        personaje.iniciarViaje(origen, TravelCalculator.calcularTicks(origen, construccion.getSectorDestino()));

        personajeRepository.save(personaje);
    }

    // ================= ASIGNAR SECTOR =================

    @SuppressWarnings("null")
    public void asignarTrabajadorSector(Long personajeId, Long sectorId) {
        if (personajeId == null) {
            throw new IllegalArgumentException("El ID del personaje no puede ser null");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser null");
        }

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new EntityNotFoundException("Personaje no encontrado"));

        if (!(personaje instanceof Trabajador)) {
            throw new GameRuleViolationException("Solo los trabajadores pueden ser asignados a un sector de trabajo");
        }

        if (!personaje.puedeActuar()) {
            throw new GameRuleViolationException("El personaje está incapacitado y no puede trabajar");
        }

        MapSector sector = mapSectorRepository.findById(sectorId)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado"));

        if (sector.getBuilding() == null) {
            throw new RuntimeException("El sector no tiene edificio");
        }

        MapSector origen = personaje.getSectorActual();

        // Asignar sector
        personaje.setSectorAsignado(sector);

        // Si hay construcción en curso en este sector, asignar a ella
        Colonia colonia = personaje.getColonia();
        for (ConstruccionEnCurso construccion : colonia.getColaConstruccion()) {
            if (construccion.getSectorDestino().equals(sector)) {
                personaje.setConstruccionAsignada(construccion);
                break;
            }
        }

        personaje.iniciarViaje(origen, TravelCalculator.calcularTicks(origen, sector));

        personajeRepository.save(personaje);
    }

    @Transactional
    public void desasignarTrabajador(Long personajeId) {
        if (personajeId == null) {
            throw new IllegalArgumentException("El ID del personaje no puede ser null");
        }

        Personaje personaje = personajeRepository.findById(personajeId)
                .orElseThrow(() -> new EntityNotFoundException("Personaje no encontrado"));

        if (!(personaje instanceof Trabajador)) {
            throw new GameRuleViolationException("El personaje indicado no es un trabajador");
        }

        Colonia colonia = personaje.getColonia();
        if (colonia == null || colonia.getSectorNave() == null) {
            throw new GameRuleViolationException("El personaje no pertenece a una colonia con nave desplegada");
        }

        if (personaje.getConstruccionAsignada() == null && personaje.getSectorAsignado() == null && !personaje.estaEnViaje()) {
            throw new GameRuleViolationException("El trabajador ya está disponible en la nave");
        }

        if (personaje.estaEnViaje()) {
            throw new GameRuleViolationException("No puedes desasignar a un trabajador mientras está viajando");
        }

        MapSector origen = personaje.getSectorActual();
        personaje.forzarReposo();
        personaje.iniciarViaje(origen, TravelCalculator.calcularTicks(origen, colonia.getSectorNave()));

        personajeRepository.save(personaje);
    }

    public List<ConstruccionEnCurso> getConstrucciones(Long coloniaId) {
        if (coloniaId == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }
        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));
        return colonia.getColaConstruccion();
    }

    @Transactional
    public void cambiarEstadoGeneradorNeon(Long coloniaId, Long sectorId, boolean activo) {
        if (coloniaId == null) {
            throw new IllegalArgumentException("El ID de la colonia no puede ser null");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El ID del sector no puede ser null");
        }

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new EntityNotFoundException("Colonia no encontrada"));

        MapSector sector = mapSectorRepository.findById(sectorId)
                .orElseThrow(() -> new EntityNotFoundException("Sector no encontrado"));

        if (sector.getOwner() == null || !sector.getOwner().getId().equals(colonia.getUsuario().getId())) {
            throw new GameRuleViolationException("El sector no pertenece a tu colonia");
        }

        if (sector.getBuilding() != TipoEdificio.GENERADOR_NEON) {
            throw new GameRuleViolationException("El sector no contiene un generador neon");
        }

        sector.setGeneradorNeonActivo(activo);
        mapSectorRepository.save(sector);
    }

    private static final Map<TipoEdificio, SectorResource> RECURSO_REQUERIDO = Map.ofEntries(
        Map.entry(TipoEdificio.MINA_NEOCROMO,       SectorResource.NEOCROMO),
        Map.entry(TipoEdificio.MINA_UMBRIUM,         SectorResource.UMBRIUM),
        Map.entry(TipoEdificio.MINA_SYNTHERIUM,      SectorResource.SYNTHERIUM),
        Map.entry(TipoEdificio.MINA_HEXALIUM,        SectorResource.HEXALIUM),
        Map.entry(TipoEdificio.MINA_VOIDIUM,         SectorResource.VOIDIUM),
        Map.entry(TipoEdificio.GRANJA_KROMAFRUTA,    SectorResource.KROMAFRUTA),
        Map.entry(TipoEdificio.GRANJA_NEUROTRIGO,    SectorResource.NEUROTRIGO),
        Map.entry(TipoEdificio.GRANJA_ALGACARNE,     SectorResource.ALGACARNE),
        Map.entry(TipoEdificio.CRIADERO_RATAX,       SectorResource.RATAX),
        Map.entry(TipoEdificio.CULTIVO_FLORSOMNIO,   SectorResource.FLORSOMNIO),
        Map.entry(TipoEdificio.LAB_REFLEXA,          SectorResource.REFLEXA),
        Map.entry(TipoEdificio.LAB_NANOCURA,         SectorResource.NANOCURA),
        Map.entry(TipoEdificio.LAB_SOMNEX,           SectorResource.SOMNEX)
    );

    private void validarCompatibilidadRecursoEdificio(MapSector sector, TipoEdificio tipo) {
        if (esInfraestructuraEnergetica(tipo) && sector.getTerrain() != TerrainType.DESIERTO) {
            throw new GameRuleViolationException(
                    "La infraestructura energética solo puede construirse en sectores de desierto");
        }

        if (esDefensaSectorial(tipo) && sector.getTerrain() == TerrainType.PANTANO) {
            throw new GameRuleViolationException("Las defensas no pueden construirse en pantano");
        }

        SectorResource recursoRequerido = RECURSO_REQUERIDO.get(tipo);
        if (recursoRequerido == null) return; // PLACA_SOLAR, REACTOR_FUSION, etc. — sin restricción

        SectorResource recursoSector = sector.getResource();
        if (recursoSector != recursoRequerido) {
            throw new GameRuleViolationException(
                "No puedes construir " + tipo.name() + " aquí. " +
                "Requiere el recurso " + recursoRequerido.name() +
                ", pero este sector tiene: " + recursoSector.name()
            );
        }
    }

    private boolean esInfraestructuraEnergetica(TipoEdificio tipo) {
        return tipo == TipoEdificio.PLACA_SOLAR
                || tipo == TipoEdificio.GENERADOR_NEON
                || tipo == TipoEdificio.REACTOR_FUSION
                || tipo == TipoEdificio.BATERIA_ENERGIA;
    }

    private boolean esDefensaSectorial(TipoEdificio tipo) {
        return tipo == TipoEdificio.ESCUDO_SECTOR
                || tipo == TipoEdificio.TORRETA_NEOCROMO
                || tipo == TipoEdificio.CANON_HEXALIUM;
    }
}