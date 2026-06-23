package com.analyzer.modules.parser.pipeline.chunker;

import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 兜底 Chunker：任何没有专用 chunker 的文件类型都走这里。
 * 小文件整体入库，大文件滑动窗口切分。
 */
@Component
public class SlidingWindowChunker implements CodeChunker {

    private static final int MAX_WHOLE_FILE_LINES = 60;
    private static final int WINDOW_SIZE = 40;
    private static final int OVERLAP = 10;

    @Override
    public boolean supports(FileLanguage language) {
        return true;
    }

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public List<CodeChunk> chunk(SourceFile sourceFile, String projectId) {
        String content = sourceFile.getContent();
        List<String> lines = content.lines().toList();

        if (lines.size() <= MAX_WHOLE_FILE_LINES) {
            return List.of(CodeChunk.builder()
                    .projectId(projectId)
                    .filePath(sourceFile.getRelativePath())
                    .language(sourceFile.getLanguage())
                    .strategy(ChunkStrategy.WHOLE_FILE)
                    .role(CodeRole.UNKNOWN)
                    .content(content)
                    .startLine(1)
                    .build());
        }

        List<CodeChunk> chunks = new ArrayList<>();
        int step = WINDOW_SIZE - OVERLAP;

        for (int i = 0; i < lines.size(); i += step) {
            int end = Math.min(i + WINDOW_SIZE, lines.size());
            String windowContent = String.join("\n", lines.subList(i, end));

            chunks.add(CodeChunk.builder()
                    .projectId(projectId)
                    .filePath(sourceFile.getRelativePath())
                    .language(sourceFile.getLanguage())
                    .strategy(ChunkStrategy.SLIDING_WINDOW)
                    .role(CodeRole.UNKNOWN)
                    .content(windowContent)
                    .startLine(i + 1)
                    .build());

            if (end == lines.size()) break;
        }

        return chunks;
    }
}