package com.cyberpunk.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.exception.GameRuleViolationException;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.MapSectorRepository;
import com.cyberpunk.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ColoniaRepository coloniaRepository;
    private final MapSectorRepository mapSectorRepository;
    private final PersonajeService personajeService;
    private final MapService mapService;
    private final ExplorationService explorationService;
    private final ColoniaService coloniaService;

    private final Random random = new Random();

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ColoniaRepository coloniaRepository,
            MapSectorRepository mapSectorRepository,
            PersonajeService personajeService,
            MapService mapService,
            ExplorationService explorationService,
            ColoniaService coloniaService) {

        this.usuarioRepository = usuarioRepository;
        this.coloniaRepository = coloniaRepository;
        this.mapSectorRepository = mapSectorRepository;
        this.personajeService = personajeService;
        this.mapService = mapService;
        this.explorationService = explorationService;
        this.coloniaService = coloniaService;
    }

    @Transactional
    public Usuario crearUsuario(String username, String password) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        // =========================
        // CREAR USUARIO
        // =========================

        Usuario usuario = new Usuario(username, password);
        usuarioRepository.save(usuario);

        // =========================
        // CREAR COLONIA
        // =========================

        Colonia colonia = new Colonia("Colonia Inicial");
        colonia.setUsuario(usuario);

        // 🔴 NUEVO: generar sector inicial
        MapSector sectorInicial = generarSectorInicial();
        sectorInicial.setOwner(usuario);

        colonia.setSectorNave(sectorInicial);

        coloniaRepository.save(colonia);

        usuario.setColonia(colonia);

        // =========================
        // PERSONAJES INICIALES
        // =========================

        personajeService.crearTrabajadorInicial(colonia, "Trabajador1");
        personajeService.crearTrabajadorInicial(colonia, "Trabajador2");
        personajeService.crearTrabajadorInicial(colonia, "Trabajador3");

        personajeService.crearGuerreroInicial(colonia, "Guerrero1");
        personajeService.crearGuerreroInicial(colonia, "Guerrero2");

        // 🔴 IMPORTANTE: guardar colonia después de añadir personajes
        coloniaRepository.save(colonia);

        // =========================
        // FOG INICIAL
        // =========================

        explorationService.revelarSectoresIniciales(colonia);

        return usuario;
    }

    private MapSector generarSectorInicial() {

        int intentos = 0;
        final int MAX_INTENTOS = 1000;

        while (intentos < MAX_INTENTOS) {

            int x = random.nextInt(101) - 50; // -50 to 50
            int y = random.nextInt(101) - 50;

            // Primero verificar distancia
            if (coloniaService.validarDistanciaNuevaColonia(x, y)) {

                // Verificar si sector existe y owner null
                Optional<MapSector> sectorOpt = mapSectorRepository.findByXAndY(x, y);
                if (sectorOpt.isEmpty() || sectorOpt.get().getOwner() == null) {

                    // Ok, generar/obtener
                    return mapService.getOrGenerateSector(x, y);
                }
            }

            intentos++;
        }

        throw new GameRuleViolationException("No se pudo encontrar un sector válido para la colonia inicial");
    }

    public Usuario buscarPorUsername(String username) {

        return usuarioRepository
                .findByUsername(username)
                .orElse(null);
    }

    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @SuppressWarnings("null")
    public Usuario obtenerUsuario(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }
        return usuarioRepository.findById(id).orElse(null);
    }
}