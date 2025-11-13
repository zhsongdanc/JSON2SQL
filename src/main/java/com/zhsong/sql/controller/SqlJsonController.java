package com.zhsong.sql.controller;

import com.zhsong.sql.dto.SqlJsonDto;
import com.zhsong.sql.service.SqlJsonConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SQL和JSON转换控制器
 */
@RestController
@RequestMapping("/api/sql-json")
public class SqlJsonController {
    
    @Autowired
    private SqlJsonConverterService converterService;
    
    /**
     * SQL转JSON
     * POST /api/sql-json/sql-to-json
     * 
     * 请求体示例:
     * {
     *   "sql": "SELECT * FROM users WHERE id = 1",
     *   "dbType": "mysql"
     * }
     */
    @PostMapping("/sql-to-json")
    public SqlJsonDto sqlToJson(@RequestBody SqlToJsonRequest request) {
        return converterService.sqlToJson(request.getSql(), request.getDbType());
    }
    
    /**
     * JSON转SQL
     * POST /api/sql-json/json-to-sql
     * 
     * 请求体示例:
     * {
     *   "json": { ... },
     *   "dbType": "mysql"
     * }
     */
    @PostMapping("/json-to-sql")
    public JsonToSqlResponse jsonToSql(@RequestBody JsonToSqlRequest request) {
        String sql = converterService.jsonToSql(request.getJson(), request.getDbType());
        JsonToSqlResponse response = new JsonToSqlResponse();
        response.setSql(sql);
        return response;
    }
    
    /**
     * SQL转JSON请求
     */
    @lombok.Data
    public static class SqlToJsonRequest {
        private String sql;
        private String dbType = "mysql";
    }
    
    /**
     * JSON转SQL请求
     */
    @lombok.Data
    public static class JsonToSqlRequest {
        private SqlJsonDto json;
        private String dbType = "mysql";
    }
    
    /**
     * JSON转SQL响应
     */
    @lombok.Data
    public static class JsonToSqlResponse {
        private String sql;
    }
}

