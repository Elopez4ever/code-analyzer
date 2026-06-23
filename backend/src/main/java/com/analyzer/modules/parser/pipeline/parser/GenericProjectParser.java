package com.analyzer.modules.parser.pipeline.parser;

import com.analyzer.modules.parser.pipeline.parser.detector.FileLanguageDetector;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.file.Path;

/**
 * 通用项目解析器
 */
@Slf4j
@Component
public class GenericProjectParser extends AbstractProjectParser {
    public GenericProjectParser(FileLanguageDetector detector) {
        super(detector);
    }
    /**
     * 永远返回 true，作为兜底解析器
     */
    @Override
    public boolean supports(Path projectRoot) {
        return true;
    }
    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }
    /**
     * 兜底解析器接受所有能识别的文件，包括 OTHER 类型
     * 这样即使语言未识别，也能作为纯文本处理
     */
    @Override
    protected boolean acceptSourceFile(SourceFile file) {
        return true;
    }
}
