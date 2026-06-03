package com.analyzer.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "analyzer.ai")
@Data
public class AIClientProperties {
    private String defaultChatModel = "gpt-4o";
    private String embeddingModel = "text-embedding-3-small";
    private Map<String, ModelConfig> models = new HashMap<>();
    @Data
    public static class ModelConfig {
        private String provider;   // openai, ollama, azure ...
        private String apiKey;
        private String baseUrl;
        private String modelName;
        private Double temperature = 0.7;
        private Integer maxTokens = 2048;
    }
}
