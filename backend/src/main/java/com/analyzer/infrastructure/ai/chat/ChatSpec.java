package com.analyzer.infrastructure.ai.chat;

import reactor.core.publisher.Flux;

public interface ChatSpec {
    ChatSpec model(String model);
    ChatSpec system(String systemPrompt);
    ChatSpec temperature(double temperature);
    ChatSpec maxTokens(int maxTokens);
    String call(String userMessage);
    Flux<String> stream(String userMessage);
}
