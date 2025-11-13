package com.zhsong.sql.dialect;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

/**
 * 数据库方言接口
 * 使用策略模式支持不同的数据库类型
 */
public interface DatabaseDialect {
    
    /**
     * 获取数据库类型
     */
    String getDbType();
    
    /**
     * 将SQL语句格式化为字符串
     */
    String format(SQLStatement statement);
    
    /**
     * 解析SQL字符串为AST
     */
    SQLStatement parse(String sql);
    
    /**
     * MySQL方言实现
     */
    class MySqlDialect implements DatabaseDialect {
        @Override
        public String getDbType() {
            return JdbcConstants.MYSQL.name();
        }
        
        @Override
        public String format(SQLStatement statement) {
            return SQLUtils.toMySqlString(statement);
        }
        
        @Override
        public SQLStatement parse(String sql) {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
            return parser.parseStatement();
        }
    }
    
    /**
     * Oracle方言实现
     */
    class OracleDialect implements DatabaseDialect {
        @Override
        public String getDbType() {
            return JdbcConstants.ORACLE.name();
        }
        
        @Override
        public String format(SQLStatement statement) {
            return SQLUtils.toOracleString(statement);
        }
        
        @Override
        public SQLStatement parse(String sql) {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.ORACLE);
            return parser.parseStatement();
        }
    }
}

