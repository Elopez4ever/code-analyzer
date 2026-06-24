package com.analyzer.modules.parser.pipeline.chunker;

import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaCodeChunker implements CodeChunker {

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?:public|private|protected|static|final|abstract|synchronized|\\s)*" +
                    "[\\w<>\\[\\],\\s]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+[\\w,\\s]+)?\\s*\\{");

    private static final Set<String> STEREOTYPE_ANNOTATIONS = Set.of(
            "Controller", "RestController", "Service", "Repository",
            "Entity", "Configuration", "Component"
    );

    @Override
    public boolean supports(FileLanguage language) {
        return language == FileLanguage.JAVA;
    }

    @Override
    public List<CodeChunk> chunk(SourceFile sourceFile, String projectId) {
        String content = sourceFile.getContent();
        List<CodeChunk> chunks = new ArrayList<>();

        CodeRole classRole = resolveClassRole(content);
        String stereotype = resolveStereotype(content);
        Map<String, String> meta = new HashMap<>();
        if (stereotype != null) {
            meta.put("stereotype", stereotype);
        }

        // 类级别 chunk
        chunks.add(CodeChunk.builder()
                .projectId(projectId)
                .filePath(sourceFile.getRelativePath())
                .language(FileLanguage.JAVA)
                .strategy(ChunkStrategy.SEMANTIC)
                .role(classRole)
                .content(content)
                .startLine(1)
                .metadata(meta)
                .build());

        // 非整体保留的类型 -> 额外拆方法
        if (!shouldKeepWhole(classRole, stereotype)) {
            chunks.addAll(extractMethods(sourceFile, projectId, content));
        }

        return chunks;
    }

    private boolean shouldKeepWhole(CodeRole role, String stereotype) {
        // 接口、枚举、实体、配置类整体保留
        if (role == CodeRole.INTERFACE_DECLARATION || role == CodeRole.ENUM_DECLARATION) {
            return true;
        }
        if (stereotype != null) {
            return Set.of("Entity", "Configuration").contains(stereotype);
        }
        return false;
    }

    private CodeRole resolveClassRole(String content) {
        if (content.contains("interface ")) return CodeRole.INTERFACE_DECLARATION;
        if (content.contains("enum ")) return CodeRole.ENUM_DECLARATION;
        return CodeRole.CLASS_DECLARATION;
    }

    private String resolveStereotype(String content) {
        for (String annotation : STEREOTYPE_ANNOTATIONS) {
            if (content.contains("@" + annotation)) {
                return annotation;
            }
        }
        return null;
    }

    private List<CodeChunk> extractMethods(SourceFile sourceFile, String projectId, String content) {
        List<CodeChunk> methods = new ArrayList<>();
        Matcher matcher = METHOD_PATTERN.matcher(content);

        while (matcher.find()) {
            int start = matcher.start();
            int end = findMethodEnd(content, matcher.end() - 1);
            if (end > start) {
                String methodBody = content.substring(start, end + 1);
                methods.add(CodeChunk.builder()
                        .projectId(projectId)
                        .filePath(sourceFile.getRelativePath())
                        .language(FileLanguage.JAVA)
                        .strategy(ChunkStrategy.SEMANTIC)
                        .role(CodeRole.METHOD)
                        .content(methodBody)
                        .startLine(countLines(content, start))
                        .build());
            }
        }
        return methods;
    }

    private int findMethodEnd(String content, int openBraceIndex) {
        int depth = 0;
        for (int i = openBraceIndex; i < content.length(); i++) {
            if (content.charAt(i) == '{') depth++;
            else if (content.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private int countLines(String content, int offset) {
        return (int) content.substring(0, Math.min(offset, content.length()))
                .chars().filter(c -> c == '\n').count() + 1;
    }
}