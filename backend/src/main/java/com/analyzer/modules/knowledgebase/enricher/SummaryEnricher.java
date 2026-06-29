package com.analyzer.modules.knowledgebase.enricher;

import com.analyzer.infrastructure.ai.chat.ChatService;
import com.analyzer.modules.knowledgebase.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 使用 LLM 生成自然语言摘要
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryEnricher implements ChunkEnricher {

    private final ChatService chatService;

    private static final String MODEL_KEY = "deepseek";
    private static final String SYSTEM_PROMPT =
            "你是一个代码分析助手，用简洁的中文总结代码片段的功能，不超过两句话。";

    @Override
    public int order() {
        return EnricherPriority.SUMMARY.getOrder();
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        try {
            String summary = chatService.prompt()
                    .model(MODEL_KEY)
                    .system(SYSTEM_PROMPT)
                    .call(buildUserPrompt(chunk));

            return chunk.toBuilder().summary(summary).build();
        } catch (Exception e) {
            log.warn("chunk 总结失败: [{}]", chunk.getId(), e);
            return chunk;
        }
    }

    private String buildUserPrompt(CodeChunk chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append("文件: ").append(chunk.getFilePath()).append("\n");
        sb.append("角色: ").append(chunk.getRole()).append("\n");
        if (chunk.getMetadata() != null) {
            if (chunk.getMetadata().get("className") != null) {
                sb.append("类名: ").append(chunk.getMetadata().get("className")).append("\n");
            }
            if (chunk.getMetadata().get("methodName") != null) {
                sb.append("方法名: ").append(chunk.getMetadata().get("methodName")).append("\n");
            }
        }
        if (chunk.getKeywords() != null && !chunk.getKeywords().isEmpty()) {
            sb.append("关键词: ").append(String.join(", ", chunk.getKeywords())).append("\n");
        }
        sb.append("代码:\n").append(chunk.getContent());
        return sb.toString();
    }
}
