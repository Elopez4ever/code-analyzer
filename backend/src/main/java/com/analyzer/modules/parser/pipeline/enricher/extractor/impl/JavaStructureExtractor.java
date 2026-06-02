package com.analyzer.modules.parser.pipeline.enricher.extractor.impl;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;
import com.analyzer.modules.parser.pipeline.enricher.extractor.StructureExtractor;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaStructureExtractor implements StructureExtractor {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
    private static final Pattern CLASS_PATTERN = Pattern.compile("(?:class|interface|enum)\\s+(\\w+)");
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("extends\\s+(\\w+)");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("implements\\s+([\\w,\\s]+?)\\s*\\{");

    @Override
    public Set<CodeChunkType> supportedTypes() {
        return Set.of(
                CodeChunkType.CLASS, CodeChunkType.INTERFACE, CodeChunkType.ENUM,
                CodeChunkType.CONTROLLER, CodeChunkType.SERVICE, CodeChunkType.REPOSITORY,
                CodeChunkType.ENTITY, CodeChunkType.CONFIGURATION, CodeChunkType.COMPONENT
        );
    }

    @Override
    public void extract(CodeChunk chunk) {
        String content = chunk.getContent();
        match(PACKAGE_PATTERN, content, 1).ifPresent(v -> chunk.getMetadata().put("packageName", v));
        match(CLASS_PATTERN, content, 1).ifPresent(v -> chunk.getMetadata().put("className", v));
        match(EXTENDS_PATTERN, content, 1).ifPresent(v -> chunk.getMetadata().put("extends", v.trim()));
        match(IMPLEMENTS_PATTERN, content, 1).ifPresent(v -> chunk.getMetadata().put("implements", v.trim()));
        chunk.getMetadata().put("stereotype", chunk.getChunkType().name().toLowerCase());
    }
    private Optional<String> match(Pattern pattern, String content, int group) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Optional.of(matcher.group(group)) : Optional.empty();
    }
}
