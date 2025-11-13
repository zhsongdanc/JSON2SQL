package com.zhsong.sql.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * SQL的JSON表示格式
 * 支持SELECT、INSERT、UPDATE、DELETE等语句
 */
@Data
public class SqlJsonDto {
    /**
     * SQL类型：SELECT, INSERT, UPDATE, DELETE
     */
    private String type;
    
    /**
     * SELECT语句
     */
    private SelectDto select;
    
    /**
     * INSERT语句
     */
    private InsertDto insert;
    
    /**
     * UPDATE语句
     */
    private UpdateDto update;
    
    /**
     * DELETE语句
     */
    private DeleteDto delete;
    
    @Data
    public static class SelectDto {
        /**
         * 查询字段列表
         */
        private List<SelectItemDto> selectList;
        
        /**
         * FROM子句
         */
        private TableSourceDto from;
        
        /**
         * WHERE条件
         */
        private ExprDto where;
        
        /**
         * GROUP BY子句
         */
        private GroupByDto groupBy;
        
        /**
         * ORDER BY子句
         */
        private OrderByDto orderBy;
        
        /**
         * LIMIT子句
         */
        private LimitDto limit;
        
        /**
         * HAVING子句
         */
        private ExprDto having;
        
        /**
         * UNION查询
         */
        private List<SelectDto> union;
        
        /**
         * UNION类型：UNION, UNION_ALL
         */
        private String unionType;
        
        /**
         * WITH子句（CTE）
         */
        private List<WithDto> with;
    }
    
    @Data
    public static class SelectItemDto {
        /**
         * 表达式
         */
        private ExprDto expr;
        
        /**
         * 别名
         */
        private String alias;
    }
    
    @Data
    public static class InsertDto {
        /**
         * 表名
         */
        private String table;
        
        /**
         * 列名列表
         */
        private List<String> columns;
        
        /**
         * VALUES值列表（多行）
         */
        private List<List<ExprDto>> values;
        
        /**
         * SELECT查询（INSERT INTO ... SELECT）
         */
        private SelectDto select;
    }
    
    @Data
    public static class UpdateDto {
        /**
         * 表名
         */
        private String table;
        
        /**
         * SET子句（字段和值的映射）
         */
        private Map<String, ExprDto> set;
        
        /**
         * WHERE条件
         */
        private ExprDto where;
    }
    
    @Data
    public static class DeleteDto {
        /**
         * 表名
         */
        private String table;
        
        /**
         * WHERE条件
         */
        private ExprDto where;
    }
    
    @Data
    public static class TableSourceDto {
        /**
         * 表类型：TABLE, JOIN, SUBQUERY, WITH
         */
        private String type;
        
        /**
         * 表名（type=TABLE时使用）
         */
        private String table;
        
        /**
         * 别名
         */
        private String alias;
        
        /**
         * JOIN左表
         */
        private TableSourceDto left;
        
        /**
         * JOIN右表
         */
        private TableSourceDto right;
        
        /**
         * JOIN类型：INNER, LEFT, RIGHT, FULL, CROSS
         */
        private String joinType;
        
        /**
         * JOIN条件
         */
        private ExprDto condition;
        
        /**
         * 子查询（type=SUBQUERY时使用）
         */
        private SelectDto subQuery;
    }
    
    @Data
    public static class ExprDto {
        /**
         * 表达式类型：IDENTIFIER, LITERAL, BINARY_OP, FUNCTION, CASE, IN, EXISTS, BETWEEN, LIKE, IS_NULL, PARAMETER
         */
        private String type;
        
        /**
         * 标识符名称（type=IDENTIFIER）
         */
        private String name;
        
        /**
         * 属性表达式：owner.name（type=PROPERTY）
         */
        private String owner;
        
        /**
         * 字面量值（type=LITERAL）
         */
        private Object value;
        
        /**
         * 字面量类型：STRING, NUMBER, BOOLEAN, NULL, DATE, TIME, TIMESTAMP
         */
        private String literalType;
        
        /**
         * 二元运算符：=, !=, <>, <, <=, >, >=, AND, OR, +, -, *, /, %, LIKE, NOT_LIKE, IN, NOT_IN
         */
        private String operator;
        
        /**
         * 左表达式（type=BINARY_OP）
         */
        private ExprDto left;
        
        /**
         * 右表达式（type=BINARY_OP）
         */
        private ExprDto right;
        
        /**
         * 函数名（type=FUNCTION）
         */
        private String functionName;
        
        /**
         * 函数参数列表（type=FUNCTION）
         */
        private List<ExprDto> arguments;
        
        /**
         * DISTINCT标志（type=FUNCTION）
         */
        private Boolean distinct;
        
        /**
         * CASE表达式（type=CASE）
         */
        private ExprDto caseExpr;
        
        /**
         * CASE WHEN列表
         */
        private List<CaseWhenDto> whenList;
        
        /**
         * CASE ELSE表达式
         */
        private ExprDto elseExpr;
        
        /**
         * IN表达式：expr IN (values)
         */
        private ExprDto inExpr;
        
        /**
         * IN值列表
         */
        private List<ExprDto> inValues;
        
        /**
         * NOT标志
         */
        private Boolean not;
        
        /**
         * BETWEEN表达式：expr BETWEEN start AND end
         */
        private ExprDto betweenExpr;
        
        /**
         * BETWEEN起始值
         */
        private ExprDto betweenStart;
        
        /**
         * BETWEEN结束值
         */
        private ExprDto betweenEnd;
        
        /**
         * EXISTS子查询
         */
        private SelectDto existsQuery;
        
        /**
         * 子查询（用于子查询表达式）
         */
        private SelectDto subQuery;
        
        /**
         * 参数名（type=PARAMETER，如?或:name）
         */
        private String parameterName;
    }
    
    @Data
    public static class CaseWhenDto {
        /**
         * WHEN条件
         */
        private ExprDto when;
        
        /**
         * THEN值
         */
        private ExprDto then;
    }
    
    @Data
    public static class GroupByDto {
        /**
         * GROUP BY表达式列表
         */
        private List<ExprDto> items;
        
        /**
         * HAVING条件
         */
        private ExprDto having;
    }
    
    @Data
    public static class OrderByDto {
        /**
         * ORDER BY项列表
         */
        private List<OrderItemDto> items;
    }
    
    @Data
    public static class OrderItemDto {
        /**
         * 排序表达式
         */
        private ExprDto expr;
        
        /**
         * 排序方向：ASC, DESC
         */
        private String direction;
    }
    
    @Data
    public static class LimitDto {
        /**
         * 偏移量
         */
        private Integer offset;
        
        /**
         * 行数
         */
        private Integer rowCount;
    }
    
    @Data
    public static class WithDto {
        /**
         * CTE名称
         */
        private String name;
        
        /**
         * CTE查询
         */
        private SelectDto query;
        
        /**
         * 列名列表（可选）
         */
        private List<String> columns;
    }
}

