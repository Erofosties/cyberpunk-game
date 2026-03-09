package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberpunk.domain.edificio.Edificio;

@Repository
public interface EdificioRepository extends JpaRepository<Edificio, Long> {

}