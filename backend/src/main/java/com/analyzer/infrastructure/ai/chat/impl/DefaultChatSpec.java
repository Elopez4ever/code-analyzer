package com.analyzer.infrastructure.ai.chat.impl;

import com.analyzer.infrastructure.ai.chat.ChatSpec;
import reactor.core.publisher.Flux;

class DefaultChatSpec implements ChatSpec {

    private final ChatServiceImpl chatService;

    private String model;
    private String systemPrompt;
    private Double temperature;
    private Integer maxTokens;

    DefaultChatSpec(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @Override
    public ChatSpec model(String model) {
        this.model = model;
        return this;
    }

    @Override
    public ChatSpec system(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    @Override
    public ChatSpec temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    @Override
    public ChatSpec maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    @Override
    public String call(String userMessage) {
        return chatService.chat(buildRequest(userMessage)).getContent();
    }

    @Override
    public Flux<String> stream(String userMessage) {
        return chatService.chatStream(buildRequest(userMessage));
    }

    private ChatRequest buildRequest(String userMessage) {
        return ChatRequest.builder()
                .model(model)
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
}