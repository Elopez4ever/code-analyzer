package com.analyzer.modules.task.event;

import lombok.Getter;

@Getter
public class TaskFailedEvent extends BaseTaskEvent {
    private final String failedStage;
    private final String errorMessage;

    public TaskFailedEvent(String taskId, String projectId, String failedStage, Throwable cause) {
        super(taskId, projectId);
        this.failedStage = failedStage;
        this.errorMessage = cause.getMessage();
    }
}
