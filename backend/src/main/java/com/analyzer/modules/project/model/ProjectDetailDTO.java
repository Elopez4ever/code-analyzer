package com.analyzer.modules.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Project修改DTO")
public class ProjectDetailDTO {

    @Schema(description = "项目id")
    @NotBlank
    private String projectId;

    @Schema(description = "项目名称", example = "example")
    private String name;

    @Schema(description = "git链接")
    private String gitUrl;
}
