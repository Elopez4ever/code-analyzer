package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class EnricherPipeline {
    private final List<ChunkEnricher> enrichers;
    public EnricherPipeline(List<ChunkEnricher> enrichers) {
        this.enrichers = enrichers.stream()
                .sorted(Comparator.comparingInt(ChunkEnricher::order)
                        .thenComparing(e -> e.getClass().getSimpleName())) // 同order按类字母排序, 保证幂等性
                .toList();
    }

    public List<CodeChunk> enrich(List<CodeChunk> chunks) {
        return chunks.stream().map(this::enrich).toList();
    }

    public CodeChunk enrich(CodeChunk chunk) {
        CodeChunk result = chunk;
        for (ChunkEnricher enricher : enrichers) {
            if (supportsLanguage(enricher, result.getLanguage())) {
                result = enricher.enrich(result);
            }
        }
        return result;
    }

    private boolean supportsLanguage(ChunkEnricher enricher, FileLanguage language) {
        List<FileLanguage> supported = enricher.supportedLanguages();
        return supported == null || supported.contains(language);
    }
}
