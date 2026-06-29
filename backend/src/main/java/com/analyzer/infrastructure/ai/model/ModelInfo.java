package com.analyzer.infrastructure.ai.model;

import lombok.Data;

@Data
public class ModelInfo {
    private String name;
    private String description;
    private int maxTokens = 2048;
    private double temperature = 0.7;
}