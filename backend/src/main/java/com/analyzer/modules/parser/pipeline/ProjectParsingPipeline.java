package com.analyzer.modules.parser.pipeline;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.infrastructure.ai.embedding.EmbeddingService;
import com.analyzer.infrastructure.vectorstore.VectorStoreService;
import com.analyzer.infrastructure.vectorstore.entity.CodeChunkVector;
import com.analyzer.modules.parser.pipeline.chunker.ChunkerRouter;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 向量化存储一个项目
 *  parse -> chunk -> enrich -> embed -> embed -> store
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectParsingPipeline {
    private final List<ProjectParser> parsers;
    private final ChunkerRouter chunkerRouter;
    private final EnricherPipeline enricherPipeline;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStore;
    private static final int BATCH_SIZE = 20;

    public void execute(String projectId, String projectPath) {
        Path root = Path.of(projectPath);

        // 将项目拆分为 SourceFiles
        ProjectParser parser = parsers.stream()
                .filter(p -> p.supports(root))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到匹配项目的parser!"));
        List<SourceFile> sourceFiles = parser.parse(root);

        // 分块
        List<CodeChunk> chunks = sourceFiles.stream()
                .flatMap(file -> chunkerRouter.getChunker(file.getLanguage())
                        .chunk(file, projectId).stream())
                .toList();
        log.info("项目 {} 生成 {} 个代码块", projectId, chunks.size());

        //元数据增强
        List<CodeChunk> enrichedChunks = enricherPipeline.enrich(chunks);

        // 4. embed + store: 批量向量化并存储
        embedAndStore(enrichedChunks);
        log.info("项目 {} 索引完成, 共存储 {} 个向量", projectId, enrichedChunks.size());
    }

    private void embedAndStore(List<CodeChunk> chunks) {
        for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
            List<CodeChunk> batch = chunks.subList(i, Math.min(i + BATCH_SIZE, chunks.size()));

            List<String> texts = batch.stream()
                    .map(chunk -> chunk.getSummary() != null ? chunk.getSummary() : chunk.getContent())
                    .toList();

            List<float[]> embeddings = embeddingService.embedBatch(texts);

            List<CodeChunkVector> vectors = new ArrayList<>(batch.size());
            for (int j = 0; j < batch.size(); j++) {
                CodeChunk chunk = batch.get(j);
                vectors.add(CodeChunkVector.builder()
                        .id(chunk.getId())
                        .projectId(chunk.getProjectId())
                        .content(chunk.getContent())
                        .summary(chunk.getSummary())
                        .embedding(embeddings.get(j))
                        .metadata(chunk.getMetadata())
                        .keywords(chunk.getKeywords())
                        .build());
            }

            vectorStore.storeBatch(vectors);
            log.debug("已存储批次 {}/{}", i / BATCH_SIZE + 1, (chunks.size() + BATCH_SIZE - 1) / BATCH_SIZE);
        }
    }
}
