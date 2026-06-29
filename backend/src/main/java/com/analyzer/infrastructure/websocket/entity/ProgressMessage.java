package com.analyzer.infrastructure.websocket.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressMessage {
    private String taskId;
    private String projectId;
    private String step;
    private int percent;
    private String message;
    private String status;  // PROCESSING / DONE / ERROR，仅 Task 级别通知使用
}
