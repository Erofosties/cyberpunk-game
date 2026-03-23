package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.service.MapService;

@RestController
@RequestMapping("/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }
    @GetMapping("/{usuarioId}")
    public String getMapaVisible(@PathVariable Long usuarioId) {

        return mapService.getMapaVisibleComoTexto(usuarioId);
    }
    @GetMapping("/sector")
    public MapSector getSector(
            @RequestParam int x,
            @RequestParam int y) {

        return mapService.getOrGenerateSector(x, y);
    }
}