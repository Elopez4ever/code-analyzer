package com.analyzer.infrastructure.persistence.po;

import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectPO {
    private String id;
    private String projectId;       // 业务 ID，UUID
    private String name;            // 项目名（从 git url 或 zip 文件名提取）
    private String gitUrl;          // git 地址，upload 方式为 null
    private String localPath;       // clone/解压后的本地路径
    private ProjectStatus status;
    private Integer chunkCount;     // 解析出的 chunk 总数
    private String errorMessage;    // 失败时的错误信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
