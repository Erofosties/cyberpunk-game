package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.dto.ConstruirEdificioRequest;
import com.cyberpunk.service.EdificioService;

@RestController
@RequestMapping("/edificios")
public class EdificioController {
	private final EdificioService edificioService;
	
	public EdificioController(EdificioService edificioService) {
		this.edificioService = edificioService;
		
	}
	
	@PostMapping("/construir")
	public String construirEdificio(@RequestBody ConstruirEdificioRequest request) {
		edificioService.construirEdificio(
				request.getColoniaId(),
				request.getTipoEdificio());
		return "Construccion iniciada";
	}
	

}
