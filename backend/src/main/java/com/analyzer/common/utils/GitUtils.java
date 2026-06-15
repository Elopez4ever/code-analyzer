package com.analyzer.common.utils;

import com.analyzer.common.result.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitUtils {

    private static final Pattern REPO_NAME_PATTERN = Pattern.compile("[/:]([^/:]+?)(?:\\.git)?/?$");

    /**
     * 校验 Git 链接格式
     * @param gitUrl git链接
     */
    public static void validateGitUrl(String gitUrl) {
        if (!StringUtils.hasText(gitUrl)) {
            throw new BusinessException("git URL 不能为空");
        }
        if (!gitUrl.matches("^https?://.+\\.git$") && !gitUrl.matches("^git@.+:.+\\.git$")) {
            throw new BusinessException("无效的 git URL: " + gitUrl);
        }
    }

    /**
     * 从 git URL 提取仓库名
     * @param gitUrl git链接
     * @return 仓库名
     */
    public static String extractRepoName(String gitUrl) {
        if (!StringUtils.hasText(gitUrl)) {
            throw new BusinessException("git URL 不能为空");
        }
        Matcher matcher = REPO_NAME_PATTERN.matcher(gitUrl.trim());
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        throw new BusinessException("无法从 git URL 中解析仓库名: " + gitUrl);
    }
}