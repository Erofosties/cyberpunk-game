package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.dto.CrearPersonajeRequest;
import com.cyberpunk.service.PersonajeService;

@RestController
@RequestMapping("/personajes")
public class PersonajeController {

    private final PersonajeService personajeService;

    public PersonajeController(PersonajeService personajeService){
        this.personajeService = personajeService;
    }

    @PostMapping
    public String crear(@RequestBody CrearPersonajeRequest request){

        personajeService.crearPersonaje(request);

        return "Personaje creado";
    }
}
