package com.analyzer.common.result;

import com.analyzer.common.constant.CommonConstants;
import com.analyzer.common.result.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;


@Getter
public class Result<T> {

    private final Integer code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功响应 ==========

    public static <T> Result<T> success() {
        return new Result<>(CommonConstants.StatusCode.SUCCESS, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CommonConstants.StatusCode.SUCCESS, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CommonConstants.StatusCode.SUCCESS, message, data);
    }

    // ========== 失败响应 ==========

    public static <T> Result<T> error(String message) {
        return new Result<>(CommonConstants.StatusCode.SERVER_ERROR, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    // ========== 辅助方法 ==========

    public boolean checkSuccess() {
        return CommonConstants.StatusCode.SUCCESS == this.code;
    }
}
