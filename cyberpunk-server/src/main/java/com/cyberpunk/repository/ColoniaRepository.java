package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberpunk.domain.colonia.Colonia;

public interface ColoniaRepository extends JpaRepository<Colonia, Long> {
}