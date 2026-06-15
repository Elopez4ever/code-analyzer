package com.analyzer.modules.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建项目dto")
public class ProjectCreateDTO {

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String name;

    @Schema(description = "git链接")
    @NotBlank(message = "Git仓库地址不能为空")
    private String gitUrl;
}