package com.analyzer.modules.task.domain;

public enum TaskStage {
    CREATED,
    PARSING,
    INGESTING,
    DONE,
    FAILED
}