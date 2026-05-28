package com.analyzer.infrastructure.persistence.entity;

public enum ProjectStatus {
    PARSING,     // 异步解析代码结构中
    READY,       // 可以 chat
    FAILED       // 任一阶段失败
}
