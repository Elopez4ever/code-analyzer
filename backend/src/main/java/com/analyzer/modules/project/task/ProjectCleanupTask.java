package com.analyzer.modules.project.task;

import cn.hutool.core.util.ArrayUtil;
import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.persistence.service.ProjectPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 定时任务, 异步清理删除后的项目
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCleanupTask {
    private final ProjectPersistenceService projectPersistenceService;
    private final AppConfigProperties appConfigProperties;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanup() {
        List<ProjectPO> deletedProjects = projectPersistenceService.findByStatus(ProjectStatus.DELETED);
        if (ArrayUtil.isEmpty(deletedProjects)) {
            return;
        }

        log.info("开始清理已删除项目, 数量: {}", deletedProjects.size());
        for (ProjectPO project : deletedProjects) {
            try {
                // 删除本地文件
                if (project.getLocalPath() != null) {
                    File localDir = new File(project.getLocalPath());
                    if (localDir.exists()) {
                        FileUtils.deleteQuietly(localDir);
                        log.info("已清理项目本地文件: projectId={}, path={}", project.getProjectId(), project.getLocalPath());
                    }
                }
                // 从数据库删除记录
                projectPersistenceService.deleteById(project.getProjectId());
                log.info("已清理项目数据库记录: projectId={}", project.getProjectId());
            } catch (Exception e) {
                log.error("清理项目失败: projectId={}, error={}", project.getProjectId(), e.getMessage(), e);
                // 失败直接跳过
            }
        }
        log.info("本轮项目清理完成");
    }
}
