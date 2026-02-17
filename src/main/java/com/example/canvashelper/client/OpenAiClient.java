package com.example.canvashelper.client;

import com.example.canvashelper.config.OpenAiProperties;
import com.example.canvashelper.service.SettingsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {
    private final HttpClient httpClient;
    private final OpenAiProperties props;
    private final SettingsService settingsService;
    private final ObjectMapper mapper;

    public OpenAiClient(HttpClient httpClient, OpenAiProperties props, SettingsService settingsService, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.props = props;
        this.settingsService = settingsService;
        this.mapper = mapper;
    }

    public List<Double> embedding(String input) {
        JsonNode node = post("/embeddings", Map.of("model", props.embeddingModel(), "input", input));
        List<Double> vector = new ArrayList<>();
        node.path("data").get(0).path("embedding").forEach(v -> vector.add(v.asDouble()));
        return vector;
    }

    public String chat(String systemPrompt, String userMessage) {
        JsonNode node = post("/chat/completions", Map.of(
                "model", props.chatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.2
        ));
        return node.path("choices").get(0).path("message").path("content").asText();
    }

    private JsonNode post(String path, Object bodyObj) {
        String apiKey = settingsService.openAiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("PUT_YOUR")) {
            throw new IllegalStateException("OpenAI API key missing. Set openai.apiKey or OPENAI_API_KEY.");
        }
        try {
            String body = mapper.writeValueAsString(bodyObj);
            HttpRequest request = HttpRequest.newBuilder(URI.create(props.baseUrl() + path))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RuntimeException("OpenAI error " + response.statusCode() + ": " + response.body());
            }
            return mapper.readTree(response.body());
        } catch (Exception e) {
            throw new RuntimeException("OpenAI HTTP call failed", e);
        }
    }
}
