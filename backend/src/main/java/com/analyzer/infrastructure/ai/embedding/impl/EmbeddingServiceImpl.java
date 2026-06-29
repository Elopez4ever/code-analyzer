package com.analyzer.infrastructure.ai.embedding.impl;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import com.analyzer.infrastructure.ai.embedding.EmbeddingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    @Retry(name = "chat-service")
    @CircuitBreaker(name = "chat-service")
    public float[] embed(String text) {
        try {
            return embeddingModel.call(
                    new EmbeddingRequest(List.of(text), null)
            ).getResult().getOutput();
        } catch (Exception e) {
            log.error("Embedding 失败 | error={}", e.getMessage());
            throw new BusinessException(ErrorCode.EMBED_FAILED);
        }
    }

    @Override
    @Retry(name = "chat-service")
    @CircuitBreaker(name = "chat-service")
    public List<float[]> embedBatch(List<String> texts) {
        try {
            EmbeddingResponse response = embeddingModel.call(
                    new EmbeddingRequest(texts, null)
            );
            return response.getResults().stream()
                    .map(Embedding::getOutput)
                    .toList();
        } catch (Exception e) {
            log.error("Embedding 批量失败 | size={} | error={}", texts.size(), e.getMessage());
            throw new BusinessException(ErrorCode.EMBED_FAILED);
        }
    }

    @Override
    public int dimensions() {
        return embeddingModel.dimensions();
    }
}