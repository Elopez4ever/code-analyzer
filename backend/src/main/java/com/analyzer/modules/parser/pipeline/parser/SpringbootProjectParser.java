package com.analyzer.modules.parser.pipeline.parser;

import com.analyzer.common.exception.BusinessException;
import com.analyzer.modules.parser.pipeline.ProjectParser;
import com.analyzer.modules.parser.pipeline.detector.FileLanguageDetector;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Boot 项目解析器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringbootProjectParser implements ProjectParser {

    private final FileLanguageDetector detector;
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    /**
     * 判断当前解析器是否支持该项目的解析。
     * @param projectRoot 项目根目录的路径（非空）
     * @return 如果支持则返回 {@code true}，否则返回 {@code false}
     */
    @Override
    public boolean supports(Path projectRoot) {
        return Files.exists(projectRoot.resolve("pom.xml"))
                || Files.exists(projectRoot.resolve("build.gradle"));
    }

    /**
     * 解析项目根目录下的所有源文件。
     * @param projectRoot 项目根目录路径，将被递归遍历
     * @return 解析出来的源文件列表，不会为 {@code null}
     * @throws BusinessException 如果遍历项目目录时发生 I/O 错误
     */
    @Override
    public List<SourceFile> parse(Path projectRoot) {
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !detector.isExcluded(projectRoot.relativize(path).toString()))
                    .filter(Files::isReadable)
                    .filter(this::isWithinSizeLimit)
                    .map(path -> toSourceFile(projectRoot, path))
                    .filter(file -> file.getLanguage() != FileLanguage.OTHER)
                    .toList();
        } catch (IOException e) {
            throw new BusinessException("项目转换中发生错误: " + projectRoot, e);
        }
    }

    /**
     * 将指定的文件路径转换为源文件对象。
     * @param projectRoot 项目根目录路径，用于计算相对路径
     * @param filePath    待转换的文件绝对路径（必须是普通文件）
     * @return 转换后的源文件对象
     */
    private SourceFile toSourceFile(Path projectRoot, Path filePath) {
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
     * 检查指定文件的大小是否在允许的范围内
     * @param path 文件的路径
     * @return 如果文件大小不超过 {@value #MAX_FILE_SIZE} 字节则返回 {@code true}，
     *         否则返回 {@code false}；若无法获取文件大小也返回 {@code false}
     */
    private boolean isWithinSizeLimit(Path path) {
        try {
            return Files.size(path) <= MAX_FILE_SIZE;
        } catch (IOException e) {
            return false;
        }
    }
}