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
     * 向指定 topic 推送进度
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
}
