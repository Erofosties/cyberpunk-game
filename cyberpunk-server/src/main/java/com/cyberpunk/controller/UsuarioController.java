package com.cyberpunk.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
    public Usuario crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        return usuarioService.crearUsuario(
                request.getUsername(),
                request.getPassword()
        );
    }

    @GetMapping
    public Page<Usuario> obtenerUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return usuarioService.obtenerUsuarios(pageable);
    }

    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuario(id);
    }
}