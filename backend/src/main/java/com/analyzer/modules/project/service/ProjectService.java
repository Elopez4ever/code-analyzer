package com.analyzer.modules.project.service;

import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.common.exception.BusinessException;
import com.analyzer.common.exception.ErrorCode;
import com.analyzer.infrastructure.persistence.service.ProjectPersistenceService;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.file.FileValidationService;
import com.analyzer.modules.parser.service.ProjectParsingService;
import com.analyzer.modules.project.model.ProjectPageDTO;
import com.analyzer.modules.project.model.ProjectVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final AppConfigProperties appConfig;
    private final FileValidationService fileValidationService;
    private final ProjectPersistenceService projectPersistenceService;
    private final ProjectParsingService parsingService;
    private final GitService gitService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 通过 zip 上传项目
     * @param file zip文件
     * @param projectName 项目名
     */
    public void saveFromZip(MultipartFile file, String projectName) {
        // 校验文件大小
        fileValidationService.validateFile(file, MAX_FILE_SIZE);

        String fileName = file.getOriginalFilename();
        log.info("收到项目上传请求: {}, 大小: {}" , fileName , file.getSize());

        // 验证文件类型
        // TODO: 验证文件类型设计问题
        String contentType = fileValidationService.detectContentType(file);
//        validateContentType(contentType);

        // 检查项目是否存在
        Optional<ProjectPO> existingProject = projectPersistenceService.findExistingResume(projectName);
        if (existingProject.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROJECT);
        }
        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        // 解压
        unzip(file, localPath);

        // 入库
        ProjectPO projectPO = new ProjectPO();
        projectPO.setProjectId(projectId);
        projectPO.setName(projectName != null ? projectName : generateDefaultProjectName());
        projectPO.setLocalPath(localPath);
        projectPO.setStatus(ProjectStatus.PARSING);
        projectPersistenceService.save(projectPO);

        // 异步解析项目
        parsingService.parseAsync(projectId, localPath);
    }

    /**
     * 通过 git 链接上传
     * @param gitUrl git 链接
     * @param projectName 项目名
     * @return project
     */
    public ProjectPO saveFromGit(String gitUrl, String projectName) {
        // 校验 URL 格式
        gitService.validateGitUrl(gitUrl);
        // 检查项目是否存在
        Optional<ProjectPO> existingProject = projectPersistenceService.findExistingResume(projectName);
        if (existingProject.isPresent()) {
            return existingProject.get();
        }
        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        // clone 仓库
        gitService.cloneRepository(gitUrl, localPath);

        // 入库
        ProjectPO projectPO = new ProjectPO();
        projectPO.setProjectId(projectId);
        projectPO.setName(projectName != null ? projectName : extractRepoName(gitUrl));
        projectPO.setGitUrl(gitUrl);
        projectPO.setLocalPath(localPath);
        projectPO.setStatus(ProjectStatus.PARSING);
        projectPersistenceService.save(projectPO);
        // 异步解析
        parsingService.parseAsync(projectId, localPath);
        return projectPO;
    }

    /**
     * 分页查询项目列表
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
     * 创建默认项目名
     * @return 项目名称
     */
    private String generateDefaultProjectName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "未命名项目_" + timestamp;
    }

    // TODO: unzip Service?
    /**
     * 解压 zip 到指定目录
     */
    private void unzip(MultipartFile file, String destDir) {
        Path destPath = Paths.get(destDir);
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = destPath.resolve(entry.getName()).normalize();
                // 防止 zip slip 攻击
                if (!filePath.startsWith(destPath)) {
                    throw new BusinessException("非法的 zip 条目: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new BusinessException("解压失败: " + e.getMessage());
        }
    }

    private String extractRepoName(String gitUrl) {
        // https://github.com/user/repo.git → repo
        String name = gitUrl.substring(gitUrl.lastIndexOf('/') + 1);
        return name.replace(".git", "");
    }
}
