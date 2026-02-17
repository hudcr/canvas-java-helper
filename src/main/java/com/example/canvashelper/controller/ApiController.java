package com.example.canvashelper.controller;

import com.example.canvashelper.domain.AssignmentEntity;
import com.example.canvashelper.dto.ApiDtos;
import com.example.canvashelper.repository.AssignmentRepository;
import com.example.canvashelper.repository.CourseRepository;
import com.example.canvashelper.service.ChatService;
import com.example.canvashelper.service.SettingsService;
import com.example.canvashelper.service.SyncService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final SyncService syncService;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final ChatService chatService;
    private final SettingsService settingsService;

    public ApiController(SyncService syncService, CourseRepository courseRepo, AssignmentRepository assignmentRepo,
                         ChatService chatService, SettingsService settingsService) {
        this.syncService = syncService;
        this.courseRepo = courseRepo;
        this.assignmentRepo = assignmentRepo;
        this.chatService = chatService;
        this.settingsService = settingsService;
    }

    @PostMapping("/sync")
    public ApiDtos.SyncResponse sync() { return syncService.syncNow(); }

    @GetMapping("/courses")
    public Map<String, Object> courses() {
        var data = courseRepo.findAll().stream().map(c -> new ApiDtos.CourseSummary(c.getId(), c.getName(), c.getCourseCode())).toList();
        long upcoming = assignmentRepo.countByDueAtBetween(OffsetDateTime.now(), OffsetDateTime.now().plusDays(7));
        long overdue = assignmentRepo.countByDueAtBefore(OffsetDateTime.now());
        return Map.of("courses", data, "upcoming", upcoming, "overdue", overdue);
    }

    @GetMapping("/assignments")
    public List<ApiDtos.AssignmentSummary> assignments(@RequestParam Long courseId) {
        return assignmentRepo.findByCourseId(courseId).stream().map(this::toSummary).toList();
    }

    @PostMapping("/chat")
    public ApiDtos.ChatResponse chat(@Valid @RequestBody ApiDtos.ChatRequest request) {
        return chatService.answer(request.message());
    }

    @GetMapping("/settings")
    public Map<String, String> getSettings() { return settingsService.masked(); }

    @PostMapping("/settings")
    public void saveSettings(@RequestBody Map<String, String> settings) { settingsService.save(settings); }

    private ApiDtos.AssignmentSummary toSummary(AssignmentEntity a) {
        return new ApiDtos.AssignmentSummary(a.getId(), a.getCourse().getId(), a.getName(), a.getDueAt(), a.getPointsPossible());
    }
}
