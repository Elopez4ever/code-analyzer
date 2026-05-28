package com.analyzer.infrastructure.vectorstore;

import com.analyzer.infrastructure.vectorstore.model.VectorDocument;
import com.analyzer.infrastructure.vectorstore.model.VectorSearchResult;

import java.util.List;
import java.util.Map;

/**
 * 职责：向量的存储和检索
 */
public interface VectorStore {
    void createCollection(String collection, int dimension);
    void store(String collection, VectorDocument document);
    void storeBatch(String collection, List<VectorDocument> documents);
    List<VectorSearchResult> search(String collection, float[] queryVector, int topK);
    List<VectorSearchResult> search(String collection, float[] queryVector, int topK, Map<String, Object> filter);
    void deleteByField(String collection, String field, String value);
    boolean collectionExists(String collection);
}