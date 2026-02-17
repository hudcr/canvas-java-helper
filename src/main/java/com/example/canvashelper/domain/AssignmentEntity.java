package com.example.canvashelper.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {
    @Id
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;
    @Column(nullable = false)
    private String name;
    @Lob
    private String description;
    private Double pointsPossible;
    private OffsetDateTime dueAt;
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate(){ updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CourseEntity getCourse() { return course; }
    public void setCourse(CourseEntity course) { this.course = course; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPointsPossible() { return pointsPossible; }
    public void setPointsPossible(Double pointsPossible) { this.pointsPossible = pointsPossible; }
    public OffsetDateTime getDueAt() { return dueAt; }
    public void setDueAt(OffsetDateTime dueAt) { this.dueAt = dueAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
