package com.analyzer.common.result.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误 ==========
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== Project Module 错误 ==========
    DUPLICATE_PROJECT(405, "项目已存在"),
    CLONE_ERROR(406, "克隆项目时出现错误"),
    INVALID_FILE_TYPE(407, "上传的文件类型不支持"),
    ZIP_TOO_MANY_ENTRIES(408, "文件条目数过多"),
    ZIP_ILLEGAL_ENTRY(409, "非法的条目名"),
    ZIP_UNCOMPRESSED_TOO_LARGE(410, "上传文件过大"),
    ZIP_EXTRACT_FAILED(411, "文件解压失败"),
    PROJECT_NOT_FOUND(412, "项目不存在"),

    // ========== Parser Module 错误 ==========
    PARSER_NOT_FOUND(413, "未找到符合项目的Parser"),
    PARSE_FAILED(414, "解析出错"),


    // ========== Chat Module 错误 =========
    MODEL_NOT_AVAILABLE(415, "模型不可用"),
    EMBED_FAILED(416, "Embedding 批量调用失败"),
    CHAT_FAILED(417, "Chat调用失败"),
    STREAM_FAILED(418, "Stream 初始化失败"),
    ;

    private final Integer code;
    private final String message;
}

