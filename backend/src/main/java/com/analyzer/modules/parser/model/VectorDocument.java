package com.analyzer.modules.parser.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VectorDocument {
    private String id;
    private float[] vector;
    private Map<String, Object> metadata;
}
