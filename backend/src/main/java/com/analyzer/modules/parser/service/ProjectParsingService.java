package com.analyzer.modules.parser.service;

import com.analyzer.infrastructure.persistence.entity.ProjectStatus;
import com.analyzer.modules.parser.pipeline.ProjectParsingPipeline;
import com.analyzer.modules.project.service.ProjectPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectParsingService {

    private final ProjectParsingPipeline pipeline;
    private final ProjectPersistenceService persistenceService;

    @Async
    public void parseAsync(String projectId, String projectPath) {
        try {
            pipeline.execute(projectId, projectPath);
            persistenceService.updateStatus(projectId, ProjectStatus.READY);
        } catch (Exception e) {
            log.error(e.getMessage());
            persistenceService.updateStatus(projectId, ProjectStatus.FAILED);
        }
    }
}
