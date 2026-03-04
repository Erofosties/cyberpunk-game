package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberpunk.domain.edificio.Edificio;

public interface EdificioRepository extends JpaRepository<Edificio, Long> {

}