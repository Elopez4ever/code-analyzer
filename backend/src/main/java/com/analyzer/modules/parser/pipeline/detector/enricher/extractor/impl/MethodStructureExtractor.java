package com.analyzer.modules.parser.pipeline.detector.enricher.extractor.impl;

import com.analyzer.modules.parser.pipeline.detector.enricher.extractor.StructureExtractor;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MethodStructureExtractor implements StructureExtractor {
    private static final Pattern JAVA_METHOD = Pattern.compile(
            "(?:public|private|protected|static|final|abstract|synchronized|\\s)*" +
                    "[\\w<>\\[\\],\\s]+\\s+(\\w+)\\s*\\([^)]*\\)");
    private static final Pattern JS_FUNCTION = Pattern.compile(
            "(?:export\\s+)?(?:async\\s+)?(?:function\\s+(\\w+)|(?:const|let|var)\\s+(\\w+)\\s*=)");
    private static final Pattern PARAMS = Pattern.compile("\\(([^)]*)\\)");
    @Override
    public Set<CodeChunkType> supportedTypes() {
        return Set.of(CodeChunkType.METHOD);
    }
    @Override
    public void extract(CodeChunk chunk) {
        String content = chunk.getContent();
        if (chunk.getLanguage() == FileLanguage.JAVA) {
            match(JAVA_METHOD, content, 1).ifPresent(chunk::setMethodName);
        } else {
            Matcher m = JS_FUNCTION.matcher(content);
            if (m.find()) {
                chunk.setMethodName(m.group(1) != null ? m.group(1) : m.group(2));
            }
        }
        match(PARAMS, content, 1).ifPresent(params -> {
            String trimmed = params.trim();
            int count = trimmed.isEmpty() ? 0 : trimmed.split(",").length;
            chunk.getMetadata().put("paramCount", String.valueOf(count));
        });
        if (chunk.getFilePath() != null) {
            String fileName = chunk.getFilePath().substring(chunk.getFilePath().lastIndexOf('/') + 1);
            String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            chunk.getMetadata().put("belongsToFile", baseName);
        }
    }
    private Optional<String> match(Pattern pattern, String content, int group) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Optional.of(matcher.group(group)) : Optional.empty();
    }
}