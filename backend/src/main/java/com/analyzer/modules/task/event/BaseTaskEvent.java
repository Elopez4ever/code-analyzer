package com.analyzer.modules.task.event;

import lombok.Getter;

import java.time.Instant;

@Getter
public class BaseTaskEvent {
    private final String taskId;
    private final String projectId;
    private final Instant occurredAt = Instant.now();
    protected BaseTaskEvent(String taskId, String projectId) {
        this.taskId = taskId;
        this.projectId = projectId;
    }
}
