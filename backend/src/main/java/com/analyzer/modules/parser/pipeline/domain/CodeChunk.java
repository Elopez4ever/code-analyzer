package com.analyzer.modules.parser.pipeline.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CodeChunk {
    private String id;
    private String projectId;
    private String filePath;
    private String content;
    private CodeChunkType type;
    private String language;
    private String className;
    private String methodName;
    private String packageName;
    private int startLine;
    private int endLine;
    private Map<String, String> metadata;
}
