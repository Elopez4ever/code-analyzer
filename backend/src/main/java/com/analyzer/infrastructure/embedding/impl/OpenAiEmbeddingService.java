package com.analyzer.infrastructure.embedding.impl;

import com.analyzer.infrastructure.embedding.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {
    @Override
    public float[] embed(String text) {
        return new float[0];
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        return List.of();
    }

    @Override
    public int dimensions() {
        return 0;
    }
}
