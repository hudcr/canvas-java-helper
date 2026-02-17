package com.example.canvashelper.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CanvasDtos {
    public record CourseDto(Long id, String name, @JsonProperty("course_code") String courseCode,
                            @JsonProperty("workflow_state") String workflowState) {}

    public record AssignmentDto(Long id, String name, String description,
                                @JsonProperty("points_possible") Double pointsPossible,
                                @JsonProperty("due_at") String dueAt) {}

    public record EnrollmentDto(Grades grades) {
        public record Grades(@JsonProperty("current_grade") String currentGrade,
                             @JsonProperty("final_grade") String finalGrade,
                             @JsonProperty("current_score") Double currentScore,
                             @JsonProperty("final_score") Double finalScore) {}
    }
}
