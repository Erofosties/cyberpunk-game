package com.cyberpunk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cyberpunk.domain.map.MapSector;
import com.cyberpunk.domain.map.SectorExploration;
import com.cyberpunk.domain.usuario.Usuario;

public interface SectorExplorationRepository extends JpaRepository<SectorExploration, Long> {

    // ================= BÁSICOS =================

    Optional<SectorExploration> findByUsuarioAndSector(Usuario usuario, MapSector sector);

    List<SectorExploration> findByUsuario(Usuario usuario);

    // 🔴 NUEVO: necesario para DTO del mapa
    List<SectorExploration> findByUsuarioId(Long usuarioId);

    List<SectorExploration> findByUsuarioIdAndVisibleTrue(Long usuarioId);

    boolean existsByUsuarioAndSector(Usuario usuario, MapSector sector);
    boolean existsByUsuarioIdAndSectorId(Long usuarioId, Long sectorId);

    // ================= FOG OF WAR =================

    @Modifying
    @Query("UPDATE SectorExploration se SET se.visible = false WHERE se.usuario.id = :usuarioId")
    void ocultarTodosPorUsuario(Long usuarioId);
}