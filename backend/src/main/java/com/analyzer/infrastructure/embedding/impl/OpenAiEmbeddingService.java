package com.analyzer.infrastructure.embedding.impl;

import com.analyzer.common.config.EmbeddingProperties;
import com.analyzer.infrastructure.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiEmbeddingService implements EmbeddingService {
    private final RestTemplate restTemplate;
    private final EmbeddingProperties.ModelConfig config;
    @Override
    public float[] embed(String text) {
        List<float[]> results = embedBatch(List.of(text));
        return results.get(0);
    }
    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<float[]> allEmbeddings = new ArrayList<>();
        for (int i = 0; i < texts.size(); i += config.getMaxBatchSize()) {
            List<String> batch = texts.subList(i, Math.min(i + config.getMaxBatchSize(), texts.size()));
            allEmbeddings.addAll(callApi(batch));
        }
        return allEmbeddings;
    }
    @SuppressWarnings("unchecked")
    private List<float[]> callApi(List<String> texts) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            headers.set("Authorization", "Bearer " + config.getApiKey());
        }
        Map<String, Object> body = Map.of(
                "model", config.getModel(),
                "input", texts,
                "dimensions", config.getDimensions()
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                log.error("Embedding API returned empty response");
                return List.of();
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            List<float[]> embeddings = new ArrayList<>();
            for (Map<String, Object> item : data) {
                List<Number> embedding = (List<Number>) item.get("embedding");
                float[] vector = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    vector[i] = embedding.get(i).floatValue();
                }
                embeddings.add(vector);
            }
            log.debug("Successfully embedded {} texts using model {}", texts.size(), config.getModel());
            return embeddings;
        } catch (Exception e) {
            log.error("Failed to call embedding API [{}]: {}", config.getApiUrl(), e.getMessage(), e);
            throw new RuntimeException("Embedding API call failed", e);
        }
    }
}