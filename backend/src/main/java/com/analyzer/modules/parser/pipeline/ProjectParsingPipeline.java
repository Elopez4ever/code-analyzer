package com.analyzer.modules.parser.pipeline;

import com.analyzer.modules.parser.pipeline.stage.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectParsingPipeline {
    private final List<ProjectParser> parsers;
    private final List<CodeChunker> chunkers;
    private final List<ChunkEnricher> enrichers;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private static final String COLLECTION = "code_chunks";
    private static final int BATCH_SIZE = 20;

    @PostConstruct
    public void init() {
        if (!vectorStore.collectionExists(COLLECTION)) {
            vectorStore.createCollection(COLLECTION, embeddingService.dimensions());
        }
    }

    public void execute(String projectId, String projectPath) {

    }
}
