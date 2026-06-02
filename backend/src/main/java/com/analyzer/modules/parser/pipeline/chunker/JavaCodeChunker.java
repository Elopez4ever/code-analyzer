package com.analyzer.modules.parser.pipeline.chunker;


import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeChunkType;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaCodeChunker implements CodeChunker {

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "(@\\w+\\s+)*(public|abstract|final)?\\s*(class|interface|enum)\\s+(\\w+)");
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

        CodeChunkType classType = resolveClassType(content);

        // 实体类、配置类、接口、枚举 -> 整体作为一个 chunk
        if (shouldKeepWhole(classType)) {
            chunks.add(buildChunk(sourceFile, projectId, content, classType, 0));
        } else {
            // Service、Controller、Repository、普通 Class -> 拆方法
            chunks.add(buildChunk(sourceFile, projectId, content, classType, 0)); // 类级别 chunk
            chunks.addAll(extractMethods(sourceFile, projectId, content));        // 方法级别 chunks
        }

        return chunks;
    }

    private boolean shouldKeepWhole(CodeChunkType type) {
        return Set.of(
                CodeChunkType.ENTITY, CodeChunkType.ENUM,
                CodeChunkType.INTERFACE, CodeChunkType.CONFIGURATION
        ).contains(type);
    }

    private CodeChunkType resolveClassType(String content) {
        // 先看注解
        for (String annotation : STEREOTYPE_ANNOTATIONS) {
            if (content.contains("@" + annotation)) {
                return CodeChunkType.valueOf(annotation.toUpperCase()
                        .replace("RESTCONTROLLER", "CONTROLLER"));
            }
        }
        // 再看关键字
        if (content.contains("interface ")) return CodeChunkType.INTERFACE;
        if (content.contains("enum ")) return CodeChunkType.ENUM;
        return CodeChunkType.CLASS;
    }

    private List<CodeChunk> extractMethods(SourceFile sourceFile, String projectId, String content) {
        List<CodeChunk> methods = new ArrayList<>();
        Matcher matcher = METHOD_PATTERN.matcher(content);

        while (matcher.find()) {
            int start = matcher.start();
            int end = findMethodEnd(content, matcher.end() - 1); // 从 { 开始找配对的 }
            if (end > start) {
                String methodBody = content.substring(start, end + 1);
                methods.add(buildChunk(sourceFile, projectId, methodBody, CodeChunkType.METHOD, start));
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

    private CodeChunk buildChunk(SourceFile sourceFile, String projectId, String content,
                                 CodeChunkType type, int startOffset) {
        return CodeChunk.builder()
                .projectId(projectId)
                .filePath(sourceFile.getRelativePath())
                .language(FileLanguage.JAVA)
                .chunkType(type)
                .content(content)
                .startLine(countLines(sourceFile.getContent(), startOffset))
                .build();
    }

    private int countLines(String content, int offset) {
        return (int) content.substring(0, Math.min(offset, content.length()))
                .chars().filter(c -> c == '\n').count() + 1;
    }
}
