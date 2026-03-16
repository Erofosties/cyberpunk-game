package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.*;

import com.cyberpunk.service.SectorBuildService;

@RestController
@RequestMapping("/map/build")
public class SectorBuildController {

    private final SectorBuildService sectorBuildService;

    public SectorBuildController(SectorBuildService sectorBuildService) {
        this.sectorBuildService = sectorBuildService;
    }

    @PostMapping
    public String construir(

            @RequestParam Long coloniaId,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam String tipoEdificio) {

        sectorBuildService.construirEnSector(coloniaId, x, y, tipoEdificio);

        return "Construcción iniciada en el sector";
    }
}