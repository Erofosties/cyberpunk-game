package com.cyberpunk.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.CosteEdificioCalculator;
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio;
import com.cyberpunk.domain.recursos.Recursos;
import com.cyberpunk.domain.recursos.Recursos.ResourceType;
import com.cyberpunk.repository.ColoniaRepository;

@Service
public class EdificioService {

    private final ColoniaRepository coloniaRepository;

    public EdificioService(ColoniaRepository coloniaRepository) {
        this.coloniaRepository = coloniaRepository;
    }

    public void construirEdificio(Long coloniaId, String tipoEdificio) {

        Colonia colonia = coloniaRepository.findById(coloniaId)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));

        TipoEdificio tipo;

        try {
            tipo = TipoEdificio.valueOf(tipoEdificio);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de edificio inválido");
        }

        Recursos recursos = colonia.getRecursos();

        Map<ResourceType, Integer> coste =
                CosteEdificioCalculator.calcularCoste(tipo, 0);

        if (!recursos.tieneSuficiente(coste)) {
            throw new RuntimeException("Recursos insuficientes");
        }

        recursos.consumir(coste);

        ConstruccionEnCurso construccion = new ConstruccionEnCurso(tipoEdificio);

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }
}