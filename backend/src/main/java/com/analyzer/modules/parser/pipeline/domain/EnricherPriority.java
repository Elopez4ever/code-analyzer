package com.analyzer.modules.parser.pipeline.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnricherPriority {
    METADATA(10),
    KEYWORD(20),
    SUMMARY(100);
    private final int order;
}
