package com.cyberpunk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}