package com.analyzer.modules.knowledgebase.enricher.rule;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.analyzer.modules.parser.pipeline.domain.CodeRole;

/**
 * 规则仓库 从 CodeRole 映射到一系列规则
 */
public class RuleRegistry {
    private final Map<CodeRole, List<ExtractionRule>> keywordRules = new EnumMap<>(CodeRole.class);
    private final Map<CodeRole, List<ExtractionRule>> metadataRules = new EnumMap<>(CodeRole.class);
    public RuleRegistry addKeywordRule(CodeRole type, ExtractionRule rule) {
        keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
        return this;
    }
    public RuleRegistry addKeywordRules(CodeRole type, List<ExtractionRule> rules) {
        keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).addAll(rules);
        return this;
    }
    public RuleRegistry addKeywordRules(List<CodeRole> types, List<ExtractionRule> rules) {
        for (CodeRole type : types) {
            keywordRules.computeIfAbsent(type, k -> new ArrayList<>()).addAll(rules);
        }
        return this;
    }
    public RuleRegistry addMetadataRule(CodeRole type, ExtractionRule rule) {
        metadataRules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
        return this;
    }
    public List<ExtractionRule> getKeywordRules(CodeRole type) {
        return keywordRules.getOrDefault(type, List.of());
    }
    public List<ExtractionRule> getMetadataRules(CodeRole type) {
        return metadataRules.getOrDefault(type, List.of());
    }
}
