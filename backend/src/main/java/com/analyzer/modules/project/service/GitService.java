package com.analyzer.modules.project.service;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.common.result.exception.ErrorCode;
import com.analyzer.common.utils.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class GitService {

    /**
     * 克隆项目
     */
    public File cloneRepository(String gitUrl, String projectId, String uploadPath) {
        GitUtils.validateGitUrl(gitUrl);
        File localDir = new File(uploadPath, projectId);
        try {
            Git.cloneRepository()
                    .setURI(gitUrl)
                    .setDirectory(localDir)
                    .setCloneAllBranches(false)
                    .call()
                    .close();
        } catch (GitAPIException e) {
            FileUtils.deleteQuietly(localDir);
            log.error(e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLONE_ERROR);
        }
        return localDir;
    }
}