package com.analyzer.modules.task.event;

import lombok.Getter;

@Getter
public class TaskStartedEvent extends BaseTaskEvent {
    private final String localPath;

    public TaskStartedEvent(String taskId, String projectId, String localPath) {
        super(taskId, projectId);
        this.localPath = localPath;
    }
}
