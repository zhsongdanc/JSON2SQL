package com.zhsong.sql.converter;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.zhsong.sql.dto.SqlJsonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AST转JSON转换器测试
 */
class AstToJsonConverterTest {
    
    private AstToJsonConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new AstToJsonConverter();
    }
    
    @Test
    void testSimpleSelect() {
        String sql = "SELECT id, name FROM users";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("SELECT", result.getType());
        assertNotNull(result.getSelect());
        assertNotNull(result.getSelect().getSelectList());
        assertEquals(2, result.getSelect().getSelectList().size());
        assertEquals("users", result.getSelect().getFrom().getTable());
    }
    
    @Test
    void testSelectWithWhere() {
        String sql = "SELECT * FROM users WHERE id = 1";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
        assertEquals("BINARY_OP", result.getSelect().getWhere().getType());
        assertEquals("=", result.getSelect().getWhere().getOperator());
    }
    
    @Test
    void testSelectWithJoin() {
        String sql = "SELECT u.id, o.order_id FROM users u INNER JOIN orders o ON u.id = o.user_id";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getFrom());
        assertEquals("JOIN", result.getSelect().getFrom().getType());
        assertEquals("INNER", result.getSelect().getFrom().getJoinType());
    }
    
    @Test
    void testSelectWithGroupBy() {
        String sql = "SELECT user_id, COUNT(*) as cnt FROM orders GROUP BY user_id";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getGroupBy());
        assertNotNull(result.getSelect().getGroupBy().getItems());
        assertEquals(1, result.getSelect().getGroupBy().getItems().size());
    }
    
    @Test
    void testSelectWithOrderBy() {
        String sql = "SELECT * FROM users ORDER BY id DESC, name ASC";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getOrderBy());
        assertNotNull(result.getSelect().getOrderBy().getItems());
        assertTrue(result.getSelect().getOrderBy().getItems().size() >= 1);
    }
    
    @Test
    void testSelectWithLimit() {
        String sql = "SELECT * FROM users LIMIT 10";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getLimit());
        assertEquals(10, result.getSelect().getLimit().getRowCount());
    }
    
    @Test
    void testSelectWithLimitOffset() {
        String sql = "SELECT * FROM users LIMIT 10 OFFSET 20";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getLimit());
        assertEquals(10, result.getSelect().getLimit().getRowCount());
        assertEquals(20, result.getSelect().getLimit().getOffset());
    }
    
    @Test
    void testSelectWithSubquery() {
        String sql = "SELECT * FROM (SELECT id FROM users) AS sub";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getFrom());
        assertEquals("SUBQUERY", result.getSelect().getFrom().getType());
        assertNotNull(result.getSelect().getFrom().getSubQuery());
    }
    
    @Test
    void testSelectWithInExpression() {
        String sql = "SELECT * FROM users WHERE id IN (1, 2, 3)";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
        assertEquals("IN", result.getSelect().getWhere().getType());
        assertNotNull(result.getSelect().getWhere().getInValues());
    }
    
    @Test
    void testSelectWithBetweenExpression() {
        String sql = "SELECT * FROM users WHERE age BETWEEN 18 AND 65";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
        assertEquals("BETWEEN", result.getSelect().getWhere().getType());
    }
    
    @Test
    void testSelectWithLikeExpression() {
        String sql = "SELECT * FROM users WHERE name LIKE '%john%'";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
        assertEquals("BINARY_OP", result.getSelect().getWhere().getType());
        assertEquals("LIKE", result.getSelect().getWhere().getOperator());
    }
    
    @Test
    void testSelectWithFunction() {
        String sql = "SELECT COUNT(*), MAX(age), MIN(age) FROM users";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getSelectList());
        assertTrue(result.getSelect().getSelectList().size() >= 1);
    }
    
    @Test
    void testSelectWithCaseExpression() {
        String sql = "SELECT CASE WHEN age < 18 THEN 'minor' WHEN age < 65 THEN 'adult' ELSE 'senior' END FROM users";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getSelectList());
        assertEquals("CASE", result.getSelect().getSelectList().get(0).getExpr().getType());
    }
    
    @Test
    void testInsertWithValues() {
        String sql = "INSERT INTO users (id, name, age) VALUES (1, 'John', 25)";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("INSERT", result.getType());
        assertNotNull(result.getInsert());
        assertEquals("users", result.getInsert().getTable());
        assertNotNull(result.getInsert().getColumns());
        assertEquals(3, result.getInsert().getColumns().size());
        assertNotNull(result.getInsert().getValues());
    }
    
    @Test
    void testInsertWithSelect() {
        String sql = "INSERT INTO users (id, name) SELECT id, name FROM temp_users";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("INSERT", result.getType());
        assertNotNull(result.getInsert().getSelect());
    }
    
    @Test
    void testUpdate() {
        String sql = "UPDATE users SET name = 'Jane', age = 26 WHERE id = 1";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("UPDATE", result.getType());
        assertNotNull(result.getUpdate());
        assertEquals("users", result.getUpdate().getTable());
        assertNotNull(result.getUpdate().getSet());
        assertTrue(result.getUpdate().getSet().size() >= 2);
        assertNotNull(result.getUpdate().getWhere());
    }
    
    @Test
    void testDelete() {
        String sql = "DELETE FROM users WHERE id = 1";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("DELETE", result.getType());
        assertNotNull(result.getDelete());
        assertEquals("users", result.getDelete().getTable());
        assertNotNull(result.getDelete().getWhere());
    }
    
    @Test
    void testSelectWithLeftJoin() {
        String sql = "SELECT * FROM users u LEFT JOIN orders o ON u.id = o.user_id";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("LEFT", result.getSelect().getFrom().getJoinType());
    }
    
    @Test
    void testSelectWithRightJoin() {
        String sql = "SELECT * FROM users u RIGHT JOIN orders o ON u.id = o.user_id";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertEquals("RIGHT", result.getSelect().getFrom().getJoinType());
    }
    
    @Test
    void testSelectWithComplexWhere() {
        String sql = "SELECT * FROM users WHERE id = 1 AND age > 18 OR name LIKE '%john%'";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
    }
    
    @Test
    void testSelectWithHaving() {
        String sql = "SELECT user_id, COUNT(*) FROM orders GROUP BY user_id HAVING COUNT(*) > 10";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getGroupBy());
        assertNotNull(result.getSelect().getGroupBy().getHaving());
    }
    
    @Test
    void testSelectWithParameter() {
        String sql = "SELECT * FROM users WHERE id = ?";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getWhere());
        assertEquals("PARAMETER", result.getSelect().getWhere().getRight().getType());
    }
    
    @Test
    void testSelectWithPropertyExpression() {
        String sql = "SELECT u.id, u.name FROM users u";
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement statement = parser.parseStatement();
        
        statement.accept(converter);
        SqlJsonDto result = converter.getResult();
        
        assertNotNull(result);
        assertNotNull(result.getSelect().getSelectList());
        // 检查是否有PROPERTY类型的表达式
    }
}

