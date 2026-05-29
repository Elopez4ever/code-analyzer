package com.analyzer.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "analyzer.ai")
public class AIClientProperties {
    /**
     * 多模型配置
     */
    private Map<String, ModelConfig> clients;

    /**
     * 默认模型
     */
    private String defaultClient = "deepseek";

    /**
     * 模型配置
     */
    @Data
    public static class ModelConfig {
        private String baseUrl;
        private String apiKey;
        private String model;
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
    }
}
