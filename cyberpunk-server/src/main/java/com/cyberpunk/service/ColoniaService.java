package com.cyberpunk.service;

import java.util.Iterator;

import org.springframework.stereotype.Service;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.repository.ColoniaRepository;

@Service
public class ColoniaService {

    private final ColoniaRepository coloniaRepository;

    public ColoniaService(ColoniaRepository coloniaRepository) {
        this.coloniaRepository = coloniaRepository;
    }
    public Colonia obtenerColonia(Long id) {
        return coloniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colonia no encontrada"));
    }
    public void procesarConstrucciones(Long coloniaId) {

        Colonia colonia = coloniaRepository.findById(coloniaId).orElseThrow();

        Iterator<ConstruccionEnCurso> it = colonia.getColaConstruccion().iterator();

        while (it.hasNext()) {

            ConstruccionEnCurso c = it.next();

            if (c.finalizada()) {

                c.getEdificio().subirNivel();

                it.remove();
            }
        }

        coloniaRepository.save(colonia);
    }
}
