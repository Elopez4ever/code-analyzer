package com.analyzer.modules.project.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDeleteDTO {

    @NotEmpty(message = "项目ID列表不能为空")
    private List<String> projectIds;
}