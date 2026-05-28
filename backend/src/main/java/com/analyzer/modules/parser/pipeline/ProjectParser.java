package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.model.SourceFile;

import java.util.List;

/**
 * 职责：识别项目类型，提取源文件列表
 * 不做分块，只做文件级别的提取和过滤
 */
public interface ProjectParser {
    boolean supports(String projectPath);
    /**
     * 提取项目中需要解析的源文件
     */
    List<SourceFile> extractFiles(String projectPath);
}
