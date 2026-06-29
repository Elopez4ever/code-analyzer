package com.analyzer.modules.project.service;

import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import com.analyzer.common.utils.GitUtils;
import com.analyzer.infrastructure.persistence.po.enums.UploadMethod;
import com.analyzer.infrastructure.persistence.service.ProjectPersistenceService;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.file.FileValidationService;
import com.analyzer.modules.task.service.TaskService;
import com.analyzer.modules.project.model.ProjectCreateDTO;
import com.analyzer.modules.project.model.ProjectDetailDTO;
import com.analyzer.modules.project.model.ProjectPageDTO;
import com.analyzer.modules.project.model.ProjectVO;
import com.analyzer.modules.project.progress.ProgressTracker;
import com.analyzer.modules.project.progress.ProgressTrackerFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final AppConfigProperties appConfig;
    private final FileValidationService fileValidationService;
    private final ProjectPersistenceService projectPersistenceService;
    private final TaskService taskService;
    private final GitService gitService;
    private final ZipService zipService;
    private final ProgressTrackerFactory progressTrackerFactory;

    /**
     * 通过 zip 上传项目
     *
     * @param file        文件
     * @param projectName 项目名
     */
    public void saveFromZip(MultipartFile file, String projectName) {
        fileValidationService.validateFile(file, appConfig.getMaxFileSize());
        log.info("收到项目上传请求: {}, 大小: {}", file.getOriginalFilename(), file.getSize());

        String contentType = fileValidationService.detectContentType(file);
        zipService.validateZipContentType(contentType);

        String resolvedName = projectName != null ? projectName : generateDefaultProjectName();
        String projectId = generateProjectId();
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        ProgressTracker tracker = progressTrackerFactory.create(appConfig.getTopic(), projectId);

        try {
            zipService.unzip(file, localPath, tracker.itemCallback("EXTRACTING", 10, 60));

            ProjectPO projectPO = buildProjectPO(projectId, resolvedName, null, localPath, UploadMethod.ZIP);
            tracker.step("SAVING", 60, 80, "保存项目信息", () ->
                    saveProject(projectPO, new File(localPath))
            );

            tracker.complete("项目已就绪，正在后台解析代码结构");
            taskService.submit(projectId, localPath);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            cleanup(projectId, new File(localPath));
            tracker.error("上传失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 通过 git 链接创建
     *
     * @param createDTO 创建 dto
     */
    public void saveFromGit(ProjectCreateDTO createDTO) {
        String gitUrl = createDTO.getGitUrl();
        GitUtils.validateGitUrl(gitUrl);

        String projectName = StringUtils.hasText(createDTO.getName())
                ? createDTO.getName()
                : GitUtils.extractRepoName(gitUrl);

        String projectId = generateProjectId();
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        ProgressTracker tracker = progressTrackerFactory.create(appConfig.getTopic(), projectId);

        try {
            File localDir = tracker.step("CLONING", 10, 60, "克隆仓库", () ->
                    gitService.cloneRepository(gitUrl, projectId, localPath)
            );

            ProjectPO projectPO = buildProjectPO(projectId, projectName, gitUrl, localDir.getAbsolutePath(), UploadMethod.GIT);
            tracker.step("SAVING", 60, 80, "保存项目信息", () ->
                    saveProject(projectPO, localDir)
            );

            tracker.complete("项目已就绪，正在后台解析代码结构");
            taskService.submit(projectId, localDir.getAbsolutePath());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            cleanup(projectId, new File(localPath));
            tracker.error("上传失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 分页查询项目列表
     *
     * @param pageDTO 分页 dto
     * @return 查询结果
     */
    public Page<ProjectVO> listPage(ProjectPageDTO pageDTO) {
        Page<ProjectPO> result = projectPersistenceService.listPage(pageDTO.getPage(), pageDTO.getSize(), pageDTO.getProjectName());
        Page<ProjectVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 修改项目
     *
     * @param projectDetailDTO 项目细节dto
     */
    public void modifyProject(ProjectDetailDTO projectDetailDTO) {
        String projectId = projectDetailDTO.getProjectId();
        ProjectPO po = projectPersistenceService.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (StringUtils.hasText(projectDetailDTO.getName())) {
            po.setName(projectDetailDTO.getName());
        }
        if (StringUtils.hasText(projectDetailDTO.getGitUrl())) {
            po.setGitUrl(projectDetailDTO.getGitUrl());
        }

        try {
            projectPersistenceService.update(po);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT);
        }
    }

    /**
     * 批量删除项目
     *
     * @param projectIds 项目ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }

        // 仅标记状态
        projectPersistenceService.batchUpdateStatus(projectIds, ProjectStatus.DELETED);
    }

    /**
     * 生成项目 ID
     */
    private String generateProjectId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成默认项目名
     */
    private String generateDefaultProjectName() {
        return "未命名项目_" + System.currentTimeMillis();
    }

    /**
     * 构建项目po
     */
    private ProjectPO buildProjectPO(String projectId, String name, String gitUrl, String localPath, UploadMethod method) {
        ProjectPO po = new ProjectPO();
        po.setProjectId(projectId);
        po.setName(name);
        po.setGitUrl(gitUrl);
        po.setLocalPath(localPath);
        po.setStatus(ProjectStatus.PARSING);
        po.setMethod(method);
        return po;
    }

    /**
     * 保存项目
     */
    private void saveProject(ProjectPO projectPO, File cleanupTarget) {
        try {
            projectPersistenceService.save(projectPO);
        } catch (DuplicateKeyException e) {
            FileUtils.deleteQuietly(cleanupTarget);
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT);
        } catch (Exception e) {
            FileUtils.deleteQuietly(cleanupTarget);
            throw e;
        }
    }

    /**
     * 清理本地文件
     */
    private void cleanup(String projectId, File localDir) {
        FileUtils.deleteQuietly(localDir);
        try {
            projectPersistenceService.deleteById(projectId);
        } catch (Exception ignored) {
            // 可能尚未入库，忽略
        }
    }

    /**
     * 转换为vo
     */
    private ProjectVO convertToVO(ProjectPO po) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(po, vo);
        vo.setStatus(po.getStatus().getValue());
        vo.setUploadMethod(po.getMethod().getValue());
        return vo;
    }
}