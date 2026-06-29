package com.analyzer.infrastructure.ai.embedding;

import java.util.List;

public interface EmbeddingService {
    float[] embed(String text);
    List<float[]> embedBatch(List<String> texts);
    int dimensions();
}