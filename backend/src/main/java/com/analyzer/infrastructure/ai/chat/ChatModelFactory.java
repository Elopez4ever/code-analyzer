package com.analyzer.infrastructure.ai.chat;

import com.analyzer.common.config.AIClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatModelFactory {
    private final AIClientProperties properties;
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();

    public ChatModel getDefaultModel() {
        return getModel(properties.getDefaultChatModel());
    }

    public ChatModel getModel(String modelKey) {
        if (!properties.getModels().containsKey(modelKey)) {
            throw new IllegalArgumentException("未配置的模型: " + modelKey);
        }
        return chatModelCache.computeIfAbsent(modelKey, this::createChatModel);
    }

    public Set<String> availableModels() {
        return properties.getModels().keySet();
    }

    private ChatModel createChatModel(String modelKey) {
        AIClientProperties.ModelConfig config = properties.getModels().get(modelKey);

        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModelName())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }
}
