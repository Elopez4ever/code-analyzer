package com.analyzer.modules.parser.pipeline.parser.detector;


import cn.hutool.core.util.ObjectUtil;
import com.analyzer.common.config.ParserConfigProperties;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Comparator;
import java.util.Optional;

/**
 * 文件语言类型检测器
 */
@Component
@RequiredArgsConstructor
public class FileLanguageDetector {
    private final ParserConfigProperties config;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 检测匹配的语言类型
     * @param relativePath 相对路径
     * @return 语言类型
     */
    public FileLanguage detect(String relativePath) {
        String fileName = extractFileName(relativePath);
        Optional<ParserConfigProperties.FileTypeRule> matched = config.getRules().stream()
                .filter(rule -> matchRule(relativePath, fileName, rule))
                .max(Comparator.comparingInt(ParserConfigProperties.FileTypeRule::getPriority));
        return matched
                .map(rule -> FileLanguage.valueOf(rule.getLanguage()))
                .orElse(FileLanguage.OTHER);
    }

    /**
     * 判断文件是否应该被跳过
     * @param relativePath 相对路径
     * @return 判断结果
     */
    public boolean isExcluded(String relativePath) {
        return config.getExcludePatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, relativePath));
    }

    /**
     * 检测文件是否匹配
     * @param relativePath 相对路径
     * @param fileName 文件名
     * @param rule 匹配规则
     * @return 匹配结果
     */
    private boolean matchRule(String relativePath, String fileName, ParserConfigProperties.FileTypeRule rule) {
        // 按文件名匹配
        if (ObjectUtil.isNotNull(rule.getFileNames()) && rule.getFileNames().contains(fileName)) {
            return true;
        }
        // 按路径模式匹配
        if (ObjectUtil.isNotNull(rule.getPathPatterns())) {
            boolean pathMatch = rule.getPathPatterns().stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, relativePath));
            if (pathMatch) return true;
        }
        // 按扩展名匹配
        if (ObjectUtil.isNotNull(rule.getExtensions())) {
            return rule.getExtensions().stream()
                    .anyMatch(relativePath::endsWith);
        }
        return false;
    }

    /**
     * 从路径中提取文件名
     * @param path 路径
     * @return 文件名
     */
    private String extractFileName(String path) {
        int idx = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return idx > 0 ? path.substring(idx + 1) : path;
    }
}
