package com.analyzer.modules.parser.pipeline.domain;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SourceFile {
    private String relativePath;
    private String absolutePath;
    private String content;
    private FileLanguage language;
    private long fileSize;
}
