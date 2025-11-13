package com.zhsong.sql.service;

import com.zhsong.sql.dto.SqlJsonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQL和JSON转换服务测试
 */
class SqlJsonConverterServiceTest {
    
    private SqlJsonConverterService service;
    
    @BeforeEach
    void setUp() {
        service = new SqlJsonConverterService();
    }
    
    @Test
    void testSqlToJsonSimpleSelect() {
        String sql = "SELECT id, name FROM users";
        SqlJsonDto result = service.sqlToJson(sql, "mysql");
        
        assertNotNull(result);
        assertEquals("SELECT", result.getType());
        assertNotNull(result.getSelect());
        assertNotNull(result.getSelect().getSelectList());
        assertEquals(2, result.getSelect().getSelectList().size());
    }
    
    @Test
    void testSqlToJsonWithWhere() {
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlJsonDto result = service.sqlToJson(sql, "mysql");
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
    }
    
    @Test
    void testSqlToJsonInsert() {
        String sql = "INSERT INTO users (id, name) VALUES (1, 'John')";
        SqlJsonDto result = service.sqlToJson(sql, "mysql");
        
        assertNotNull(result);
        assertEquals("INSERT", result.getType());
        assertNotNull(result.getInsert());
        assertEquals("users", result.getInsert().getTable());
    }
    
    @Test
    void testSqlToJsonUpdate() {
        String sql = "UPDATE users SET name = 'Jane' WHERE id = 1";
        SqlJsonDto result = service.sqlToJson(sql, "mysql");
        
        assertNotNull(result);
        assertEquals("UPDATE", result.getType());
        assertNotNull(result.getUpdate());
    }
    
    @Test
    void testSqlToJsonDelete() {
        String sql = "DELETE FROM users WHERE id = 1";
        SqlJsonDto result = service.sqlToJson(sql, "mysql");
        
        assertNotNull(result);
        assertEquals("DELETE", result.getType());
        assertNotNull(result.getDelete());
    }
    
    @Test
    void testSqlToJsonWithOracle() {
        String sql = "SELECT * FROM users WHERE id = 1";
        SqlJsonDto result = service.sqlToJson(sql, "oracle");
        
        assertNotNull(result);
        assertEquals("SELECT", result.getType());
    }
    
    @Test
    void testSqlToJsonWithNullSql() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.sqlToJson(null, "mysql");
        });
    }
    
    @Test
    void testSqlToJsonWithEmptySql() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.sqlToJson("", "mysql");
        });
    }
    
    @Test
    void testSqlToJsonWithInvalidSql() {
        // 应该抛出异常或返回错误
        assertThrows(Exception.class, () -> {
            service.sqlToJson("INVALID SQL", "mysql");
        });
    }
    
    @Test
    void testJsonToSqlSimpleSelect() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        java.util.List<SqlJsonDto.SelectItemDto> selectList = new java.util.ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        dto.setSelect(select);
        
        String sql = service.jsonToSql(dto, "mysql");
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
        assertTrue(sql.contains("users"));
    }
    
    @Test
    void testJsonToSqlWithWhere() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        java.util.List<SqlJsonDto.SelectItemDto> selectList = new java.util.ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("BINARY_OP");
        where.setOperator("=");
        SqlJsonDto.ExprDto left = new SqlJsonDto.ExprDto();
        left.setType("IDENTIFIER");
        left.setName("id");
        where.setLeft(left);
        SqlJsonDto.ExprDto right = new SqlJsonDto.ExprDto();
        right.setType("LITERAL");
        right.setLiteralType("NUMBER");
        right.setValue(1);
        where.setRight(right);
        select.setWhere(where);
        
        dto.setSelect(select);
        
        String sql = service.jsonToSql(dto, "mysql");
        assertNotNull(sql);
        assertTrue(sql.contains("WHERE"));
    }
    
    @Test
    void testJsonToSqlInsert() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("INSERT");
        
        SqlJsonDto.InsertDto insert = new SqlJsonDto.InsertDto();
        insert.setTable("users");
        java.util.List<String> columns = new java.util.ArrayList<>();
        columns.add("id");
        columns.add("name");
        insert.setColumns(columns);
        
        java.util.List<java.util.List<SqlJsonDto.ExprDto>> values = new java.util.ArrayList<>();
        java.util.List<SqlJsonDto.ExprDto> row = new java.util.ArrayList<>();
        SqlJsonDto.ExprDto id = new SqlJsonDto.ExprDto();
        id.setType("LITERAL");
        id.setLiteralType("NUMBER");
        id.setValue(1);
        row.add(id);
        SqlJsonDto.ExprDto name = new SqlJsonDto.ExprDto();
        name.setType("LITERAL");
        name.setLiteralType("STRING");
        name.setValue("John");
        row.add(name);
        values.add(row);
        insert.setValues(values);
        
        dto.setInsert(insert);
        
        String sql = service.jsonToSql(dto, "mysql");
        assertNotNull(sql);
        assertTrue(sql.contains("INSERT"));
        assertTrue(sql.contains("users"));
    }
    
    @Test
    void testJsonToSqlUpdate() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("UPDATE");
        
        SqlJsonDto.UpdateDto update = new SqlJsonDto.UpdateDto();
        update.setTable("users");
        java.util.Map<String, SqlJsonDto.ExprDto> set = new java.util.HashMap<>();
        SqlJsonDto.ExprDto name = new SqlJsonDto.ExprDto();
        name.setType("LITERAL");
        name.setLiteralType("STRING");
        name.setValue("Jane");
        set.put("name", name);
        update.setSet(set);
        
        dto.setUpdate(update);
        
        String sql = service.jsonToSql(dto, "mysql");
        assertNotNull(sql);
        assertTrue(sql.contains("UPDATE"));
    }
    
    @Test
    void testJsonToSqlDelete() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("DELETE");
        
        SqlJsonDto.DeleteDto delete = new SqlJsonDto.DeleteDto();
        delete.setTable("users");
        
        dto.setDelete(delete);
        
        String sql = service.jsonToSql(dto, "mysql");
        assertNotNull(sql);
        assertTrue(sql.contains("DELETE"));
    }
    
    @Test
    void testJsonToSqlWithOracle() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        java.util.List<SqlJsonDto.SelectItemDto> selectList = new java.util.ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        dto.setSelect(select);
        
        String sql = service.jsonToSql(dto, "oracle");
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT"));
    }
    
    @Test
    void testJsonToSqlWithNullDto() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.jsonToSql(null, "mysql");
        });
    }
    
    @Test
    void testRoundTripConversion() {
        // 测试SQL -> JSON -> SQL的往返转换
        String originalSql = "SELECT id, name FROM users WHERE id = 1";
        
        SqlJsonDto json = service.sqlToJson(originalSql, "mysql");
        assertNotNull(json);
        
        String convertedSql = service.jsonToSql(json, "mysql");
        assertNotNull(convertedSql);
        assertTrue(convertedSql.contains("SELECT"));
        assertTrue(convertedSql.contains("users"));
    }
}

