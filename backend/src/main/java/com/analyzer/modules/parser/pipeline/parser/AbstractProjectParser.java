package com.analyzer.modules.parser.pipeline.parser;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import com.analyzer.modules.parser.pipeline.ProjectParser;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import com.analyzer.modules.parser.pipeline.parser.detector.FileLanguageDetector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * 项目解析器的公共基类, 封装文件遍历、过滤、转换等通用逻辑
 */
@Slf4j
public abstract class AbstractProjectParser implements ProjectParser {
    protected final FileLanguageDetector detector;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    protected AbstractProjectParser(FileLanguageDetector detector) {
        this.detector = detector;
    }

    @Override
    public List<SourceFile> parse(Path projectRoot) {
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !detector.isExcluded(projectRoot.relativize(path).toString()))
                    .filter(Files::isReadable)
                    .filter(this::isWithinSizeLimit)
                    .map(path -> toSourceFile(projectRoot, path))
                    .filter(file -> {
                        if (file.getLanguage() == FileLanguage.OTHER) {
                            log.debug("跳过未识别文件: {}", file.getRelativePath());
                            return false;
                        }
                        return true;
                    })
                    .toList();
        } catch (IOException e) {
            log.error("项目解析中发生错误: {}", projectRoot, e);
            throw new BusinessException(ErrorCode.PARSE_FAILED);
        }
    }

    /**
     * 过滤条件：子类可覆写以决定哪些 SourceFile 保留
     * 默认过滤掉语言为 OTHER 的文件
     */
    protected boolean acceptSourceFile(SourceFile file) {
        return file.getLanguage() != FileLanguage.OTHER;
    }

    /**
     * 将指定的文件路径转换为源文件对象
     */
    protected SourceFile toSourceFile(Path projectRoot, Path filePath) {
        String relativePath = projectRoot.relativize(filePath).toString().replace('\\', '/');
        FileLanguage language = detector.detect(relativePath);
        String content;
        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
            log.warn("读取文件失败: {}", filePath, e);
            content = "";
        }
        return SourceFile.builder()
                .relativePath(relativePath)
                .absolutePath(filePath.toString())
                .content(content)
                .language(language)
                .fileSize(content.length())
                .build();
    }

    /**
     * 检查文件大小
     */
    protected boolean isWithinSizeLimit(Path path) {
        try {
            return Files.size(path) <= MAX_FILE_SIZE;
        } catch (IOException e) {
            return false;
        }
    }
}
