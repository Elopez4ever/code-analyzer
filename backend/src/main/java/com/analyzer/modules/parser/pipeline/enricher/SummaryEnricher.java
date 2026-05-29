package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.infrastructure.ai.AIClient;
import com.analyzer.infrastructure.ai.AIClientFactory;
import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryEnricher implements ChunkEnricher {
    private final AIClientFactory factory;

    @Override
    public List<FileLanguage> supportedLanguages() {
        return List.of();
    }

    @Override
    public int order() {
        return 100;
    }

    // TODO: 使用默认模型, 以及如何构建提示词
    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        try {
            AIClient aiClient = factory.getClient("deepseek");
            String summary = aiClient.chat(
                    "你是一个代码分析助手，用简洁的中文总结代码片段的功能，不超过两句话。",
                    buildUserPrompt(chunk)
            );
            return chunk.toBuilder().summary(summary).build();
        } catch (Exception e) {
            log.warn("Summary generation failed for chunk [{}]", chunk.getId(), e);
            return chunk;
        }
    }


    private String buildUserPrompt(CodeChunk chunk) {
        // 构建用户提示词
        return chunk.getContent();
    }
}
