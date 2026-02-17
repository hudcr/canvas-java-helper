package com.example.canvashelper.repository;

import com.example.canvashelper.domain.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
}
