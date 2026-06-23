package com.analyzer.modules.parser.pipeline.parser;

import com.analyzer.modules.parser.pipeline.parser.detector.FileLanguageDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@Component
public class MavenGradleProjectParser extends AbstractProjectParser {
    protected MavenGradleProjectParser(FileLanguageDetector detector) {
        super(detector);
    }

    @Override
    public boolean supports(Path projectRoot) {
        return false;
    }

    @Override
    public int priority() {
        return 10;
    }
}
