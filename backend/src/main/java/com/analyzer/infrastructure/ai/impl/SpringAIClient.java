package com.analyzer.infrastructure.ai.impl;

import com.analyzer.infrastructure.ai.AIClient;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;

@AllArgsConstructor
public class SpringAIClient implements AIClient {

    private final ChatClient client;

    @Override
    public String chat(String prompt) {
        return chat("default", prompt);
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return client.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
