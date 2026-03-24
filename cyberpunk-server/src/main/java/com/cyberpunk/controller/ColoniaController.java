package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.dto.DesplegarNaveRequest;
import com.cyberpunk.service.ColoniaService;

import jakarta.validation.Valid;

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
            @Valid @RequestBody DesplegarNaveRequest request) {

        coloniaService.desplegarNave(
                id,
                request.getX(),
                request.getY()
        );

        return "Nave desplegada o redesplegada";
    }
}