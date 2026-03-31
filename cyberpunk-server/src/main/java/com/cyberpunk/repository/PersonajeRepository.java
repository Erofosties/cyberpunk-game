package com.cyberpunk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.personaje.Personaje;

public interface PersonajeRepository extends JpaRepository<Personaje, Long> {

    List<Personaje> findByColonia(Colonia colonia);
}