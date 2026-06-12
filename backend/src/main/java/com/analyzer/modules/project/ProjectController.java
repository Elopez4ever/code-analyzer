package com.analyzer.modules.project;

import com.analyzer.common.result.Result;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.modules.project.model.ProjectCreateDTO;
import com.analyzer.modules.project.model.CodeChunkVO;
import com.analyzer.modules.project.model.ProjectPageDTO;
import com.analyzer.modules.project.model.ProjectVO;
import com.analyzer.modules.project.service.ProjectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * @param createDTO
     * @return
     */
    @Operation(summary = "通过 Git 创建项目")
    @PostMapping("/create")
    public Result<ProjectPO> createFromGit(@RequestBody ProjectCreateDTO createDTO) {
        return null;
    }

    /**
     * 通过 Zip 上传项目
     * @param file zip文件
     * @param projectName 项目名
     * @return
     */
    @Operation(summary = "通过 Zip 上传项目")
    @PostMapping("/upload")
    public Result<ProjectPO> createFromZip(@RequestParam("file") MultipartFile file,
                                           @RequestParam("projectName") String projectName) {
        projectService.saveFromZip(file, projectName);
        return Result.success();
    }

    /**
     * 获取项目解析结果
     * @param projectId
     * @return
     */
    @Operation(summary = "获取项目解析结果")
    @GetMapping("/{projectId}/chunks")
    public Result<List<CodeChunkVO>> getChunks(@PathVariable String projectId) {
        return null;
    }
}
