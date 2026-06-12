package com.analyzer.modules.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "分页查询参数")
public class ProjectPageDTO {

    @Schema(description = "项目查询页码, 默认为1", defaultValue = "1", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "项目查询每页条数, 默认为100", defaultValue = "10", example = "10")
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer size = 10;
}
