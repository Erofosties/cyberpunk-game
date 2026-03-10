package com.cyberpunk.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.repository.ColoniaRepository;

@Service
public class GameEngine {

    private final ColoniaRepository coloniaRepository;

    public GameEngine(ColoniaRepository coloniaRepository) {
        this.coloniaRepository = coloniaRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void actualizarJuego() {

        List<Colonia> colonias = coloniaRepository.findAll();

        for (Colonia colonia : colonias) {

            colonia.procesarConstrucciones();

            colonia.producirRecursos();
        }

        coloniaRepository.saveAll(colonias);

        System.out.println("Tick del juego ejecutado");
    }
}