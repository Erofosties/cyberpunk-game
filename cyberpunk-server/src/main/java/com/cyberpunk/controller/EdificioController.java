package com.cyberpunk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cyberpunk.dto.AsignarConstruccionRequest;
import com.cyberpunk.dto.AsignarSectorTrabajoRequest;
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

    // ================= CONSTRUIR EDIFICIO =================

    @PostMapping("/construir")
    public ResponseEntity<String> construirEdificio(
            @Valid @RequestBody ConstruirEdificioRequest request) {

        edificioService.construirEdificio(
                request.getColoniaId(),
                request.getTipoEdificio(),
                request.getSectorId()
        );

        return ResponseEntity.ok("Construccion iniciada correctamente");
    }

    // ================= ASIGNAR TRABAJADOR A CONSTRUCCION =================

    @PostMapping("/asignar-construccion")
    public ResponseEntity<String> asignarConstruccion(
            @Valid @RequestBody AsignarConstruccionRequest request) {

        edificioService.asignarTrabajadorConstruccion(
                request.getPersonajeId(),
                request.getConstruccionId()
        );

        return ResponseEntity.ok("Trabajador asignado a construccion");
    }

    // ================= ASIGNAR TRABAJADOR A SECTOR =================

    @PostMapping("/asignar-trabajo")
    public ResponseEntity<String> asignarTrabajo(
            @Valid @RequestBody AsignarSectorTrabajoRequest request) {

        edificioService.asignarTrabajadorSector(
                request.getPersonajeId(),
                request.getSectorId()
        );

        return ResponseEntity.ok("Trabajador asignado a sector");
    }
}