package com.analyzer.modules.parser.pipeline.chunker;

import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkerRouter {

    private final List<CodeChunker> chunkers;

    /**
     * 路由逻辑：
     * 在所有 supports 该语言的 chunker 中，取优先级最高（数字最小）的。
     * 由于 SlidingWindowChunker 始终返回 true，保证不会为空。
     */
    public CodeChunker getChunker(FileLanguage language) {
        CodeChunker selected = chunkers.stream()
                .filter(c -> c.supports(language))
                .min(Comparator.comparingInt(CodeChunker::priority))
                .orElseThrow(() -> new IllegalStateException("No chunker available"));

        if (selected.priority() >= 1000) {
            log.info("语言 {} 无专用 chunker，使用滑动窗口兜底", language);
        }

        return selected;
    }
}