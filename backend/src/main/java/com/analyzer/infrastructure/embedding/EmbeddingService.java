package com.analyzer.infrastructure.embedding;

import java.util.List;

/**
 * 职责：文本向量化
 */
public interface EmbeddingService {

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);
}
