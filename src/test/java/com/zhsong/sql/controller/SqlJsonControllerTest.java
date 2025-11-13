package com.zhsong.sql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhsong.sql.dto.SqlJsonDto;
import com.zhsong.sql.service.SqlJsonConverterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SQL和JSON转换控制器测试
 */
@WebMvcTest(SqlJsonController.class)
class SqlJsonControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SqlJsonConverterService converterService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // 设置默认的mock行为
    }
    
    @Test
    void testSqlToJson() throws Exception {
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlJsonDto expectedDto = new SqlJsonDto();
        expectedDto.setType("SELECT");
        
        when(converterService.sqlToJson(sql, "mysql")).thenReturn(expectedDto);
        
        SqlJsonController.SqlToJsonRequest request = new SqlJsonController.SqlToJsonRequest();
        request.setSql(sql);
        request.setDbType("mysql");
        
        mockMvc.perform(post("/api/sql-json/sql-to-json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SELECT"));
    }
    
    @Test
    void testSqlToJsonWithDefaultDbType() throws Exception {
        String sql = "SELECT * FROM users";
        SqlJsonDto expectedDto = new SqlJsonDto();
        expectedDto.setType("SELECT");
        
        when(converterService.sqlToJson(sql, "mysql")).thenReturn(expectedDto);
        
        SqlJsonController.SqlToJsonRequest request = new SqlJsonController.SqlToJsonRequest();
        request.setSql(sql);
        // 不设置dbType，应该使用默认值mysql
        
        mockMvc.perform(post("/api/sql-json/sql-to-json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    @Test
    void testJsonToSql() throws Exception {
        SqlJsonDto jsonDto = new SqlJsonDto();
        jsonDto.setType("SELECT");
        
        String expectedSql = "SELECT * FROM users";
        
        when(converterService.jsonToSql(any(SqlJsonDto.class), anyString())).thenReturn(expectedSql);
        
        SqlJsonController.JsonToSqlRequest request = new SqlJsonController.JsonToSqlRequest();
        request.setJson(jsonDto);
        request.setDbType("mysql");
        
        mockMvc.perform(post("/api/sql-json/json-to-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sql").value(expectedSql));
    }
    
    @Test
    void testJsonToSqlWithOracle() throws Exception {
        SqlJsonDto jsonDto = new SqlJsonDto();
        jsonDto.setType("SELECT");
        
        String expectedSql = "SELECT * FROM users";
        
        when(converterService.jsonToSql(any(SqlJsonDto.class), anyString())).thenReturn(expectedSql);
        
        SqlJsonController.JsonToSqlRequest request = new SqlJsonController.JsonToSqlRequest();
        request.setJson(jsonDto);
        request.setDbType("oracle");
        
        mockMvc.perform(post("/api/sql-json/json-to-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sql").exists());
    }
    
    @Test
    void testSqlToJsonWithInvalidRequest() throws Exception {
        // 测试空SQL
        SqlJsonController.SqlToJsonRequest request = new SqlJsonController.SqlToJsonRequest();
        request.setSql("");
        request.setDbType("mysql");
        
        when(converterService.sqlToJson("", "mysql"))
                .thenThrow(new IllegalArgumentException("SQL语句不能为空"));
        
        mockMvc.perform(post("/api/sql-json/sql-to-json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testJsonToSqlWithInvalidRequest() throws Exception {
        SqlJsonController.JsonToSqlRequest request = new SqlJsonController.JsonToSqlRequest();
        request.setJson(null);
        request.setDbType("mysql");
        
        when(converterService.jsonToSql(null, "mysql"))
                .thenThrow(new IllegalArgumentException("JSON DTO不能为空"));
        
        mockMvc.perform(post("/api/sql-json/json-to-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

