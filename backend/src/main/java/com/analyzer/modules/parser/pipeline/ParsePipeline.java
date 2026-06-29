package com.analyzer.modules.parser.pipeline;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import com.analyzer.modules.parser.pipeline.chunker.ChunkerRouter;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * 纯解析管道：parse → chunk，只输出 {@code List<CodeChunk>}。
 * 不包含任何 enrich / embed / store 逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParsePipeline {
    private final List<ProjectParser> parsers;
    private final ChunkerRouter chunkerRouter;

    /**
     * 解析项目目录，输出所有代码块。
     *
     * @param projectId   项目 ID（透传给 chunker 生成 chunk id）
     * @param projectPath 项目本地目录路径
     * @return 解析并分块后的代码块列表
     */
    public List<CodeChunk> execute(String projectId, String projectPath) {
        Path root = Path.of(projectPath);

        // 1. 选择合适的 ProjectParser 提取源文件列表
        ProjectParser parser = parsers.stream()
                .sorted(Comparator.comparingInt(ProjectParser::priority))
                .filter(p -> p.supports(root))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARSER_NOT_FOUND));
        List<SourceFile> sourceFiles = parser.parse(root);

        // 2. 分块：每个源文件通过对应的 Chunker 拆分为 CodeChunk
        List<CodeChunk> chunks = sourceFiles.stream()
                .flatMap(file -> chunkerRouter.getChunker(file.getLanguage())
                        .chunk(file, projectId).stream())
                .toList();
        log.info("项目 {} 解析完成，生成 {} 个代码块", projectId, chunks.size());

        return chunks;
    }
}
