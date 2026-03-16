package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.dto.DesplegarNaveRequest;
import com.cyberpunk.service.ColoniaService;

@RestController
@RequestMapping("/colonias")
public class ColoniaController {

    private final ColoniaService coloniaService;

    public ColoniaController(ColoniaService coloniaService) {
        this.coloniaService = coloniaService;
    }

    @GetMapping("/{id}")
    public Colonia obtenerColonia(@PathVariable Long id) {
        return coloniaService.obtenerColonia(id);
    }
    @PostMapping("/{id}/desplegar")
    public String desplegarNave(
            @PathVariable Long id,
            @RequestBody DesplegarNaveRequest request) {

        coloniaService.desplegarNave(
                id,
                request.getX(),
                request.getY()
        );

        return "Nave desplegada";
    }
}