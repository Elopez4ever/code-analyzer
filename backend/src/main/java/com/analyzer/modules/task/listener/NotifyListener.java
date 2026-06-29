package com.analyzer.modules.task.listener;

import com.analyzer.infrastructure.websocket.WebSocketEventService;
import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.persistence.service.ProjectPersistenceService;
import com.analyzer.modules.task.event.IngestCompletedEvent;
import com.analyzer.modules.task.event.ParseCompletedEvent;
import com.analyzer.modules.task.event.TaskFailedEvent;
import com.analyzer.modules.task.event.TaskStartedEvent;
import com.analyzer.modules.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听所有任务事件，统一推 WebSocket 通知前端，并更新项目状态。
 * 不加 @Async，保证推送顺序和状态一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyListener {

    private final WebSocketEventService webSocketEventService;
    private final TaskService taskService;
    private final ProjectPersistenceService persistenceService;

    @EventListener
    public void onTaskStarted(TaskStartedEvent e) {
        push(e.getProjectId(), e.getTaskId(), 0, "PARSING", "任务已启动，开始解析代码", "PROCESSING");
    }

    @EventListener
    public void onParseCompleted(ParseCompletedEvent e) {
        push(e.getProjectId(), e.getTaskId(), 50, "INGESTING", "代码解析完成，正在写入知识库", "PROCESSING");
    }

    @EventListener
    public void onIngestCompleted(IngestCompletedEvent e) {
        taskService.markDone(e.getTaskId());
        persistenceService.updateStatus(e.getProjectId(), ProjectStatus.READY);
        push(e.getProjectId(), e.getTaskId(), 100, "DONE",
                "知识库构建完成，共处理 " + e.getChunkCount() + " 个代码块", "DONE");
    }

    @EventListener
    public void onTaskFailed(TaskFailedEvent e) {
        taskService.markFailed(e.getTaskId(), e.getErrorMessage());
        persistenceService.updateStatus(e.getProjectId(), ProjectStatus.FAILED);
        push(e.getProjectId(), e.getTaskId(), -1, e.getFailedStage(),
                "任务失败: " + e.getErrorMessage(), "ERROR");
    }

    private void push(String projectId, String taskId, int progress,
                      String stage, String message, String status) {
        webSocketEventService.sendTaskProgress(
                taskService.getTopic(), taskId, projectId, progress, stage, message, status);
    }
}
