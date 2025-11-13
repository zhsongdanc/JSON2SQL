package com.zhsong.sql.controller;

import com.zhsong.sql.exception.ConverterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("INVALID_PARAMETER");
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(ConverterException.class)
    public ResponseEntity<ErrorResponse> handleConverterException(ConverterException e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("CONVERTER_ERROR");
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse response = new ErrorResponse();
        response.setCode("INTERNAL_ERROR");
        response.setMessage("内部服务器错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @lombok.Data
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}

