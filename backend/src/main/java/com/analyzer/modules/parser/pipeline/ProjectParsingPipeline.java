package com.analyzer.modules.parser.pipeline;

import com.analyzer.common.exception.BusinessException;
import com.analyzer.infrastructure.embedding.EmbeddingService;
import com.analyzer.infrastructure.vectorstore.VectorStore;
import com.analyzer.modules.parser.pipeline.chunker.ChunkerRouter;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * 向量化存储一个项目
 *  parse -> chunk -> enrich -> embed -> store
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectParsingPipeline {
    private final List<ProjectParser> parsers;
    private final ChunkerRouter chunkerRouter;
    private final EnricherPipeline enricherPipeline;
    private static final String COLLECTION = "code_chunks";
    private static final int BATCH_SIZE = 20;

    public void execute(String projectId, String projectPath) {
        Path root = Path.of(projectPath);

        // 将项目拆分为 SourceFiles
        ProjectParser parser = parsers.stream()
                .filter(p -> p.supports(root))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到匹配项目的parser!"));
        List<SourceFile> sourceFiles = parser.parse(root);

        // 分块
        List<CodeChunk> chunks = sourceFiles.stream()
                .flatMap(file -> chunkerRouter.getChunker(file.getLanguage())
                        .chunk(file, projectId).stream())
                .toList();
        log.info("项目 {} 生成 {} 个代码块", projectId, chunks.size());

        //元数据增强
        List<CodeChunk> enrichedChunks = enricherPipeline.enrich(chunks);

        // TODO: 批量向量化 + 存储

    }
}
