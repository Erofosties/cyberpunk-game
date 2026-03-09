package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberpunk.domain.colonia.Colonia;

@Repository
public interface ColoniaRepository extends JpaRepository<Colonia, Long> {

}