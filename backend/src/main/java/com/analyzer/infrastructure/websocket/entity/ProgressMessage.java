package com.analyzer.infrastructure.websocket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressMessage {
    private String taskId;
    private String step;
    private int percent;
    private String message;
}
