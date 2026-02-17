package com.example.canvashelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "canvas")
public record CanvasProperties(String baseUrl, String accessToken) {
}
