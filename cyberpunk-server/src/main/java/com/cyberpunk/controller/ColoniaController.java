package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.repository.ColoniaRepository;
import com.cyberpunk.service.ColoniaService;

@RestController
@RequestMapping("/colonias")
public class ColoniaController {

    private final ColoniaRepository coloniaRepository;
    private final ColoniaService coloniaService;

    public ColoniaController(ColoniaRepository coloniaRepository,
                             ColoniaService coloniaService) {
        this.coloniaRepository = coloniaRepository;
        this.coloniaService = coloniaService;
    }

    @GetMapping("/{id}")
    public Colonia obtenerColonia(@PathVariable Long id) {
        return coloniaRepository.findById(id).orElse(null);
    }

    @PostMapping("/{id}/procesar")
    public void procesarConstrucciones(@PathVariable Long id) {
        coloniaService.procesarConstrucciones(id);
    }
}