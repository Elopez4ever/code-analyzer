package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;

import java.util.List;

/**
 * 职责：对代码块补充元数据（可选阶段）
 * 比如生成摘要、打标签、提取依赖关系
 */
public interface ChunkEnricher {
    /**
     * 声明支持的语言，null 表示支持所有语言
     */
    List<FileLanguage> supportedLanguages();
    /**
     * enricher 执行顺序，数字越小越先执行
     */
    int order();
    CodeChunk enrich(CodeChunk chunk);
}
