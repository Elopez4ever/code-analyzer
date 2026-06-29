package com.analyzer.modules.task.event;

import lombok.Getter;

@Getter
public class IngestCompletedEvent extends BaseTaskEvent {
    private final int chunkCount;

    public IngestCompletedEvent(String taskId, String projectId, int chunkCount) {
        super(taskId, projectId);
        this.chunkCount = chunkCount;
    }
}
