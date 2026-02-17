package com.example.canvashelper.service;

import com.example.canvashelper.client.CanvasClient;
import com.example.canvashelper.client.CanvasDtos;
import com.example.canvashelper.domain.AssignmentEntity;
import com.example.canvashelper.domain.CourseEntity;
import com.example.canvashelper.domain.GradeSnapshotEntity;
import com.example.canvashelper.dto.ApiDtos;
import com.example.canvashelper.repository.AssignmentRepository;
import com.example.canvashelper.repository.CourseRepository;
import com.example.canvashelper.repository.GradeSnapshotRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SyncService {
    private final CanvasClient canvasClient;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeSnapshotRepository gradeSnapshotRepository;
    private final RagService ragService;

    public SyncService(CanvasClient canvasClient, CourseRepository courseRepository, AssignmentRepository assignmentRepository,
                       GradeSnapshotRepository gradeSnapshotRepository, RagService ragService) {
        this.canvasClient = canvasClient;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.gradeSnapshotRepository = gradeSnapshotRepository;
        this.ragService = ragService;
    }

    @Transactional
    public ApiDtos.SyncResponse syncNow() {
        List<CanvasDtos.CourseDto> courses = canvasClient.fetchActiveCourses();
        int assignments = 0;
        for (CanvasDtos.CourseDto c : courses) {
            CourseEntity course = courseRepository.findById(c.id()).orElseGet(CourseEntity::new);
            course.setId(c.id());
            course.setName(c.name());
            course.setCourseCode(c.courseCode());
            course.setWorkflowState(c.workflowState());
            courseRepository.save(course);

            for (CanvasDtos.AssignmentDto a : canvasClient.fetchAssignments(c.id())) {
                AssignmentEntity assignment = assignmentRepository.findById(a.id()).orElseGet(AssignmentEntity::new);
                assignment.setId(a.id());
                assignment.setCourse(course);
                assignment.setName(a.name());
                assignment.setDescription(a.description());
                assignment.setPointsPossible(a.pointsPossible());
                assignment.setDueAt(a.dueAt() == null ? null : OffsetDateTime.parse(a.dueAt()));
                assignmentRepository.save(assignment);
                assignments++;
            }

            canvasClient.fetchEnrollments(c.id()).stream().findFirst().ifPresent(enrollment -> {
                if (enrollment.grades() != null) {
                    GradeSnapshotEntity snapshot = new GradeSnapshotEntity();
                    snapshot.setCourse(course);
                    snapshot.setCurrentGrade(enrollment.grades().currentGrade());
                    snapshot.setFinalGrade(enrollment.grades().finalGrade());
                    snapshot.setCurrentScore(enrollment.grades().currentScore());
                    snapshot.setFinalScore(enrollment.grades().finalScore());
                    gradeSnapshotRepository.save(snapshot);
                }
            });
        }
        int updatedChunks = ragService.rebuildChangedChunks();
        return new ApiDtos.SyncResponse(courses.size(), assignments, updatedChunks);
    }

    @Scheduled(fixedDelayString = "${app.syncHours:6}h")
    public void scheduledSync() {
        try { syncNow(); } catch (Exception ignored) {}
    }
}
