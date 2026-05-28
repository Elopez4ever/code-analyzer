package com.analyzer.infrastructure.file;

import com.analyzer.common.exception.BusinessException;
import com.analyzer.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Service
@Slf4j
public class FileValidationService {

    private final Tika tika;

    public FileValidationService() {
        tika = new Tika();
    }

    /**
     * 验证文件基本属性（是否为空、文件大小）
     *
     * @param file 上传的文件
     * @param maxSizeBytes 最大文件大小（字节）
     */
    public void validateFile(MultipartFile file, long maxSizeBytes) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要上传的文件");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件大小超过限制");
        }
    }

    /**
     * 验证文件类型（基于MIME类型）
     *
     * @param contentType 文件的MIME类型
     * @param allowedTypes 允许的MIME类型列表（支持部分匹配，如"pdf"会匹配"application/pdf"）
     * @param errorMessage 验证失败时的错误消息
     */
    public void validateContentTypeByList(String contentType, List<String> allowedTypes, String errorMessage) {
        if (!isAllowedType(contentType, allowedTypes)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    errorMessage != null ? errorMessage : "不支持的文件类型: " + contentType);
        }
    }

    /**
     * 检测文件的 MIME 类型
     * 使用 Tika 进行基于内容的检测，比 HTTP 头部更准确
     *
     * @param file MultipartFile 文件
     * @return MIME 类型字符串
     */
    public String detectContentType(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream, file.getOriginalFilename());
        } catch (IOException e) {
            log.warn("无法检测文件类型，使用 Content-Type 头部: {}", e.getMessage());
            return file.getContentType();
        }
    }

    /**
     * 检查文件类型是否在允许列表中
     */
    private boolean isAllowedType(String contentType, List<String> allowedTypes) {
        if (contentType == null || allowedTypes == null || allowedTypes.isEmpty()) {
            return false;
        }

        String lowerContentType = contentType.toLowerCase();
        return allowedTypes.stream()
                .anyMatch(allowed -> {
                    String lowerAllowed = allowed.toLowerCase();
                    return lowerContentType.contains(lowerAllowed) || lowerAllowed.contains(lowerContentType);
                });
    }
}
