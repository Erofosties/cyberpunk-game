package com.cyberpunk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cyberpunk.dto.AsignarConstruccionRequest;
import com.cyberpunk.dto.AsignarTrabajoRequest;
import com.cyberpunk.dto.ConstruirEdificioRequest;
import com.cyberpunk.service.EdificioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/edificios")
public class EdificioController {

    private final EdificioService edificioService;

    public EdificioController(EdificioService edificioService) {
        this.edificioService = edificioService;
    }

    @PostMapping("/construir")
    public ResponseEntity<String> construirEdificio(
            @Valid @RequestBody ConstruirEdificioRequest request) {

        edificioService.construirEdificio(
                request.getColoniaId(),
                request.getTipoEdificio()
        );

        return ResponseEntity.ok("Construccion iniciada");
    }

    @PostMapping("/asignar-construccion")
    public ResponseEntity<String> asignarConstruccion(
            @RequestBody AsignarConstruccionRequest request) {

        edificioService.asignarTrabajadorConstruccion(
                request.getPersonajeId(),
                request.getConstruccionId()
        );

        return ResponseEntity.ok("Trabajador asignado a construccion");
    }

    @PostMapping("/asignar-trabajo")
    public ResponseEntity<String> asignarTrabajo(
            @RequestBody AsignarTrabajoRequest request) {

        edificioService.asignarTrabajadorEdificio(
                request.getPersonajeId(),
                request.getEdificioId()
        );

        return ResponseEntity.ok("Trabajador asignado a edificio");
    }
}