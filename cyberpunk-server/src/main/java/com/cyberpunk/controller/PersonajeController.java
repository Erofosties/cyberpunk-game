package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.domain.personaje.Personaje;
import com.cyberpunk.dto.ConsumibleCuracionRequest;
import com.cyberpunk.dto.CrearGuerreroRequest;
import com.cyberpunk.dto.CrearPersonajeRequest;
import com.cyberpunk.dto.CrearTrabajadorRequest;
import com.cyberpunk.dto.DesplegarGuerrerosRequest;
import com.cyberpunk.dto.ResultadoAccionGuerrerosDTO;
import com.cyberpunk.service.GuerreroService;
import com.cyberpunk.service.PersonajeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/personajes")
public class PersonajeController {

    private final PersonajeService personajeService;
    private final GuerreroService guerreroService;

    public PersonajeController(PersonajeService personajeService, GuerreroService guerreroService){
        this.personajeService = personajeService;
        this.guerreroService = guerreroService;
    }

    @PostMapping
    public String crear(@Valid @RequestBody CrearPersonajeRequest request){

        personajeService.crearPersonaje(request);

        return "Personaje creado";
    }

    @PostMapping("/trabajador")
    public String crearTrabajador(@Valid @RequestBody CrearTrabajadorRequest request){

        personajeService.crearTrabajador(request);

        return "Trabajador creado";
    }

    @PostMapping("/guerrero")
    public String crearGuerrero(@Valid @RequestBody CrearGuerreroRequest request){

        personajeService.crearGuerrero(request);

        return "Guerrero creado";
    }

    @PostMapping("/guerreros/desplegar")
    public ResultadoAccionGuerrerosDTO desplegarGuerreros(@Valid @RequestBody DesplegarGuerrerosRequest request) {
        return guerreroService.desplegarGuerreros(
                request.getUsuarioId(),
                request.getGuerreroIds(),
                request.getX(),
                request.getY()
        );
    }

    @PostMapping("/guerreros/{guerreroId}/retirar")
    public String retirarGuerrero(@PathVariable Long guerreroId) {
        personajeService.retirarGuerreroANave(guerreroId);
        return "Guerrero enviado de vuelta a la nave";
    }

    @PostMapping("/{personajeId}/nanocura")
    public Personaje usarNanocura(
            @PathVariable Long personajeId,
            @Valid @RequestBody ConsumibleCuracionRequest request) {

        return personajeService.usarNanocura(request.getColoniaId(), personajeId);
    }

    @PostMapping("/{personajeId}/florsomnio")
    public Personaje usarFlorsomnio(
            @PathVariable Long personajeId,
            @Valid @RequestBody ConsumibleCuracionRequest request) {

        return personajeService.usarFlorsomnio(request.getColoniaId(), personajeId);
    }
}
