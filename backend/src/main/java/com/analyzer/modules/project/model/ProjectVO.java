package com.analyzer.modules.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "")
public class ProjectVO {
    private String projectId;
    private String name;
    private Integer status;
    private Integer uploadMethod;
    private String gitUrl;
    private LocalDateTime updatedAt;
}
