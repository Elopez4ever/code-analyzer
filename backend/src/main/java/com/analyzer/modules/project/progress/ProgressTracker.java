package com.analyzer.modules.project.progress;

import com.analyzer.infrastructure.websocket.WebSocketEventService;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class ProgressTracker {
    private final WebSocketEventService eventService;
    private final String topic;
    private final String taskId;

    public void update(String step, int percent, String message) {
        eventService.sendProgress(topic, taskId, step, percent, message);
    }

    public <T> T step(String step, int startPercent, int endPercent, String desc, Supplier<T> action) {
        update(step, startPercent, desc);
        T result = action.get();
        update(step, endPercent, desc + " ✓");
        return result;
    }

    public void step(String step, int startPercent, int endPercent, String desc, Runnable action) {
        update(step, startPercent, desc);
        action.run();
        update(step, endPercent, desc + " ✓");
    }

    public void error(String message) {
        update("ERROR", 0, message);
    }

    /**
     * 返回一个通用的逐项回调
     */
    public ItemProgressCallback itemCallback(String step, int startPercent, int endPercent) {
        return (itemName, index, total) -> {
            int percent = startPercent + (endPercent - startPercent) * index / Math.max(total, 1);
            update(step, percent, itemName);
        };
    }
}
