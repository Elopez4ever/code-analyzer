package com.analyzer.modules.parser.pipeline.enricher.extractor;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeRole;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuleBasedExtractor implements StructureExtractor {

    private final Set<CodeRole> roles;
    private final Set<FileLanguage> languages;
    private final List<ExtractionRule> rules;

    private RuleBasedExtractor(Builder builder) {
        this.roles = Set.copyOf(builder.roles);
        this.languages = builder.languages == null ? Set.of() : Set.copyOf(builder.languages);
        this.rules = List.copyOf(builder.rules);
    }

    @Override
    public Set<CodeRole> supportedRoles() {
        return roles;
    }

    @Override
    public Set<FileLanguage> supportedLanguages() {
        return languages;
    }

    @Override
    public void extract(CodeChunk chunk) {
        Map<String, String> existing = chunk.getMetadata();
        Map<String, String> meta = new HashMap<>(existing != null ? existing : Map.of());
        String content = chunk.getContent();
        for (ExtractionRule rule : rules) {
            rule.apply(content).ifPresent(v -> meta.put(rule.getMetadataKey(), v));
        }
        chunk.setMetadata(meta);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<CodeRole> roles;
        private Set<FileLanguage> languages;
        private List<ExtractionRule> rules;

        public Builder roles(Set<CodeRole> roles) {
            this.roles = roles;
            return this;
        }

        public Builder languages(Set<FileLanguage> languages) {
            this.languages = languages;
            return this;
        }

        public Builder rules(List<ExtractionRule> rules) {
            this.rules = rules;
            return this;
        }

        public RuleBasedExtractor build() {
            if (roles == null || roles.isEmpty()) throw new IllegalStateException("roles must not be empty");
            if (rules == null || rules.isEmpty()) throw new IllegalStateException("rules must not be empty");
            return new RuleBasedExtractor(this);
        }
    }
}