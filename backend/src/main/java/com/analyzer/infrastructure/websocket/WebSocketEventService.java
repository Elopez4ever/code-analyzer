package com.analyzer.infrastructure.websocket;

import com.analyzer.infrastructure.websocket.entity.ProgressMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketEventService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 向指定 topic 推送进度（clone/unzip 阶段使用）
     */
    public void sendProgress(String topic, String taskId, String step, int percent, String message) {
        ProgressMessage msg = ProgressMessage.builder()
                .taskId(taskId)
                .step(step)
                .percent(percent)
                .message(message)
                .build();
        messagingTemplate.convertAndSend(topic, msg);
    }

    /**
     * 推送 Task 级别的进度通知（parse → ingest → done/failed）
     */
    public void sendTaskProgress(String topic, String taskId, String projectId,
                                  int progress, String stage, String message, String status) {
        ProgressMessage msg = ProgressMessage.builder()
                .taskId(taskId)
                .projectId(projectId)
                .step(stage)
                .percent(progress)
                .message(message)
                .status(status)
                .build();
        messagingTemplate.convertAndSend(topic, msg);
    }
}
