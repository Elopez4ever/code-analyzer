package com.analyzer.modules.project.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {
    private String projectId;
    private String name;
    private Integer status;
    private Integer uploadMethod;
    private String gitUrl;
    private LocalDateTime updatedAt;
}
