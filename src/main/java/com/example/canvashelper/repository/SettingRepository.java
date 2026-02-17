package com.example.canvashelper.repository;

import com.example.canvashelper.domain.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<SettingEntity, String> {
}
