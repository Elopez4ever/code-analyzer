package com.analyzer.modules.task.event;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import lombok.Getter;

import java.util.List;

@Getter
public class ParseCompletedEvent extends BaseTaskEvent {
    private final List<CodeChunk> chunks;

    public ParseCompletedEvent(String taskId, String projectId, List<CodeChunk> chunks) {
        super(taskId, projectId);
        this.chunks = List.copyOf(chunks);
    }
}
