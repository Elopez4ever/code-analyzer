package com.analyzer.modules.parser.pipeline.enricher.rule;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;

import java.util.function.Predicate;

/**
 * 提取规则 -> 当给定的 condition 为 true, 输出 output 字符串
 * @param condition 匹配条件
 * @param output 输出字符串
 */
public record ExtractionRule(Predicate<CodeChunk> condition, String output) {

    /**
     * 通用构造, 当 condition, 输出 output
     * @param condition 匹配条件
     * @param output 输出字符串
     * @return 提取规则
     */
    public static ExtractionRule when(Predicate<CodeChunk> condition, String output) {
        return new ExtractionRule(condition, output);
    }

    /**
     * condition: 若 CodeChunk 包含 text
     * @param text 包含内容
     * @param output  输出字符串
     * @return 提取规则
     */
    public static ExtractionRule contentContains(String text, String output) {
        return new ExtractionRule(codeChunk -> codeChunk.getContent().contains(text), output);
    }

    /**
     * condition: 若 CodeChunk 的文件名以 suffix 结尾
     * @param suffix 后缀
     * @param output 输出字符串
     * @return 提取规则
     */
    public static ExtractionRule pathEndsWith(String suffix, String output) {
        return new ExtractionRule(
                chunk -> chunk.getFilePath() != null && chunk.getFilePath().endsWith(suffix),
                output
        );
    }

    /**
     * condition: 若 CodeChunk 的 FileName 包含 text
     * @param text 包含内容
     * @param output  输出字符串
     * @return 提取规则
     */
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
