package com.analyzer.modules.project.service;

import com.analyzer.infrastructure.persistence.entity.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ProjectPersistenceService {
    // TODO：名称查重项目
    /**
     * 检查简历是否已存在（基于项目名称）
     *
     * @param projectName 项目名
     * @return 如果存在返回已有的简历实体，否则返回空
     */
    public Optional<Project> findExistingResume(String projectName) {
        return Optional.ofNullable(null);
    }

    public void save(Project project) {

    }
}
