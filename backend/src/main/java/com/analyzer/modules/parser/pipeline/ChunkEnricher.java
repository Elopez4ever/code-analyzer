package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;

import java.util.List;

/**
 * 职责：对代码块补充元数据（可选阶段）
 * 比如生成摘要、打标签、提取依赖关系
 */
public interface ChunkEnricher {
    List<CodeChunk> enrich(List<CodeChunk> chunks);
}
