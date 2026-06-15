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
import com.analyzer.modules.parser.service.ProjectParsingService;
import com.analyzer.modules.project.model.ProjectCreateDTO;
import com.analyzer.modules.project.model.ProjectDetailDTO;
import com.analyzer.modules.project.model.ProjectPageDTO;
import com.analyzer.modules.project.model.ProjectVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final ProjectParsingService parsingService;
    private final GitService gitService;
    private final ZipService zipService;

    /**
     * 通过 zip 上传项目
     */
    public void saveFromZip(MultipartFile file, String projectName) {
        fileValidationService.validateFile(file, appConfig.getMaxFileSize());
        log.info("收到项目上传请求: {}, 大小: {}", file.getOriginalFilename(), file.getSize());

        String contentType = fileValidationService.detectContentType(file);
        zipService.validateZipContentType(contentType);

        String resolvedName = projectName != null ? projectName : generateDefaultProjectName();
        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        zipService.unzip(file, localPath);
        try {
            ProjectPO projectPO = new ProjectPO();
            projectPO.setProjectId(projectId);
            projectPO.setName(resolvedName);
            projectPO.setLocalPath(localPath);
            projectPO.setStatus(ProjectStatus.PARSING);
            projectPO.setMethod(UploadMethod.ZIP);
            projectPersistenceService.save(projectPO);
        } catch (DuplicateKeyException e) {
            FileUtils.deleteQuietly(new File(localPath));
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT);
        } catch (Exception e) {
            FileUtils.deleteQuietly(new File(localPath));
            throw e;
        }

        parsingService.parseAsync(projectId, localPath);
    }

    /**
     * 通过 git 链接上传
     */
    public void saveFromGit(ProjectCreateDTO createDTO) {
        String gitUrl = createDTO.getGitUrl();
        GitUtils.validateGitUrl(gitUrl);

        String projectName = StringUtils.hasText(createDTO.getName())
                ? createDTO.getName()
                : GitUtils.extractRepoName(gitUrl);

        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        File localDir = gitService.cloneRepository(gitUrl, projectId, localPath);
        try {
            ProjectPO projectPO = new ProjectPO();
            projectPO.setProjectId(projectId);
            projectPO.setName(projectName);
            projectPO.setGitUrl(gitUrl);
            projectPO.setLocalPath(localDir.getAbsolutePath());
            projectPO.setStatus(ProjectStatus.PARSING);
            projectPO.setMethod(UploadMethod.GIT);
            projectPersistenceService.save(projectPO);
        } catch (DuplicateKeyException e) {
            FileUtils.deleteQuietly(localDir);
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT);
        } catch (Exception e) {
            FileUtils.deleteQuietly(localDir);
            throw e;
        }

//        parsingService.parseAsync(projectId, localDir.getAbsolutePath());
    }

    /**
     * 分页查询项目列表
     *
     * @param pageDTO 分页 dto
     * @return 查询结果
     */
    public Page<ProjectVO> listPage(ProjectPageDTO pageDTO) {
        Page<ProjectPO> result = projectPersistenceService.listPage(pageDTO.getPage(), pageDTO.getSize());
        Page<ProjectVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(po -> {
                    ProjectVO vo = new ProjectVO();
                    BeanUtils.copyProperties(po, vo);
                    vo.setStatus(po.getStatus().getValue());
                    vo.setUploadMethod(po.getMethod().getValue());
                    return vo;
                })
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
    public void deleteBatch(List<String> projectIds) {
        // 如果什么都没传, 直接返回成功
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }

        List<ProjectPO> projects = projectPersistenceService.findAllByIds(projectIds);
        Path allowedRoot = Paths.get(appConfig.getUploadPath()).toAbsolutePath().normalize();

        for (ProjectPO po : projects) {
            // 先删库记录, 再删文件
            projectPersistenceService.deleteById(po.getProjectId());

            if (StringUtils.hasText(po.getLocalPath())) {
                Path target = Paths.get(po.getLocalPath()).toAbsolutePath().normalize();
                if (!target.startsWith(allowedRoot)) {
                    log.warn("拒绝删除范围外的路径, 项目ID: {}, 路径: {}", po.getProjectId(), target);
                    continue;
                }
                try {
                    FileUtils.deleteDirectory(target.toFile());
                } catch (IOException e) {
                    log.error("删除目录失败, 项目ID: {}, 路径: {}", po.getProjectId(), target, e);
                }
            }
        }
    }

    /**
     * 创建默认项目名
     *
     * @return 项目名称
     */
    private String generateDefaultProjectName() {
        return "未命名项目_" + System.currentTimeMillis();
    }
}