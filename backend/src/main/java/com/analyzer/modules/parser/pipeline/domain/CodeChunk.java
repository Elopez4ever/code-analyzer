package com.analyzer.modules.parser.pipeline.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CodeChunk {
    private String id;
    private String projectId;
    private String filePath;
    private FileLanguage language;
    private CodeChunkType chunkType;       // CLASS, METHOD, CONFIG_BLOCK, SQL_STATEMENT, etc.
    private String content;
    private int startLine;
    private int endLine;

    // --- enricher 需要填充的字段 ---
    private String className;
    private String methodName;
    private String packageName;
    private List<String> keywords;
    private String summary;           // 自然语言摘要
    private HashMap<String, String> metadata;
}
