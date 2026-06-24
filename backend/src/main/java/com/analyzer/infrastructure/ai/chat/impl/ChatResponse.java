package com.analyzer.infrastructure.ai.chat.domain;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
class ChatResponse {
    private String content;
    private String model;
    private long latencyMs;
}