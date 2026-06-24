package com.analyzer.infrastructure.ai.chat.domain;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
class ChatRequest {
    private String model;
    private String systemPrompt;
    private String userMessage;
    private Double temperature;
    private Integer maxTokens;
}