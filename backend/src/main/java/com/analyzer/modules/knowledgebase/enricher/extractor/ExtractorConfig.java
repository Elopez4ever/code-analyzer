package com.analyzer.modules.knowledgebase.enricher.extractor;

import com.analyzer.modules.parser.pipeline.domain.CodeRole;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Configuration
public class ExtractorConfig {

    @Bean
    public RuleBasedExtractor javaClassExtractor() {
        return RuleBasedExtractor.builder()
                .roles(Set.of(
                        CodeRole.CLASS_DECLARATION,
                        CodeRole.INTERFACE_DECLARATION,
                        CodeRole.ENUM_DECLARATION))
                .languages(Set.of(FileLanguage.JAVA))
                .rules(List.of(
                        new ExtractionRule("packageName",
                                Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE), 1),
                        new ExtractionRule("className",
                                Pattern.compile("(?:class|interface|enum)\\s+(\\w+)"), 1),
                        new ExtractionRule("extends",
                                Pattern.compile("extends\\s+(\\w+)"), 1),
                        new ExtractionRule("implements",
                                Pattern.compile("implements\\s+([\\w,\\s]+?)\\s*\\{"), 1)
                ))
                .build();
    }

    @Bean
    public RuleBasedExtractor javaMethodExtractor() {
        return RuleBasedExtractor.builder()
                .roles(Set.of(CodeRole.METHOD))
                .languages(Set.of(FileLanguage.JAVA))
                .rules(List.of(
                        new ExtractionRule("methodName",
                                Pattern.compile(
                                        "(?:(?:public|private|protected|static|final|abstract|synchronized)\\s+)*" +
                                                "[\\w<>\\[\\],]+\\s+(\\w+)\\s*\\("), 1),
                        new ExtractionRule("paramCount",
                                Pattern.compile("\\(([^)]*)\\)"), 1,
                                p -> {
                                    String t = p.trim();
                                    return t.isEmpty() ? "0" : String.valueOf(t.split(",").length);
                                })
                ))
                .build();
    }

    @Bean
    public RuleBasedExtractor xmlMapperExtractor() {
        return RuleBasedExtractor.builder()
                .roles(Set.of(CodeRole.SQL_STATEMENT))
                .languages(Set.of(FileLanguage.XML_MYBATIS))
                .rules(List.of(
                        new ExtractionRule("statementId",
                                Pattern.compile("id=\"(\\w+)\""), 1),
                        new ExtractionRule("statementType",
                                Pattern.compile("<(select|insert|update|delete)"), 1),
                        new ExtractionRule("resultType",
                                Pattern.compile("resultType=\"([^\"]+)\""), 1)
                ))
                .build();
    }
}
