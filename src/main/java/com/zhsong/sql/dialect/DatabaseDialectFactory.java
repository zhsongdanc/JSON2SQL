package com.zhsong.sql.dialect;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库方言工厂
 * 使用工厂模式创建方言实例
 */
public class DatabaseDialectFactory {
    
    private static final Map<String, DatabaseDialect> DIALECT_MAP = new HashMap<>();
    
    static {
        DIALECT_MAP.put("mysql", new DatabaseDialect.MySqlDialect());
        DIALECT_MAP.put("oracle", new DatabaseDialect.OracleDialect());
    }
    
    /**
     * 获取数据库方言
     */
    public static DatabaseDialect getDialect(String dbType) {
        if (dbType == null) {
            dbType = "mysql";
        }
        String key = dbType.toLowerCase();
        DatabaseDialect dialect = DIALECT_MAP.get(key);
        if (dialect == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
        return dialect;
    }
}

