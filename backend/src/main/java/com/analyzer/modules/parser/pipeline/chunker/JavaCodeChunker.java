package com.analyzer.modules.parser.pipeline.chunker;


import com.analyzer.modules.parser.pipeline.CodeChunker;
import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;
import com.analyzer.modules.parser.pipeline.domain.SourceFile;

import java.util.List;

public class JavaCodeChunker implements CodeChunker {
    @Override
    public boolean supports(FileLanguage language) {
        return false;
    }

    @Override
    public List<CodeChunk> chunk(SourceFile sourceFile, String projectId) {
        return List.of();
    }
}
