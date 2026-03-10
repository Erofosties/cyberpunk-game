package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.service.TareaService;
import com.cyberpunk.dto.AsignarTareaRequest;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @PostMapping("/asignar")
    public String asignarTarea(@RequestBody AsignarTareaRequest request) {

        tareaService.asignarTarea(
                request.getPersonajeId(),
                request.getTipo(),
                request.getEdificioId()
        );

        return "Tarea asignada";
    }
}