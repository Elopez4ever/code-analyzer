package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.infrastructure.ai.AIClient;
import com.analyzer.infrastructure.ai.AIClientFactory;
import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 使用 LLM 生成自然语言摘要
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryEnricher implements ChunkEnricher {
    private final AIClientFactory factory;

    @Override
    public int order() {
        return EnricherPriority.SUMMARY.getOrder();
    }

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
            log.warn("chunk 总结失败: [{}]", chunk.getId(), e);
            return chunk;  // 失败不阻断流水线，很好
        }
    }

    /**
     * 构建 Prompt  优化: 带上已有的 metadata 和 keywords，让 AI 有更多上下文
     * @param chunk chunk
     * @return prompt
     */
    private String buildUserPrompt(CodeChunk chunk) {
        // 带上已有的 metadata 和 keywords，让 AI 有更多上下文
        StringBuilder sb = new StringBuilder();
        sb.append("文件: ").append(chunk.getFilePath()).append("\n");
        sb.append("类型: ").append(chunk.getChunkType()).append("\n");
        if (chunk.getClassName() != null) {
            sb.append("类名: ").append(chunk.getClassName()).append("\n");
        }
        if (chunk.getMethodName() != null) {
            sb.append("方法名: ").append(chunk.getMethodName()).append("\n");
        }
        if (chunk.getKeywords() != null && !chunk.getKeywords().isEmpty()) {
            sb.append("关键词: ").append(String.join(", ", chunk.getKeywords())).append("\n");
        }
        sb.append("代码:\n").append(chunk.getContent());
        return sb.toString();
    }
}
