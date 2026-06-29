package com.analyzer.modules.task.listener;

import com.analyzer.modules.parser.pipeline.ParsePipeline;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.task.domain.TaskStage;
import com.analyzer.modules.task.event.ParseCompletedEvent;
import com.analyzer.modules.task.event.TaskFailedEvent;
import com.analyzer.modules.task.event.TaskStartedEvent;
import com.analyzer.modules.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParseListener {

    private final ParsePipeline parsePipeline;
    private final TaskService taskService;
    private final ApplicationEventPublisher eventPublisher;

    @Async("taskExecutor")
    @EventListener
    public void onTaskStarted(TaskStartedEvent event) {
        String taskId = event.getTaskId();
        String projectId = event.getProjectId();

        log.info("[ParseListener] 开始解析, taskId={}", taskId);
        taskService.updateStage(taskId, TaskStage.PARSING, 10, "正在解析代码结构");

        try {
            List<CodeChunk> chunks = parsePipeline.execute(projectId, event.getLocalPath());
            log.info("[ParseListener] 解析完成, {} chunks", chunks.size());

            taskService.updateStage(taskId, TaskStage.PARSING, 50, "代码解析完成");
            eventPublisher.publishEvent(new ParseCompletedEvent(taskId, projectId, chunks));

        } catch (Exception e) {
            log.error("[ParseListener] 解析失败, taskId={}", taskId, e);
            eventPublisher.publishEvent(new TaskFailedEvent(taskId, projectId, "PARSING", e));
        }
    }
}
