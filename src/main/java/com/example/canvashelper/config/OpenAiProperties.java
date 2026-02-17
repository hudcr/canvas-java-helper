package com.example.canvashelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(String apiKey, String chatModel, String embeddingModel, String baseUrl) {
}
