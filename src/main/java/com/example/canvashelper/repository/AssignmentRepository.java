package com.example.canvashelper.repository;

import com.example.canvashelper.domain.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {
    List<AssignmentEntity> findByCourseId(Long courseId);
    long countByDueAtBetween(OffsetDateTime from, OffsetDateTime to);
    long countByDueAtBefore(OffsetDateTime time);
}
