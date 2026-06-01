package com.analyzer.modules.parser.pipeline.detector.enricher.extractor;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;

import java.util.Set;

public interface StructureExtractor {
    Set<CodeChunkType> supportedTypes();
    void extract(CodeChunk chunk);
}
