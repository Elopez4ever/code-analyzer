package com.analyzer.modules.parser.pipeline;

import com.analyzer.infrastructure.embedding.EmbeddingService;
import com.analyzer.infrastructure.vectorstore.VectorStore;
import com.analyzer.modules.parser.pipeline.chunker.ChunkerRouter;
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
    private final ChunkerRouter chunkerRouter;
    private final EnricherPipeline enricherPipeline;
    private static final String COLLECTION = "code_chunks";
    private static final int BATCH_SIZE = 20;

    public void execute(String projectId, String projectPath) {
        
    }
}
