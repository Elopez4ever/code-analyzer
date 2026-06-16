package com.analyzer.modules.project;

import com.analyzer.common.result.Result;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.modules.project.model.*;
import com.analyzer.modules.project.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目管理")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 分页获取项目
     * @return 分页结果
     */
    @Operation(summary = "分页获取项目列表")
    @GetMapping("/")
    public Result<Page<ProjectVO>> list(@Validated ProjectPageDTO projectPageDTO) {
        return Result.success(projectService.listPage(projectPageDTO));
    }

    /**
     * 通过 Git 创建项目
     * @param createDTO 项目创建dto
     * @return 创建成功则返回 success
     */
    @Operation(summary = "通过 Git 创建项目")
    @PostMapping("/create")
    public Result<?> createFromGit(@Validated @RequestBody ProjectCreateDTO createDTO) {
        projectService.saveFromGit(createDTO);
        return Result.success();
    }

    /**
     * 通过 Zip 上传项目
     * @param file zip文件
     * @param projectName 项目名
     * @return 创建成功则返回 success
     */
    @Operation(summary = "通过 Zip 上传项目")
    @PostMapping("/upload")
    public Result<ProjectPO> createFromZip(@RequestParam("file") MultipartFile file,
                                           @RequestParam("projectName") String projectName) {
        projectService.saveFromZip(file, projectName);
        return Result.success();
    }


    /**
     * 批量删除
     * @param projectDeleteDTO 项目删除dto
     * @return 删除结果
     */
    @Operation(summary = "批量删除项目")
    @PostMapping("/delete")
    public Result<?> deleteBatchProject(@Validated @RequestBody ProjectDeleteDTO projectDeleteDTO) {
        projectService.deleteBatch(projectDeleteDTO.getProjectIds());
        return Result.success();
    }

    /**
     * 修改项目详细信息
     * @param projectDetailDTO 项目细节dto
     * @return 结果
     */
    @Operation
    @PostMapping("/modify")
    public Result<?> modifyProject(@Validated ProjectDetailDTO projectDetailDTO) {
        projectService.modifyProject(projectDetailDTO);
        return Result.success();
    }
}
