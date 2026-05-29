package com.analyzer.infrastructure.ai.config;

import com.analyzer.common.config.AIClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 将AI Clients注册为Bean
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final AIClientProperties properties;

    @Bean
    public Map<String, ChatClient> chatClients() {
        HashMap<String, ChatClient> clients = new HashMap<>();
        properties.getClients().forEach((name, config) -> {
            OpenAiApi api = OpenAiApi.builder()
                    .baseUrl(config.getBaseUrl())
                    .apiKey(config.getApiKey())
                    .build();
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(config.getModel())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .build();
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(api)
                    .defaultOptions(options)
                    .build();
            ChatClient client = ChatClient.builder(chatModel).build();
            clients.put(name, client);
            log.info("ChatClient注册成功: {} -> model: {}, baseUrl ;{}", name, config.getModel(), config.getBaseUrl());
        });
        return clients;
    }
}
