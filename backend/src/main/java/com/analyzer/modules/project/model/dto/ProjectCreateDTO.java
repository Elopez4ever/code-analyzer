package com.analyzer.modules.project.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: Elopez
 * @Date: 5/21/2026
 */
@Data
public class ProjectCreateDTO {
    @NotBlank(message = "项目名称不能为空")
    private String name;
    @NotBlank(message = "Git仓库地址不能为空")
    private String gitUrl;
}