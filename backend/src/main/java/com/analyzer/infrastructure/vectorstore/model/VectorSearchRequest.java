package com.analyzer.infrastructure.vectorstore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchRequest {
    private String projectId;
    private float[] queryVector;
    private int topK;
    private String language;
    private String chunkType;
    private String filePath;
    private Map<String, String> metadataFilters;
}
