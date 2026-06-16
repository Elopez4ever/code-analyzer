package com.analyzer.modules.project.service;

import com.analyzer.common.result.exception.BusinessException;
import com.analyzer.modules.project.progress.ItemProgressCallback;
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
     * @param file zip文件
     * @param destDir 基目录
     * @param callback 回调函数
     */
    public void unzip(MultipartFile file, String destDir, ItemProgressCallback callback) {
        Path destPath = Paths.get(destDir);
        long totalSize = 0;
        int entryCount = 0;
        int fileCount = 0;
        int totalFiles = countFileEntries(file);
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (++entryCount > MAX_ENTRY_COUNT) {
                    throw new BusinessException(ErrorCode.ZIP_TOO_MANY_ENTRIES);
                }
                Path filePath = destPath.resolve(entry.getName()).normalize();
                if (!filePath.startsWith(destPath)) {
                    throw new BusinessException(ErrorCode.ZIP_ILLEGAL_ENTRY);
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    totalSize += Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                    if (totalSize > MAX_UNCOMPRESSED_SIZE) {
                        throw new BusinessException(ErrorCode.ZIP_UNCOMPRESSED_TOO_LARGE);
                    }
                    fileCount++;
                    if (callback != null) {
                        callback.onItem(entry.getName(), fileCount, totalFiles);
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ZIP_EXTRACT_FAILED);
        }
    }

    /**
     * 预先统计文件条目数
     */
    private int countFileEntries(MultipartFile file) {
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    count++;
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            return 0;
        }
        return count;
    }
}
