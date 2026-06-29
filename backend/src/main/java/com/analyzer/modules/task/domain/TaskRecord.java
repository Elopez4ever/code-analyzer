package com.analyzer.modules.task.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TaskRecord {
    private String taskId;
    private String projectId;
    private TaskStatus status;
    private TaskStage stage;
    private String message;
    private int progress;       // 0-100
    private Instant createdAt;
    private Instant updatedAt;
}