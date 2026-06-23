package com.analyzer.modules.parser.pipeline.domain;

/**
 * 代码语义角色
 */
public enum CodeRole {
    // OOP
    CLASS_DECLARATION,
    METHOD,
    INTERFACE_DECLARATION,
    ENUM_DECLARATION,

    // non OOP
    FUNCTION,
    TYPE_DEFINITION,

    // config
    CONFIG_BLOCK,
    SQL_STATEMENT,

    // 未识别
    UNKNOWN
}
