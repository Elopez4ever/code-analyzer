package com.analyzer.modules.parser.pipeline.enricher.rule;

import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 规则仓库 从 CodeChunkType 映射到一系列规则
 */
public class RuleRegistry {
    private final Map<CodeChunkType, List<ExtractionRule>> keywordRules = new EnumMap<>(CodeChunkType.class);
    private final Map<CodeChunkType, List<ExtractionRule>> metadataRules = new EnumMap<>(CodeChunkType.class);
    public RuleRegistry addKeywordRule(CodeChunkType type, ExtractionRule rule) {
        keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
        return this;
    }
    public RuleRegistry addKeywordRules(CodeChunkType type, List<ExtractionRule> rules) {
        keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).addAll(rules);
        return this;
    }
    public RuleRegistry addKeywordRules(List<CodeChunkType> types, List<ExtractionRule> rules) {
        for (CodeChunkType type : types) {
            keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).addAll(rules);
        }
        return this;
    }
    public RuleRegistry addMetadataRule(CodeChunkType type, ExtractionRule rule) {
        metadataRules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
        return this;
    }
    public List<ExtractionRule> getKeywordRules(CodeChunkType type) {
        return keywordRules.getOrDefault(type, List.of());
    }
    public List<ExtractionRule> getMetadataRules(CodeChunkType type) {
        return metadataRules.getOrDefault(type, List.of());
    }
}
