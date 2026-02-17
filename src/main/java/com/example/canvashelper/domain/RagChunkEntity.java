package com.example.canvashelper.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "rag_chunks", uniqueConstraints = @UniqueConstraint(columnNames = {"sourceType", "sourceId"}))
public class RagChunkEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceType;
    private String sourceId;
    private String title;
    @Lob
    private String content;
    private String contentHash;
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate(){ updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
