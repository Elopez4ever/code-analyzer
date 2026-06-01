package com.analyzer.modules.parser.pipeline.detector.enricher;

import com.analyzer.modules.parser.pipeline.ChunkEnricher;
import com.analyzer.modules.parser.pipeline.detector.enricher.rule.ExtractionRule;
import com.analyzer.modules.parser.pipeline.detector.enricher.rule.RuleRegistry;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.EnricherPriority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 提取关键词/标签：注解名、依赖的类型、设计模式标记
 */
@Component
public class KeywordEnricher implements ChunkEnricher {
    private final RuleRegistry ruleRegistry;
    public KeywordEnricher(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    @Override
    public int order() {
        return EnricherPriority.KEYWORD.getOrder();
    }

    @Override
    public CodeChunk enrich(CodeChunk chunk) {
        List<String> keywords = new ArrayList<>();
        keywords.add("type:" + chunk.getChunkType().name().toLowerCase());
        List<ExtractionRule> rules = ruleRegistry.getKeywordRules(chunk.getChunkType());
        for (ExtractionRule rule : rules) {
            if (rule.matches(chunk)) {
                keywords.add(rule.output());
            }
        }
        chunk.setKeywords(keywords);
        return chunk;
    }
}
