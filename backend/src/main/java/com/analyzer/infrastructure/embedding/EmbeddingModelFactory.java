package com.analyzer.infrastructure.embedding;

import com.analyzer.common.config.EmbeddingProperties;
import com.analyzer.infrastructure.embedding.impl.OpenAiEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class EmbeddingModelFactory {
    private final EmbeddingProperties properties;
    private final RestTemplate restTemplate;
    private final Map<String, EmbeddingService> serviceCache = new ConcurrentHashMap<>();
    public EmbeddingService getService() {
        return getService(properties.getDefaultModel());
    }
    public EmbeddingService getService(String modelName) {
        return serviceCache.computeIfAbsent(modelName, name -> {
            EmbeddingProperties.ModelConfig config = properties.getModels().get(name);
            if (config == null) {
                throw new IllegalArgumentException("Unknown embedding model: " + name);
            }
            return new OpenAiEmbeddingService(restTemplate, config);
        });
    }
}