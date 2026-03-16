package com.cyberpunk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ColoniaRepository coloniaRepository;
    private final PersonajeService personajeService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ColoniaRepository coloniaRepository,
            PersonajeService personajeService) {

        this.usuarioRepository = usuarioRepository;
        this.coloniaRepository = coloniaRepository;
        this.personajeService = personajeService;
    }

    public Usuario crearUsuario(String username, String password) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        // Crear usuario
        Usuario usuario = new Usuario(username, password);
        usuarioRepository.save(usuario);

        // Crear colonia
        Colonia colonia = new Colonia("Colonia Inicial");
        colonia.setUsuario(usuario);

        // IMPORTANTE: NO se asigna sector nave aquí
        colonia.setSectorNave(null);

        coloniaRepository.save(colonia);

        usuario.setColonia(colonia);
        usuarioRepository.save(usuario);

        // =========================
        // PERSONAJES INICIALES
        // =========================

        personajeService.crearTrabajadorInicial(colonia, "Trabajador1");
        personajeService.crearTrabajadorInicial(colonia, "Trabajador2");
        personajeService.crearTrabajadorInicial(colonia, "Trabajador3");

        personajeService.crearGuerreroInicial(colonia, "Guerrero1");
        personajeService.crearGuerreroInicial(colonia, "Guerrero2");

        return usuario;
    }

    public Usuario buscarPorUsername(String username) {

        return usuarioRepository
                .findByUsername(username)
                .orElse(null);
    }

    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}