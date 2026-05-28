package com.analyzer.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: Elopez
 * @Date: 5/21/2026
 */
@Data
public class CodeChunk {
    private Long id;
    private String projectId;       // 关联项目
    private String filePath;        // 相对路径：src/main/java/com/example/UserService.java
    private String className;       // 所属类名：UserService
    private String methodName;      // 方法名：getUserById，类级别 chunk 为 null
    private String chunkType;       // CLASS_DECLARATION, METHOD, FIELD, IMPORT_BLOCK
    private String content;         // 代码原文
    private Integer startLine;      // 起始行号
    private Integer endLine;        // 结束行号
    private Integer tokenCount;     // 粗估 token 数（用于后续控制上下文长度）
    private LocalDateTime createdAt;
}
