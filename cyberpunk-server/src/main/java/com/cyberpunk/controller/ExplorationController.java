package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.service.ExplorationService;
import com.cyberpunk.service.UsuarioService;

@RestController
@RequestMapping("/explore")
public class ExplorationController {

    private final ExplorationService explorationService;
    private final UsuarioService usuarioService;

    public ExplorationController(
            ExplorationService explorationService,
            UsuarioService usuarioService) {

        this.explorationService = explorationService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public MapSector explorar(

            @RequestParam Long usuarioId,
            @RequestParam int x,
            @RequestParam int y) {

        Usuario usuario = usuarioService.obtenerUsuario(usuarioId);

        return explorationService.explorarSector(usuario, x, y);
    }
}