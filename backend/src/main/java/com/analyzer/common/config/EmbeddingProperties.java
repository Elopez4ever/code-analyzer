package com.analyzer.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "analyzer.embedding")
public class EmbeddingProperties {
    private Map<String, ModelConfig> models;
    private String defaultModel = "openai";
    @Data
    public static class ModelConfig {
        private String apiUrl;
        private String apiKey;
        private String model;
        private int maxBatchSize = 20;
        private int dimensions = 1536;
    }
}
