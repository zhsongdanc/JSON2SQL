package com.zhsong.sql.controller;

import com.zhsong.sql.exception.ConverterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }
    
    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("参数错误");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleIllegalArgumentException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_PARAMETER", response.getBody().getCode());
        assertEquals("参数错误", response.getBody().getMessage());
    }
    
    @Test
    void testHandleConverterException() {
        ConverterException exception = new ConverterException("转换错误");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleConverterException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONVERTER_ERROR", response.getBody().getCode());
        assertEquals("转换错误", response.getBody().getMessage());
    }
    
    @Test
    void testHandleException() {
        Exception exception = new Exception("未知错误");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                handler.handleException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("未知错误"));
    }
}

