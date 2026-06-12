package com.analyzer.infrastructure.persistence.service;

import com.analyzer.infrastructure.persistence.mapper.ProjectMapper;
import com.analyzer.infrastructure.persistence.po.enums.ProjectStatus;
import com.analyzer.infrastructure.persistence.po.ProjectPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPersistenceService {

    private final ProjectMapper projectMapper;

    /**
     * 检查简历是否已存在（基于项目名称）
     * @param projectName 项目名
     * @return 如果存在返回已有的简历实体，否则返回空
     */
    public Optional<ProjectPO> findExistingResume(String projectName) {
        ProjectPO result = projectMapper.selectOne(
                new LambdaQueryWrapper<ProjectPO>()
                        .eq(ProjectPO::getName, projectName)
                        .last("LIMIT 1")
        );
        return Optional.ofNullable(result);
    }

    /**
     * 保存项目
     * @param projectPO 项目po
     */
    public void save(ProjectPO projectPO) {
        projectMapper.insert(projectPO);
    }

    /**
     * 更新项目状态
     * @param projectId 项目id
     * @param status 新状态
     */
    public void updateStatus(String projectId, ProjectStatus status) {
        projectMapper.update(
                new LambdaUpdateWrapper<ProjectPO>()
                        .eq(ProjectPO::getProjectId, projectId)
                        .set(ProjectPO::getStatus, status)
                        .set(ProjectPO::getUpdatedAt, LocalDateTime.now())
        );
    }

    /**
     * 分页查询项目
     * @param pageNum 页数
     * @param pageSize 页大小
     * @return 查询结果
     */
    public Page<ProjectPO> listPage(int pageNum, int pageSize) {
        return projectMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProjectPO>()
                        .orderByDesc(ProjectPO::getCreatedAt)
        );
    }
}
