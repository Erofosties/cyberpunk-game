package com.cyberpunk.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public Usuario crearUsuario(@RequestParam String username, @RequestParam String password) {
        return usuarioService.crearUsuario(username, password);
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