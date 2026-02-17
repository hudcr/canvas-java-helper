package com.example.canvashelper.repository;

import com.example.canvashelper.domain.RagChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RagChunkRepository extends JpaRepository<RagChunkEntity, Long> {
    Optional<RagChunkEntity> findBySourceTypeAndSourceId(String sourceType, String sourceId);
    List<RagChunkEntity> findTop300ByOrderByUpdatedAtDesc();
}
