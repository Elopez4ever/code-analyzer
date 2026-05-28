package com.analyzer.modules.project.service;

import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.common.exception.BusinessException;
import com.analyzer.infrastructure.persistence.entity.Project;
import com.analyzer.infrastructure.persistence.entity.ProjectStatus;
import com.analyzer.infrastructure.embedding.file.FileValidationService;
import com.analyzer.modules.parser.service.ProjectParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
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

    public Project saveFromZip(MultipartFile file, String projectName) {
        // 校验文件大小
        fileValidationService.validateFile(file, MAX_FILE_SIZE);

        String fileName = file.getOriginalFilename();
        log.info("收到项目上传请求: {}, 大小: {}" , fileName , file.getSize());

        // 验证文件类型
        String contentType = fileValidationService.detectContentType(file);
        validateContentType(contentType);

        // 检查项目是否存在
        Optional<Project> existingProject = projectPersistenceService.findExistingResume(projectName);
        if (existingProject.isPresent()) {
            return existingProject.get();
        }
        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        // 解压
        unzip(file, localPath);

        // 入库
        Project project = new Project();
        project.setProjectId(projectId);
        project.setName(projectName != null ? projectName : generateDefaultProjectName());
        project.setLocalPath(localPath);
        project.setStatus(ProjectStatus.PARSING);
        projectPersistenceService.save(project);

        // 异步解析项目
        parsingService.parseAsync(projectId, localPath);

        return project;
    }

    public Project saveFromGit(String gitUrl, String projectName) {
        // 校验 URL 格式
        gitService.validateGitUrl(gitUrl);
        // 检查项目是否存在
        Optional<Project> existingProject = projectPersistenceService.findExistingResume(projectName);
        if (existingProject.isPresent()) {
            return existingProject.get();
        }
        String projectId = UUID.randomUUID().toString().replace("-", "");
        String localPath = appConfig.getUploadPath() + "/" + projectId;

        // clone 仓库
        gitService.cloneRepository(gitUrl, localPath);

        // 入库
        Project project = new Project();
        project.setProjectId(projectId);
        project.setName(projectName != null ? projectName : extractRepoName(gitUrl));
        project.setGitUrl(gitUrl);
        project.setLocalPath(localPath);
        project.setStatus(ProjectStatus.PARSING);
        projectPersistenceService.save(project);
        // 异步解析
        parsingService.parseAsync();
        return project;
    }

    /**
     * 创建默认项目名
     * @return 项目名称
     */
    private String generateDefaultProjectName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "未命名项目_" + timestamp;
    }

    /**
     * 验证文件类型
     */
    private void validateContentType(String contentType) {
        fileValidationService.validateContentTypeByList(
                contentType,
                appConfig.getAllowedTypes(),
                "不支持的文件类型: " + contentType
        );
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
