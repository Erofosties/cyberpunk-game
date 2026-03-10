package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberpunk.domain.personaje.Personaje;

public interface PersonajeRepository extends JpaRepository<Personaje, Long> {

}