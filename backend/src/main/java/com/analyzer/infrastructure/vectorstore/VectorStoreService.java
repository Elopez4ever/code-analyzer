package com.analyzer.infrastructure.vectorstore;

import com.analyzer.infrastructure.vectorstore.entity.CodeChunkVector;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchRequest;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchResult;

import java.util.List;

/**
 * 职责：向量存储与检索
 */
public interface VectorStoreService {
    void storeBatch(List<CodeChunkVector> chunks);
    List<VectorSearchResult> search(VectorSearchRequest request);
    void deleteByProject(String projectId);
    void deleteByProjectAndFile(String projectId, String filePath);
}