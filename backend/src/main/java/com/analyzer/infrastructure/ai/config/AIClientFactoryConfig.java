package com.analyzer.infrastructure.ai.config;

import com.analyzer.common.config.AIClientProperties;
import com.analyzer.infrastructure.ai.AIClient;
import com.analyzer.infrastructure.ai.AIClientFactory;
import com.analyzer.infrastructure.ai.impl.SpringAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AIClientFactoryConfig {
    private final Map<String, ChatClient> chatClients;
    private final AIClientProperties properties;
    @Bean
    public AIClientFactory aiClientFactory() {
        Map<String, AIClient> clients = new HashMap<>();
        chatClients.forEach((name, chatClient) -> {
            clients.put(name, new SpringAIClient(chatClient));
        });
        return new AIClientFactory(clients, properties.getDefaultClient());
    }
}