package com.analyzer.modules.task.service;

import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.modules.task.domain.TaskRecord;
import com.analyzer.modules.task.domain.TaskStatus;
import com.analyzer.modules.task.domain.TaskStage;
import com.analyzer.modules.task.event.TaskStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final ApplicationEventPublisher eventPublisher;
    private final AppConfigProperties appConfig;

    // 内存存任务状态，后续可换成数据库
    private final Map<String, TaskRecord> taskStore = new ConcurrentHashMap<>();

    /**
     * 创建任务并触发流水线，ProjectService 只调这一个方法。
     */
    public String submit(String projectId, String localPath) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        TaskRecord record = TaskRecord.builder()
                .taskId(taskId)
                .projectId(projectId)
                .status(TaskStatus.RUNNING)
                .stage(TaskStage.CREATED)
                .progress(0)
                .message("任务已提交")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        taskStore.put(taskId, record);
        log.info("[Task] 任务创建: taskId={}, projectId={}", taskId, projectId);

        eventPublisher.publishEvent(new TaskStartedEvent(taskId, projectId, localPath));
        return taskId;
    }

    public void updateStage(String taskId, TaskStage stage, int progress, String message) {
        TaskRecord record = taskStore.get(taskId);
        if (record == null) return;
        record.setStage(stage);
        record.setProgress(progress);
        record.setMessage(message);
        record.setUpdatedAt(Instant.now());
    }

    public void markDone(String taskId) {
        TaskRecord record = taskStore.get(taskId);
        if (record == null) return;
        record.setStatus(TaskStatus.DONE);
        record.setStage(TaskStage.DONE);
        record.setProgress(100);
        record.setMessage("解析完成");
        record.setUpdatedAt(Instant.now());
    }

    public void markFailed(String taskId, String message) {
        TaskRecord record = taskStore.get(taskId);
        if (record == null) return;
        record.setStatus(TaskStatus.FAILED);
        record.setStage(TaskStage.FAILED);
        record.setProgress(-1);
        record.setMessage(message);
        record.setUpdatedAt(Instant.now());
    }

    public TaskRecord getTask(String taskId) {
        return taskStore.get(taskId);
    }

    /**
     * 返回配置的 WebSocket topic 前缀
     */
    public String getTopic() {
        return appConfig.getTopic();
    }
}
