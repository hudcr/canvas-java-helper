package com.example.canvashelper.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class ApiDtos {
    public record CourseSummary(Long id, String name, String code) {}
    public record AssignmentSummary(Long id, Long courseId, String name, OffsetDateTime dueAt, Double pointsPossible) {}
    public record SyncResponse(int coursesSynced, int assignmentsSynced, int chunksUpdated) {}
    public record ChatRequest(String message, String threadId) {}
    public record ChatResponse(String answer, List<String> contextTitles) {}
}
