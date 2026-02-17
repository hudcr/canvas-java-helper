package com.example.canvashelper.service;

import com.example.canvashelper.client.OpenAiClient;
import com.example.canvashelper.domain.*;
import com.example.canvashelper.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RagService {
    public record Retrieval(RagChunkEntity chunk, double score) {}

    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final GradeSnapshotRepository gradeRepo;
    private final RagChunkRepository chunkRepo;
    private final EmbeddingRepository embeddingRepo;
    private final OpenAiClient openAiClient;
    private final ObjectMapper mapper;

    public RagService(CourseRepository courseRepo, AssignmentRepository assignmentRepo, GradeSnapshotRepository gradeRepo,
                      RagChunkRepository chunkRepo, EmbeddingRepository embeddingRepo, OpenAiClient openAiClient,
                      ObjectMapper mapper) {
        this.courseRepo = courseRepo;
        this.assignmentRepo = assignmentRepo;
        this.gradeRepo = gradeRepo;
        this.chunkRepo = chunkRepo;
        this.embeddingRepo = embeddingRepo;
        this.openAiClient = openAiClient;
        this.mapper = mapper;
    }

    public int rebuildChangedChunks() {
        int changed = 0;
        for (CourseEntity c : courseRepo.findAll()) changed += upsertChunk("course", String.valueOf(c.getId()), c.getName(),
                "Course: %s (%s)".formatted(c.getName(), Optional.ofNullable(c.getCourseCode()).orElse("n/a")));
        for (AssignmentEntity a : assignmentRepo.findAll()) {
            String content = "Assignment in %s: %s\nDue: %s\nPoints: %s\nDescription: %s".formatted(
                    a.getCourse().getName(), a.getName(), a.getDueAt(), a.getPointsPossible(), strip(a.getDescription()));
            changed += upsertChunk("assignment", String.valueOf(a.getId()), a.getName(), content);
        }
        for (GradeSnapshotEntity g : gradeRepo.findAll()) {
            String content = "Grade snapshot for %s current=%s (%.2f) final=%s (%.2f) captured=%s".formatted(
                    g.getCourse().getName(), g.getCurrentGrade(), n(g.getCurrentScore()), g.getFinalGrade(), n(g.getFinalScore()), g.getCapturedAt());
            changed += upsertChunk("grade", String.valueOf(g.getId()), "Grade: " + g.getCourse().getName(), content);
        }
        return changed;
    }

    public List<Retrieval> retrieve(String query, int topK) {
        List<Double> qv = openAiClient.embedding(query);
        List<Retrieval> scores = new ArrayList<>();
        for (EmbeddingEntity emb : embeddingRepo.findAll()) {
            RagChunkEntity c = emb.getChunk();
            double score = cosine(qv, vectorFromJson(emb.getVectorJson()));
            if ("assignment".equals(c.getSourceType()) && c.getContent().contains("Due:")) {
                String dueRaw = c.getContent().split("\\n")[1].replace("Due: ", "");
                try {
                    long days = ChronoUnit.DAYS.between(OffsetDateTime.now(), OffsetDateTime.parse(dueRaw));
                    score += days < 0 ? 0.08 : (days <= 7 ? 0.12 : 0);
                } catch (Exception ignored) {}
            }
            long recency = ChronoUnit.DAYS.between(c.getUpdatedAt(), OffsetDateTime.now());
            score += recency <= 3 ? 0.06 : 0;
            scores.add(new Retrieval(c, score));
        }
        return scores.stream().sorted(Comparator.comparingDouble(Retrieval::score).reversed()).limit(topK).toList();
    }

    private int upsertChunk(String sourceType, String sourceId, String title, String content) {
        String hash = sha256(content);
        RagChunkEntity chunk = chunkRepo.findBySourceTypeAndSourceId(sourceType, sourceId).orElseGet(RagChunkEntity::new);
        boolean needsEmbedding = chunk.getId() == null || !hash.equals(chunk.getContentHash());
        chunk.setSourceType(sourceType);
        chunk.setSourceId(sourceId);
        chunk.setTitle(title);
        chunk.setContent(content);
        chunk.setContentHash(hash);
        chunkRepo.save(chunk);
        if (needsEmbedding) {
            List<Double> vector = openAiClient.embedding(content);
            EmbeddingEntity emb = embeddingRepo.findById(chunk.getId()).orElseGet(EmbeddingEntity::new);
            emb.setChunk(chunk);
            emb.setModel("default");
            emb.setVectorJson(toJson(vector));
            embeddingRepo.save(emb);
            return 1;
        }
        return 0;
    }

    private String strip(String html) { return html == null ? "" : html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim(); }
    private double n(Double v){ return v == null ? 0d : v; }

    private List<Double> vectorFromJson(String json) {
        try { return mapper.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) { return List.of(); }
    }
    private String toJson(List<Double> d){ try { return mapper.writeValueAsString(d);} catch (JsonProcessingException e){ throw new RuntimeException(e);} }

    private double cosine(List<Double> a, List<Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        int n = Math.min(a.size(), b.size());
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < n; i++) { dot += a.get(i) * b.get(i); na += a.get(i) * a.get(i); nb += b.get(i) * b.get(i); }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-9);
    }

    private String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
