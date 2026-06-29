package com.analyzer.infrastructure.ai.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class ModelProperties {
    private String defaultModel = "gpt-4o";
    private int timeoutSeconds = 60;
    private Map<String, ModelInfo> models = new LinkedHashMap<>();
}
