package com.analyzer.modules.project;

import com.analyzer.common.result.Result;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.modules.project.model.dto.ProjectCreateDTO;
import com.analyzer.modules.project.model.vo.CodeChunkVO;
import com.analyzer.modules.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @PostMapping("/create")
    public Result<ProjectPO> createFromGit(@RequestBody ProjectCreateDTO createDTO) {
        return null;
    }

    @PostMapping("/upload")
    public Result<ProjectPO> createFromZip(@RequestParam("file") MultipartFile file,
                                           @RequestParam("projectName") String projectName) {
        return Result.success(projectService.saveFromZip(file, projectName));
    }

    // 3. 查看项目解析结果
    @GetMapping("/{projectId}/chunks")
    public Result<List<CodeChunkVO>> getChunks(@PathVariable String projectId) {
        return null;
    }
}
