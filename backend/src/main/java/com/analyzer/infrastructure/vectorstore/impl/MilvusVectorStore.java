package com.analyzer.infrastructure.vectorstore.impl;

import com.analyzer.infrastructure.vectorstore.VectorStore;
import com.analyzer.infrastructure.vectorstore.entity.VectorDocument;
import com.analyzer.infrastructure.vectorstore.entity.VectorSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MilvusVectorStore implements VectorStore {
    @Override
    public void createCollection(String collection, int dimension) {

    }

    @Override
    public void store(String collection, VectorDocument document) {

    }

    @Override
    public void storeBatch(String collection, List<VectorDocument> documents) {

    }

    @Override
    public List<VectorSearchResult> search(String collection, float[] queryVector, int topK) {
        return List.of();
    }

    @Override
    public List<VectorSearchResult> search(String collection, float[] queryVector, int topK, Map<String, Object> filter) {
        return List.of();
    }

    @Override
    public void deleteByField(String collection, String field, String value) {

    }

    @Override
    public boolean collectionExists(String collection) {
        return false;
    }
}
