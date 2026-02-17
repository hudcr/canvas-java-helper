package com.example.canvashelper.client;

import com.example.canvashelper.service.SettingsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CanvasClient {
    private static final Pattern NEXT_LINK = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");
    private final HttpClient httpClient;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    public CanvasClient(HttpClient httpClient, SettingsService settingsService, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    public List<CanvasDtos.CourseDto> fetchActiveCourses() {
        String url = settingsService.canvasBaseUrl() + "/api/v1/courses?enrollment_state=active&per_page=100";
        return fetchPaged(url, new TypeReference<>() {});
    }

    public List<CanvasDtos.AssignmentDto> fetchAssignments(long courseId) {
        String url = settingsService.canvasBaseUrl() + "/api/v1/courses/" + courseId + "/assignments?per_page=100";
        return fetchPaged(url, new TypeReference<>() {});
    }

    public List<CanvasDtos.EnrollmentDto> fetchEnrollments(long courseId) {
        String url = settingsService.canvasBaseUrl() + "/api/v1/courses/" + courseId + "/enrollments?include[]=grades&per_page=100";
        return fetchPaged(url, new TypeReference<>() {});
    }

    private <T> List<T> fetchPaged(String firstUrl, TypeReference<List<T>> typeReference) {
        List<T> all = new ArrayList<>();
        String current = firstUrl;
        while (current != null) {
            HttpResponse<String> response = send(buildRequest(current));
            if (response.statusCode() == 429) {
                sleep(1200);
                continue;
            }
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Canvas API error " + response.statusCode() + ": " + response.body());
            }
            try {
                all.addAll(objectMapper.readValue(response.body(), typeReference));
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse Canvas payload", e);
            }
            current = parseNextLink(response.headers().firstValue("Link"));
        }
        return all;
    }

    private HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + settingsService.canvasToken())
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Canvas HTTP call failed", e);
        }
    }

    private String parseNextLink(Optional<String> linkHeader) {
        if (linkHeader.isEmpty()) return null;
        Matcher matcher = NEXT_LINK.matcher(linkHeader.get());
        return matcher.find() ? matcher.group(1) : null;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
