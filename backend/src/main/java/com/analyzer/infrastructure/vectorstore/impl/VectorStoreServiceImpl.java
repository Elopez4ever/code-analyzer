package com.analyzer.infrastructure.vectorstore.impl;

import com.analyzer.infrastructure.vectorstore.VectorStoreService;
import com.analyzer.infrastructure.vectorstore.entity.CodeChunkVector;
import com.analyzer.infrastructure.vectorstore.mapper.CodeChunkVectorMapper;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchRequest;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {
    private final CodeChunkVectorMapper mapper;
    private static final int BATCH_SIZE = 50;
    @Override
    @Transactional
    public void storeBatch(List<CodeChunkVector> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        // 分批写入，避免单条 SQL 太大
        for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
            List<CodeChunkVector> batch = chunks.subList(i, Math.min(i + BATCH_SIZE, chunks.size()));
            mapper.batchUpsert(batch);
        }
        log.debug("stored {} chunks in {} batches", chunks.size(),
                (chunks.size() + BATCH_SIZE - 1) / BATCH_SIZE);
    }
    @Override
    public List<VectorSearchResult> search(VectorSearchRequest request) {
        String vectorStr = toVectorString(request.getQueryVector());
        boolean hasFilter = request.getLanguage() != null
                || request.getChunkType() != null
                || request.getFilePath() != null
                || (request.getMetadataFilters() != null && !request.getMetadataFilters().isEmpty());
        if (hasFilter) {
            return mapper.searchWithFilter(
                    vectorStr,
                    request.getProjectId(),
                    request.getTopK(),
                    request.getLanguage(),
                    request.getChunkType(),
                    request.getFilePath(),
                    request.getMetadataFilters()
            );
        }
        return mapper.searchSimilar(vectorStr, request.getProjectId(), request.getTopK());
    }
    @Override
    @Transactional
    public void deleteByProject(String projectId) {
        mapper.deleteByProjectId(projectId);
        log.info("deleted all chunks for project: {}", projectId);
    }
    @Override
    @Transactional
    public void deleteByProjectAndFile(String projectId, String filePath) {
        mapper.deleteByProjectAndFile(projectId, filePath);
        log.debug("deleted chunks for project: {}, file: {}", projectId, filePath);
    }
    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}