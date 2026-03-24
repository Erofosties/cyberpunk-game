package com.cyberpunk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.dto.SectorDetalleDTO;
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
    public SectorDetalleDTO getSector(
            @RequestParam Long usuarioId,
            @RequestParam int x,
            @RequestParam int y) {

        return mapService.getSectorDetalle(usuarioId, x, y);
    }
}