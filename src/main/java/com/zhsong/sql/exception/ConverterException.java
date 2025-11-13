package com.zhsong.sql.exception;

/**
 * 转换器异常
 */
public class ConverterException extends RuntimeException {
    
    public ConverterException(String message) {
        super(message);
    }
    
    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}

