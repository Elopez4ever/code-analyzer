package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;

/**
 * 为 CodeChunk 填充结构化元数据：包名、类名、方法签名、修饰符、所属模块路径等
 */
public class MetadataEnricher implements ChunkEnricher {

    @Override
    public int order() {
        return EnricherPriority.METADATA.getOrder();
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        return null;
    }
}
