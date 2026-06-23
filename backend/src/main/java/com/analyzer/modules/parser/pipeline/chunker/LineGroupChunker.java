package com.analyzer.modules.parser.pipeline.chunker;

import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 按空行分段切分，适合配置文件、脚本等。
 */
@Component
public class LineGroupChunker implements CodeChunker {

    private static final int MAX_GROUP_LINES = 50;

    private static final Set<FileLanguage> SUPPORTED = Set.of(
            FileLanguage.PROPERTIES,
            FileLanguage.YAML,
            FileLanguage.DOCKERFILE,
            FileLanguage.SHELL,
            FileLanguage.MARKDOWN
    );

    @Override
    public boolean supports(FileLanguage language) {
        return SUPPORTED.contains(language);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public List<CodeChunk> chunk(SourceFile sourceFile, String projectId) {
        List<String> lines = sourceFile.getContent().lines().toList();
        List<CodeChunk> chunks = new ArrayList<>();

        List<String> currentGroup = new ArrayList<>();
        int groupStartLine = 1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean isBlank = line.isBlank();
            boolean groupFull = currentGroup.size() >= MAX_GROUP_LINES;

            if ((isBlank || groupFull) && !currentGroup.isEmpty()) {
                chunks.add(buildChunk(sourceFile, projectId, currentGroup, groupStartLine));
                currentGroup = new ArrayList<>();
                groupStartLine = i + 2;
            } else if (!isBlank) {
                if (currentGroup.isEmpty()) {
                    groupStartLine = i + 1;
                }
                currentGroup.add(line);
            }
        }

        if (!currentGroup.isEmpty()) {
            chunks.add(buildChunk(sourceFile, projectId, currentGroup, groupStartLine));
        }

        return chunks;
    }

    private CodeChunk buildChunk(SourceFile sourceFile, String projectId,
                                 List<String> lines, int startLine) {
        return CodeChunk.builder()
                .projectId(projectId)
                .filePath(sourceFile.getRelativePath())
                .language(sourceFile.getLanguage())
                .strategy(ChunkStrategy.LINE_GROUP)
                .role(CodeRole.CONFIG_BLOCK)
                .content(String.join("\n", lines))
                .startLine(startLine)
                .build();
    }
}