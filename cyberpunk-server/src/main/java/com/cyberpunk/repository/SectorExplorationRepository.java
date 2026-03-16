package com.cyberpunk.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.usuario.Usuario;

public interface SectorExplorationRepository extends JpaRepository<SectorExploration, Long> {

    Optional<SectorExploration> findByUsuarioAndSector(Usuario usuario, MapSector sector);

    List<SectorExploration> findByUsuario(Usuario usuario);
    List<SectorExploration> findByUsuarioIdAndVisibleTrue(Long usuarioId);

    boolean existsByUsuarioAndSector(Usuario usuario, MapSector sector);

}