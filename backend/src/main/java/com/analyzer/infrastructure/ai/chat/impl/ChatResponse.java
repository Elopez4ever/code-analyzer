package com.analyzer.infrastructure.ai.chat.impl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ChatResponse {
    private String content;
    private String model;
    private long latencyMs;
}