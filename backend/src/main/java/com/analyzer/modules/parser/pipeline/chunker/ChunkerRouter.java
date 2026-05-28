package com.analyzer.modules.parser.pipeline.chunker;

import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkerRouter {
    private final List<CodeChunker> chunkers;

    public CodeChunker getChunker(FileLanguage language) {
        return chunkers.stream()
                .filter(codeChunker -> codeChunker.supports(language))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("寻找chunker失败, 使用默认方式, 语言为: {}", language);
                    return chunkers.stream()
                            .filter(c -> c.supports(FileLanguage.OTHER))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No default chunker registered"));
                });
    }
}
