package com.analyzer.modules.knowledgebase.enricher.extractor;

import com.analyzer.modules.parser.pipeline.domain.CodeChunk;
import com.analyzer.modules.parser.pipeline.domain.CodeRole;
import com.analyzer.modules.parser.pipeline.domain.FileLanguage;

import java.util.Set;

public interface StructureExtractor {

    Set<CodeRole> supportedRoles();

    /** 默认不限语言，子类按需覆盖 */
    default Set<FileLanguage> supportedLanguages() {
        return Set.of();
    }

    /** 空集合表示不限语言 */
    default boolean supportsLanguage(FileLanguage language) {
        Set<FileLanguage> supported = supportedLanguages();
        return supported.isEmpty() || supported.contains(language);
    }

    void extract(CodeChunk chunk);
}
