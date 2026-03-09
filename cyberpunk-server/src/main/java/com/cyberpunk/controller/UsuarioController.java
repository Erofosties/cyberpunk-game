package com.cyberpunk.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.dto.CrearUsuarioRequest;
import com.cyberpunk.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody CrearUsuarioRequest request) {

        return usuarioService.crearUsuario(
                request.getUsername(),
                request.getPassword()
        );
    }

    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioService.obtenerUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuario(id);
    }
}