package com.analyzer.infrastructure.persistence.po;

import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.persistence.po.enums.UploadMethod;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project")
public class ProjectPO {
    @TableId("project_id")
    private String projectId;
    private String name;
    private String gitUrl;
    private String localPath;
    private ProjectStatus status;
    private UploadMethod method;
    private Integer chunkCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
