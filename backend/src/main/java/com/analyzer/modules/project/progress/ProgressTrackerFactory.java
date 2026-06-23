package com.analyzer.modules.project.progress;

import com.analyzer.infrastructure.websocket.WebSocketEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProgressTrackerFactory {
    private final WebSocketEventService eventService;

    public ProgressTracker create(String topic, String taskId) {
        return new ProgressTracker(eventService, topic, taskId);
    }
}
