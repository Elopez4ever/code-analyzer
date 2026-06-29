package com.analyzer.modules.knowledgebase.enricher.extractor;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.function.Function;

@Getter
public class ExtractionRule {

    private final String metadataKey;
    private final Pattern pattern;
    private final int group;

    /** 可选的后处理函数，默认直接返回捕获到的字符串 */
    private final Function<String, String> postProcessor;

    public ExtractionRule(String metadataKey, Pattern pattern, int group) {
        this(metadataKey, pattern, group, String::trim);
    }

    public ExtractionRule(String metadataKey, Pattern pattern, int group, Function<String, String> postProcessor) {
        this.metadataKey = metadataKey;
        this.pattern = pattern;
        this.group = group;
        this.postProcessor = postProcessor;
    }

    public Optional<String> apply(String content) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) return Optional.empty();
        String raw = matcher.group(group);
        if (raw == null) return Optional.empty();
        return Optional.of(postProcessor.apply(raw));
    }
}
