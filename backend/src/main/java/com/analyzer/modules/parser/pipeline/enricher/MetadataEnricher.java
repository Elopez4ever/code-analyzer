package com.analyzer.modules.parser.pipeline.enricher;

import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;
import com.analyzer.modules.parser.pipeline.enricher.extractor.StructureExtractor;
import com.analyzer.modules.parser.pipeline.enricher.rule.ExtractionRule;
import com.analyzer.modules.parser.pipeline.enricher.rule.RuleRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 为 CodeChunk 填充结构化元数据：包名、类名、方法签名、修饰符、所属模块路径等
 * <p>
 * 数据来源有两层：
 * 1. {@link StructureExtractor}（如 {@code RuleBasedExtractor}）通过正则捕获结构化字段
 * 2. {@link RuleRegistry#getMetadataRules} 通过谓词规则补充分类标签
 */
@Component
@RequiredArgsConstructor
public class MetadataEnricher implements ChunkEnricher {

    private final List<StructureExtractor> extractors;
    private final RuleRegistry ruleRegistry;

    @Override
    public int order() {
        return EnricherPriority.METADATA.getOrder();
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        // 第一层：正则提取结构化字段（className, methodName, packageName 等）
        extractors.stream()
                .filter(e -> e.supportedRoles().contains(chunk.getRole()))
                .filter(e -> e.supportsLanguage(chunk.getLanguage()))
                .forEach(e -> e.extract(chunk));

        // 第二层：谓词规则补充分类标签（templateEngine, configFormat, framework 等）
        List<ExtractionRule> metadataRules = ruleRegistry.getMetadataRules(chunk.getRole());
        if (!metadataRules.isEmpty()) {
            Map<String, String> meta = new HashMap<>(chunk.getMetadata() != null ? chunk.getMetadata() : Map.of());
            for (ExtractionRule rule : metadataRules) {
                if (rule.matches(chunk)) {
                    applyMetadataRule(meta, rule.output());
                }
            }
            chunk.setMetadata(meta);
        }

        return chunk;
    }

    /**
     * 将规则输出 {@code key:value} 写入 metadata。
     * 若输出不含冒号则跳过；使用 {@code putIfAbsent} 避免覆盖 extractor 提取的精确值。
     */
    private void applyMetadataRule(Map<String, String> meta, String output) {
        int colonIdx = output.indexOf(':');
        if (colonIdx <= 0) {
            return;
        }
        String key = output.substring(0, colonIdx);
        String value = output.substring(colonIdx + 1);
        meta.putIfAbsent(key, value);
    }
}