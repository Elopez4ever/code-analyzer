package com.analyzer.modules.knowledgebase;

import com.analyzer.infrastructure.ai.embedding.EmbeddingService;
import com.analyzer.infrastructure.vectorstore.VectorStoreService;
import com.analyzer.infrastructure.vectorstore.entity.CodeChunkVector;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库服务，负责将解析后的 {@link CodeChunk} 完整入库。
 * 当前阶段内部直接调用 EnricherPipeline / EmbeddingService / VectorStoreService，
 * 等这套流程稳定后再拆成 EnrichmentService、EmbeddingService 等细粒度组件。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    private final EnricherPipeline enricherPipeline;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStore;
    private static final int BATCH_SIZE = 20;

    /**
     * 将 chunk 列表增补元数据、向量化并写入向量库。
     *
     * @param projectId 项目 ID
     * @param chunks    解析产生的原始 chunk 列表（可能尚未带 enrichment 字段）
     */
    public void ingest(String projectId, List<CodeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("项目 {} 无 chunk 需要入库", projectId);
            return;
        }

        // 1. 元数据增强
        List<CodeChunk> enrichedChunks = enricherPipeline.enrich(chunks);

        // 2. 向量化 + 存储
        embedAndStore(enrichedChunks);

        log.info("项目 {} 知识入库完成, 共存储 {} 个向量", projectId, enrichedChunks.size());
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
