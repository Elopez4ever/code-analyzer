package com.analyzer.modules.project.service;

import com.analyzer.common.result.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.analyzer.common.result.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class ZipService {
    private static final long MAX_UNCOMPRESSED_SIZE = 500L * 1024 * 1024; // 500MB
    private static final int MAX_ENTRY_COUNT = 10_000;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/zip",
            "application/x-zip-compressed"
    );

    /**
     * 校验文件是否为合法的 Zip 文件
     * @param contentType 文件类型
     */
    public void validateZipContentType(String contentType) {
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 解压 ZIP 到指定目录
     * @param file 文件
     * @param destDir 目录
     */
    public void unzip(MultipartFile file, String destDir) {
        Path destPath = Paths.get(destDir);
        long totalSize = 0;
        int entryCount = 0;
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 防止条目数过多
                if (++entryCount > MAX_ENTRY_COUNT) {
                    throw new BusinessException(ErrorCode.ZIP_TOO_MANY_ENTRIES);
                }
                Path filePath = destPath.resolve(entry.getName()).normalize();
                // 防止 zip slip 攻击, 防止恶意条目, 如 ../../../../etc/cron.d/malicious
                if (!filePath.startsWith(destPath)) {
                    throw new BusinessException(ErrorCode.ZIP_ILLEGAL_ENTRY);
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    // 防止解压文件过大，限制单次写入总量
                    totalSize += Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    if (totalSize > MAX_UNCOMPRESSED_SIZE) {
                        throw new BusinessException(ErrorCode.ZIP_UNCOMPRESSED_TOO_LARGE);
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ZIP_EXTRACT_FAILED);
        }
    }
}
