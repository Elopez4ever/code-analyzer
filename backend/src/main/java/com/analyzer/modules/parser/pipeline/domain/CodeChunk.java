package com.analyzer.modules.parser.pipeline.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CodeChunk {
    private String id;
    private String projectId;
    private String filePath;
    private FileLanguage language;
    private String chunkType;       // CLASS, METHOD, CONFIG_BLOCK, SQL_STATEMENT, etc.
    private String name;            // 类名、方法名、配置 key 等
    private String content;
    private int startLine;
    private int endLine;
    private String summary;         // enricher 填充
    private Map<String, String> metadata;
}
