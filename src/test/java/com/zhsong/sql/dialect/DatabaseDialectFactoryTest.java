package com.zhsong.sql.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据库方言工厂测试
 */
class DatabaseDialectFactoryTest {
    
    @Test
    void testGetMySqlDialect() {
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect("mysql");
        assertNotNull(dialect);
        assertEquals("mysql", dialect.getDbType().toLowerCase());
    }
    
    @Test
    void testGetOracleDialect() {
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect("oracle");
        assertNotNull(dialect);
        assertEquals("oracle", dialect.getDbType().toLowerCase());
    }
    
    @Test
    void testGetDialectCaseInsensitive() {
        DatabaseDialect dialect1 = DatabaseDialectFactory.getDialect("MYSQL");
        assertNotNull(dialect1);
        
        DatabaseDialect dialect2 = DatabaseDialectFactory.getDialect("MySQL");
        assertNotNull(dialect2);
        
        DatabaseDialect dialect3 = DatabaseDialectFactory.getDialect("ORACLE");
        assertNotNull(dialect3);
    }
    
    @Test
    void testGetDialectWithNull() {
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect(null);
        assertNotNull(dialect);
        assertEquals("mysql", dialect.getDbType().toLowerCase()); // 默认MySQL
    }
    
    @Test
    void testGetUnsupportedDialect() {
        assertThrows(IllegalArgumentException.class, () -> {
            DatabaseDialectFactory.getDialect("postgresql");
        });
    }
    
    @Test
    void testDialectFormat() {
        DatabaseDialect mysqlDialect = DatabaseDialectFactory.getDialect("mysql");
        assertNotNull(mysqlDialect);
        
        DatabaseDialect oracleDialect = DatabaseDialectFactory.getDialect("oracle");
        assertNotNull(oracleDialect);
    }
    
    @Test
    void testDialectParse() {
        DatabaseDialect mysqlDialect = DatabaseDialectFactory.getDialect("mysql");
        assertNotNull(mysqlDialect.parse("SELECT * FROM users"));
        
        DatabaseDialect oracleDialect = DatabaseDialectFactory.getDialect("oracle");
        assertNotNull(oracleDialect.parse("SELECT * FROM users"));
    }
}

