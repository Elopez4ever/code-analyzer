package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;

import java.util.List;

/**
 * 职责：将单个源文件拆分为语义代码块
 * 不同语言有不同的分块策略
 */
public interface CodeChunker {
    boolean supports(String language);
    List<CodeChunk> chunk(SourceFile sourceFile, String projectId);
}