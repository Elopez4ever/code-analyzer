package com.analyzer.infrastructure.vectorstore.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VectorSearchResult {
    private String id;
    private float score;
    private Map<String, Object> metadata;
}