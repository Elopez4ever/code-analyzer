package com.analyzer.modules.parser.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SourceFile {
    private String relativePath;
    private String content;
    private String language;
    private Map<String, String> metadata;
}
