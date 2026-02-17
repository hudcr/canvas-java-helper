package com.example.canvashelper.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "courses")
public class CourseEntity {
    @Id
    private Long id;
    @Column(nullable = false)
    private String name;
    private String courseCode;
    private String workflowState;
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate(){ updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getWorkflowState() { return workflowState; }
    public void setWorkflowState(String workflowState) { this.workflowState = workflowState; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
