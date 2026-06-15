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
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectPersistenceService {

    private final ProjectMapper projectMapper;

    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目信息
     */
    public Optional<ProjectPO> findById(String id) {
        return Optional.ofNullable(projectMapper.selectOne(
                new LambdaQueryWrapper<ProjectPO>()
                        .eq(ProjectPO::getProjectId, id)
                        .last("LIMIT 1")
        ));
    }

    /**
     * 根据名称查询项目
     * @param name 项目名称
     * @return 项目信息
     */
    public Optional<ProjectPO> findByName(String name) {
        return Optional.ofNullable(projectMapper.selectOne(
                new LambdaQueryWrapper<ProjectPO>()
                        .eq(ProjectPO::getName, name)
                        .last("LIMIT 1")
        ));
    }

    /**
     * 根据Git地址查询项目
     * @param gitUrl Git仓库地址
     * @return 项目信息
     */
    public Optional<ProjectPO> findByGitUrl(String gitUrl) {
        return Optional.ofNullable(projectMapper.selectOne(
                new LambdaQueryWrapper<ProjectPO>()
                        .eq(ProjectPO::getGitUrl, gitUrl)
                        .last("LIMIT 1")
        ));
    }

    /**
     * 保存项目
     * @param projectPO 项目PO
     */
    public void save(ProjectPO projectPO) {
        projectMapper.insert(projectPO);
    }

    /**
     * 更新项目信息
     * @param projectPO 项目PO
     */
    public void update(ProjectPO projectPO) {
        projectMapper.update(
                projectPO,
                new LambdaUpdateWrapper<ProjectPO>()
                        .eq(ProjectPO::getProjectId, projectPO.getProjectId())
        );
    }

    /**
     * 更新项目状态
     * @param projectId 项目ID
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
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public Page<ProjectPO> listPage(int pageNum, int pageSize) {
        return projectMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProjectPO>()
                        .orderByDesc(ProjectPO::getCreatedAt)
        );
    }

    /**
     * 根据ID删除项目
     * @param projectId 项目ID
     */
    public void deleteById(String projectId) {
        projectMapper.delete(
                new LambdaQueryWrapper<ProjectPO>()
                        .eq(ProjectPO::getProjectId, projectId)
        );
    }

    /**
     * 根据ID批量查询项目
     * @param projectIds 项目ID列表
     * @return 项目列表
     */
    public List<ProjectPO> findAllByIds(List<String> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return projectMapper.selectList(
                new LambdaQueryWrapper<ProjectPO>()
                        .in(ProjectPO::getProjectId, projectIds)
        );
    }
}