package com.analyzer.infrastructure.ai.embedding.impl;

import com.analyzer.infrastructure.ai.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {
        EmbeddingResponse response = embeddingModel.call(
                new EmbeddingRequest(List.of(text), null));
        return response.getResult().getOutput();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingResponse response = embeddingModel.call(
                new EmbeddingRequest(texts, null));
        return response.getResults().stream()
                .map(Embedding::getOutput)
                .toList();
    }

    @Override
    public int dimensions() {
        return embeddingModel.dimensions();
    }
}