package com.cyberpunk.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

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
    public List<MapSector> getMapaVisible(@PathVariable Long usuarioId) {

        return mapService.getMapaVisible(usuarioId);
    }
    @GetMapping("/sector")
    public MapSector getSector(
            @RequestParam int x,
            @RequestParam int y) {

        return mapService.getOrGenerateSector(x, y);
    }
}