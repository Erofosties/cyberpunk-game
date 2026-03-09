package com.cyberpunk.service; 

import java.util.Map; 
import org.springframework.stereotype.Service; 
import com.cyberpunk.domain.colonia.Colonia; 
import com.cyberpunk.domain.colonia.ConstruccionEnCurso;
import com.cyberpunk.domain.edificio.CosteEdificioCalculator;
import com.cyberpunk.domain.edificio.Edificio; 
import com.cyberpunk.domain.edificio.Edificio.TipoEdificio; 
import com.cyberpunk.domain.recursos.Recursos; 
import com.cyberpunk.domain.recursos.Recursos.ResourceType; 
import com.cyberpunk.repository.ColoniaRepository; 
import com.cyberpunk.repository.EdificioRepository;

@Service
public class EdificioService {

    private final ColoniaRepository coloniaRepository;
    private final EdificioRepository edificioRepository;

    public EdificioService(
            ColoniaRepository coloniaRepository,
            EdificioRepository edificioRepository) {

        this.coloniaRepository = coloniaRepository;
        this.edificioRepository = edificioRepository;
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

        Edificio edificio = new Edificio(tipo);

        Recursos recursos = colonia.getRecursos();

        Map<ResourceType, Integer> coste =
                CosteEdificioCalculator.calcularCoste(tipo, edificio.getNivel());

        if (!recursos.tieneSuficiente(coste)) {
            throw new RuntimeException("Recursos insuficientes");
        }

        recursos.consumir(coste);

        edificio.setColonia(colonia);

        edificioRepository.save(edificio);

        ConstruccionEnCurso construccion = new ConstruccionEnCurso(tipoEdificio);

        construccion.setEdificio(edificio);

        colonia.addConstruccion(construccion);

        coloniaRepository.save(colonia);
    }
}