package com.analyzer.modules.project.task;

import com.analyzer.common.config.AppConfigProperties;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.analyzer.infrastructure.persistence.service.ProjectPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    @Scheduled(fixedRate = 5000)
    public void cleanup() {
        List<ProjectPO> byStatus = projectPersistenceService.findByStatus();
    }p
}
