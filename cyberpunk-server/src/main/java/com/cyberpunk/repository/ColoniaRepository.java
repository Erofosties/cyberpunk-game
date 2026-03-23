package com.cyberpunk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberpunk.domain.colonia.Colonia;

@Repository
public interface ColoniaRepository extends JpaRepository<Colonia, Long> {

    List<Colonia> findBySectorNaveIsNotNull();

    Optional<Colonia> findByUsuarioId(Long usuarioId);
}