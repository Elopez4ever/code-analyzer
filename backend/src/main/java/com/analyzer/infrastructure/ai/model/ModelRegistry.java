package com.analyzer.infrastructure.ai.model;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelRegistry {
    private final ModelProperties properties;
    @PostConstruct
    void init() {
        log.info("已注册 {} 个 Chat 模型，默认: {}", properties.getModels().size(), properties.getDefaultModel());
        properties.getModels().keySet().forEach(m -> log.info("  - {}", m));
    }
    public String getDefaultModel() {
        return properties.getDefaultModel();
    }
    public boolean isAvailable(String model) {
        return properties.getModels().containsKey(model);
    }
    public Optional<ModelInfo> getModelInfo(String model) {
        return Optional.ofNullable(properties.getModels().get(model));
    }
    public List<ModelInfo> listModels() {
        return List.copyOf(properties.getModels().values());
    }
    public Map<String, ModelInfo> getAllModels() {
        return Collections.unmodifiableMap(properties.getModels());
    }
    /**
     * 解析模型：传入则校验，未传则返回默认
     */
    public String resolveModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return properties.getDefaultModel();
        }
        if (!isAvailable(requestedModel)) {
            log.error("模型不可用: {}, {}", requestedModel, List.copyOf(properties.getModels().keySet()));
            throw new BusinessException(ErrorCode.MODEL_NOT_AVAILABLE);
        }
        return requestedModel;
    }
}
