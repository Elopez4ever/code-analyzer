package com.analyzer.modules.parser.pipeline.enricher.rule;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;

import java.util.function.Predicate;

public record ExtractionRule(Predicate<CodeChunk> condition, String output) {
    public static ExtractionRule when(Predicate<CodeChunk> condition, String output) {
        return new ExtractionRule(condition, output);
    }

    public static ExtractionRule contentContains(String text, String output) {
        return new ExtractionRule(codeChunk -> codeChunk.getContent().contains(text), output);
    }

    public static ExtractionRule pathEndsWith(String suffix, String output) {
        return new ExtractionRule(
                chunk -> chunk.getFilePath() != null && chunk.getFilePath().endsWith(suffix),
                output
        );
    }

    public static ExtractionRule fileNameContains(String text, String output) {
        return new ExtractionRule(chunk -> {
            if (chunk.getFilePath() == null) return false;
            String fileName = chunk.getFilePath()
                    .substring(chunk.getFilePath().lastIndexOf('/') + 1)
                    .toLowerCase();
            return fileName.contains(text);
        }, output);
    }

    public boolean matches(CodeChunk chunk) {
        return condition.test(chunk);
    }
}
