package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;

import java.util.List;

public class MetadataEnricher implements ChunkEnricher {
    @Override
    public List<FileLanguage> supportedLanguages() {
        return List.of();
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        return null;
    }
}
