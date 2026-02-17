package com.example.canvashelper.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "embeddings")
public class EmbeddingEntity {
    @Id
    private Long chunkId;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "chunk_id")
    private RagChunkEntity chunk;
    private String model;
    @Lob
    private String vectorJson;
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate(){ updatedAt = OffsetDateTime.now(); }

    public Long getChunkId() { return chunkId; }
    public RagChunkEntity getChunk() { return chunk; }
    public void setChunk(RagChunkEntity chunk) { this.chunk = chunk; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVectorJson() { return vectorJson; }
    public void setVectorJson(String vectorJson) { this.vectorJson = vectorJson; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
