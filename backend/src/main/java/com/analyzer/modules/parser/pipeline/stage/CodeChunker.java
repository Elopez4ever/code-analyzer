package com.analyzer.modules.parser.pipeline.stage;

import com.analyzer.infrastructure.entity.CodeChunk;
import com.analyzer.modules.parser.model.SourceFile;

import java.util.List;

/**
 * 职责：将单个源文件拆分为语义代码块
 * 不同语言有不同的分块策略
 */
public interface CodeChunker {
    boolean supports(String language);
    List<CodeChunk> chunk(SourceFile sourceFile, String projectId);
}