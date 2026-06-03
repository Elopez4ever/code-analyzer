package com.analyzer.infrastructure.ai.chat;

import reactor.core.publisher.Flux;

import java.util.Set;

public interface ChatService {
    /** 使用默认模型 */
    String chat(String userMessage);

    /** 指定模型 + system prompt */
    String chat(String modelKey, String systemPrompt, String userMessage);

    /** 流式输出 */
    Flux<String> chatStream(String modelKey, String systemPrompt, String userMessage);

    /** 获取可用模型列表 */
    Set<String> availableModels();
}
