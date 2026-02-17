package com.example.canvashelper.service;

import com.example.canvashelper.config.CanvasProperties;
import com.example.canvashelper.config.OpenAiProperties;
import com.example.canvashelper.domain.SettingEntity;
import com.example.canvashelper.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SettingsService {
    public static final String CANVAS_BASE_URL = "canvas.baseUrl";
    public static final String CANVAS_TOKEN = "canvas.accessToken";
    public static final String OPENAI_KEY = "openai.apiKey";
    private final SettingRepository repo;
    private final CanvasProperties canvasProps;
    private final OpenAiProperties openAiProperties;

    public SettingsService(SettingRepository repo, CanvasProperties canvasProps, OpenAiProperties openAiProperties) {
        this.repo = repo;
        this.canvasProps = canvasProps;
        this.openAiProperties = openAiProperties;
    }

    public String canvasBaseUrl() { return read(CANVAS_BASE_URL, canvasProps.baseUrl()); }
    public String canvasToken() { return read(CANVAS_TOKEN, canvasProps.accessToken()); }
    public String openAiKey() { return read(OPENAI_KEY, openAiProperties.apiKey()); }

    public void save(Map<String, String> values) {
        values.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                SettingEntity entity = new SettingEntity();
                entity.setSettingKey(k);
                entity.setSettingValue(v);
                repo.save(entity);
            }
        });
    }

    public Map<String, String> masked() {
        return Map.of(
                CANVAS_BASE_URL, canvasBaseUrl(),
                CANVAS_TOKEN, mask(canvasToken()),
                OPENAI_KEY, mask(openAiKey())
        );
    }

    private String read(String key, String fallback) {
        return repo.findById(key).map(SettingEntity::getSettingValue).orElse(fallback);
    }

    private String mask(String value) {
        if (value == null || value.length() < 8) return "(not set)";
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
