package com.analyzer.infrastructure.ai.exception;

import com.analyzer.common.result.exception.ErrorCode;
import lombok.Getter;

/**
 * AI 服务异常
 */
@Getter
public class AIServiceException extends RuntimeException {

    private final Integer code;
    private final boolean retryable;

    public AIServiceException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.code = ErrorCode.INTERNAL_ERROR.getCode();
        this.retryable = retryable;
    }

    public AIServiceException(ErrorCode errorCode, String message, boolean retryable) {
        super(message);
        this.code = errorCode.getCode();
        this.retryable = retryable;
    }
}
