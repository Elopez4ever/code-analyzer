package com.analyzer.common.result.exception;

import com.analyzer.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常
     * @return 返回对象
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handelBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * RequestBody 校验失败
     * @param e RB异常
     * @return 返回对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    /**
     * 保底处理
     * @param e 未知异常
     * @return 返回对象
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error(500, "服务器内部异常");
    }
}
