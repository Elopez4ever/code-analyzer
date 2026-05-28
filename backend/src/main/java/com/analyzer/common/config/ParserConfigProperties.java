package com.analyzer.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "code-parser")
public class ParserConfigProperties {
    /**
     * 应被忽略的路径
     */
    private List<String> excludePatterns;

    /**
     * 文件规则
     */
    private List<FileTypeRule> rules;

    @Data
    public static class FileTypeRule {
        /**
         * 语言
         */
        private String language;
        /**
         * 按扩展名匹配
         */
        private List<String> extensions;
        /**
         * 按完整文件名匹配
         */
        private List<String> fileNames;
        /**
         * 按照路径通配符匹配
         */
        private List<String> pathPatterns;
        /**
         * 优先级 (解决 pom.xml 和 mapper.xml 冲突问题)
         */
        private int priority;
    }
}
