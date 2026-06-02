package com.analyzer.infrastructure.vectorstore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResult {
    private String id;
    private String projectId;
    private String filePath;
    private String language;
    private String chunkType;
    private Integer startLine;
    private Integer endLine;
    private String content;
    private String summary;
    private Map<String, String> metadata;
    private List<String> keywords;
    private double score;
}