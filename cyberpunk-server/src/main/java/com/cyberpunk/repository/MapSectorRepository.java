package com.cyberpunk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberpunk.domain.map.MapSector;

public interface MapSectorRepository extends JpaRepository<MapSector, Long> {

    Optional<MapSector> findByXAndY(int x, int y);

}