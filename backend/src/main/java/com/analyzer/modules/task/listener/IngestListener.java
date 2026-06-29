package com.analyzer.modules.task.listener;

import com.analyzer.modules.knowledgebase.KnowledgeBaseService;
import com.analyzer.modules.task.domain.TaskStage;
import com.analyzer.modules.task.event.IngestCompletedEvent;
import com.analyzer.modules.task.event.ParseCompletedEvent;
import com.analyzer.modules.task.event.TaskFailedEvent;
import com.analyzer.modules.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestListener {

    private final KnowledgeBaseService knowledgeBaseService;
    private final TaskService taskService;
    private final ApplicationEventPublisher eventPublisher;

    @Async("taskExecutor")
    @EventListener
    public void onParseCompleted(ParseCompletedEvent event) {
        String taskId = event.getTaskId();
        String projectId = event.getProjectId();

        log.info("[IngestListener] 开始入库, taskId={}", taskId);
        taskService.updateStage(taskId, TaskStage.INGESTING, 55, "正在向量化并写入知识库");

        try {
            knowledgeBaseService.ingest(projectId, event.getChunks());

            taskService.updateStage(taskId, TaskStage.INGESTING, 95, "知识库写入完成");
            eventPublisher.publishEvent(
                    new IngestCompletedEvent(taskId, projectId, event.getChunks().size())
            );

        } catch (Exception e) {
            log.error("[IngestListener] 入库失败, taskId={}", taskId, e);
            eventPublisher.publishEvent(new TaskFailedEvent(taskId, projectId, "INGESTING", e));
        }
    }
}
