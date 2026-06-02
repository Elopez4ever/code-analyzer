package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;
import com.analyzer.modules.parser.pipeline.enricher.extractor.StructureExtractor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 为 CodeChunk 填充结构化元数据：包名、类名、方法签名、修饰符、所属模块路径等
 */
@Component
@AllArgsConstructor
public class MetadataEnricher implements ChunkEnricher {

    private final List<StructureExtractor> extractors;

    @Override
    public int order() {
        return EnricherPriority.METADATA.getOrder();
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
       extractors.stream()
               .filter(e -> e.supportedTypes().contains(chunk.getChunkType()))
               .forEach(e -> e.extract(chunk));
       return chunk;
    }
}
