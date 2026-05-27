package com.analyzer.modules.project.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.analyzer.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GitService {

    /**
     * 校验Git链接是否格式正确
     * @param gitUrl git链接
     */
    public void validateGitUrl(String gitUrl) {
        if (ObjectUtil.isNull(gitUrl) || StrUtil.isBlank(gitUrl)) {
            throw new BusinessException("git URL 不能为空");
        }
        if (!gitUrl.matches("^https?://.+\\.git$") && !gitUrl.matches("^git@.+:.+\\.git$")) {
            throw new BusinessException("无效的 git URL: " + gitUrl);
        }
    }

    /**
     * 克隆项目
     * @param gitUrl git链接
     * @param localPath 目标本地路径
     */
    public void cloneRepository(String gitUrl, String localPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "clone", "--depth", "1", gitUrl, localPath);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException("git clone 失败, exitCode: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new BusinessException("git clone 异常: " + e.getMessage());
        }
    }
}
