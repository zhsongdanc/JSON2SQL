package com.zhsong.sql.service;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.zhsong.sql.converter.AstToJsonConverter;
import com.zhsong.sql.converter.JsonToAstConverter;
import com.zhsong.sql.dialect.DatabaseDialect;
import com.zhsong.sql.dialect.DatabaseDialectFactory;
import com.zhsong.sql.dto.SqlJsonDto;
import org.springframework.stereotype.Service;

/**
 * SQL和JSON转换服务
 * 提供SQL转JSON和JSON转SQL的功能
 */
@Service
public class SqlJsonConverterService {
    
    /**
     * SQL转JSON
     * @param sql SQL语句
     * @param dbType 数据库类型（mysql/oracle）
     * @return JSON DTO
     */
    public SqlJsonDto sqlToJson(String sql, String dbType) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }
        
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect(dbType);
        SQLStatement statement = dialect.parse(sql);
        
        AstToJsonConverter converter = new AstToJsonConverter();
        statement.accept(converter);
        
        return converter.getResult();
    }
    
    /**
     * JSON转SQL
     * @param jsonDto JSON DTO
     * @param dbType 数据库类型（mysql/oracle）
     * @return SQL语句
     */
    public String jsonToSql(SqlJsonDto jsonDto, String dbType) {
        if (jsonDto == null) {
            throw new IllegalArgumentException("JSON DTO不能为空");
        }
        
        JsonToAstConverter converter = new JsonToAstConverter();
        SQLStatement statement = converter.convert(jsonDto);
        
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect(dbType);
        return dialect.format(statement);
    }
}

