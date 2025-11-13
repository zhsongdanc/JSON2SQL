package com.zhsong.sql.converter;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.zhsong.sql.dto.SqlJsonDto;
import java.util.Map;

/**
 * JSON转AST转换器
 * 使用构建器模式构建AST节点
 */
public class JsonToAstConverter {
    
    /**
     * 将JSON DTO转换为AST Statement
     */
    public SQLStatement convert(SqlJsonDto dto) {
        if (dto == null || dto.getType() == null) {
            throw new IllegalArgumentException("SQL类型不能为空");
        }
        
        String type = dto.getType().toUpperCase();
        switch (type) {
            case "SELECT":
                return convertSelect(dto.getSelect());
            case "INSERT":
                return convertInsert(dto.getInsert());
            case "UPDATE":
                return convertUpdate(dto.getUpdate());
            case "DELETE":
                return convertDelete(dto.getDelete());
            default:
                throw new IllegalArgumentException("不支持的SQL类型: " + type);
        }
    }
    
    private SQLSelectStatement convertSelect(SqlJsonDto.SelectDto select) {
        if (select == null) {
            throw new IllegalArgumentException("SELECT语句不能为空");
        }
        
        SQLSelectStatement statement = new SQLSelectStatement();
        SQLSelect sqlSelect = new SQLSelect();
        SQLSelectQueryBlock queryBlock = new SQLSelectQueryBlock();
        
        // SELECT列表
        if (select.getSelectList() != null && !select.getSelectList().isEmpty()) {
            for (SqlJsonDto.SelectItemDto item : select.getSelectList()) {
                if (item != null && item.getExpr() != null) {
                    SQLSelectItem selectItem = new SQLSelectItem();
                    SQLExpr expr = convertExpr(item.getExpr());
                    if (expr != null) {
                        selectItem.setExpr(expr);
                        if (item.getAlias() != null) {
                            selectItem.setAlias(item.getAlias());
                        }
                        queryBlock.addSelectItem(selectItem);
                    }
                }
            }
        }
        
        // FROM子句
        if (select.getFrom() != null) {
            SQLTableSource tableSource = convertTableSource(select.getFrom());
            if (tableSource != null) {
                queryBlock.setFrom(tableSource);
            }
        }
        
        // WHERE子句
        if (select.getWhere() != null) {
            queryBlock.setWhere(convertExpr(select.getWhere()));
        }
        
        // GROUP BY子句
        if (select.getGroupBy() != null) {
            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            if (select.getGroupBy().getItems() != null) {
                for (SqlJsonDto.ExprDto expr : select.getGroupBy().getItems()) {
                    groupBy.addItem(convertExpr(expr));
                }
            }
            if (select.getGroupBy().getHaving() != null) {
                groupBy.setHaving(convertExpr(select.getGroupBy().getHaving()));
            }
            queryBlock.setGroupBy(groupBy);
        }
        
        // HAVING子句（如果不在GROUP BY中）
        if (select.getHaving() != null && select.getGroupBy() == null) {
            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            groupBy.setHaving(convertExpr(select.getHaving()));
            queryBlock.setGroupBy(groupBy);
        }
        
        // ORDER BY子句
        if (select.getOrderBy() != null && select.getOrderBy().getItems() != null) {
            SQLOrderBy orderBy = new SQLOrderBy();
            for (SqlJsonDto.OrderItemDto item : select.getOrderBy().getItems()) {
                SQLSelectOrderByItem orderItem = new SQLSelectOrderByItem();
                orderItem.setExpr(convertExpr(item.getExpr()));
                if ("DESC".equalsIgnoreCase(item.getDirection())) {
                    orderItem.setType(SQLOrderingSpecification.DESC);
                } else {
                    orderItem.setType(SQLOrderingSpecification.ASC);
                }
                orderBy.addItem(orderItem);
            }
            queryBlock.setOrderBy(orderBy);
        }
        
        // LIMIT子句
        if (select.getLimit() != null) {
            SQLLimit limit = new SQLLimit();
            if (select.getLimit().getOffset() != null) {
                limit.setOffset(new SQLIntegerExpr(select.getLimit().getOffset()));
            }
            if (select.getLimit().getRowCount() != null) {
                limit.setRowCount(new SQLIntegerExpr(select.getLimit().getRowCount()));
            }
            queryBlock.setLimit(limit);
        }
        
        sqlSelect.setQuery(queryBlock);
        statement.setSelect(sqlSelect);
        return statement;
    }
    
    private SQLInsertStatement convertInsert(SqlJsonDto.InsertDto insert) {
        if (insert == null) {
            throw new IllegalArgumentException("INSERT语句不能为空");
        }
        
        SQLInsertStatement statement = new SQLInsertStatement();
        
        // 表名
        if (insert.getTable() != null) {
            SQLExprTableSource tableSource = new SQLExprTableSource();
            tableSource.setExpr(new SQLIdentifierExpr(insert.getTable()));
            statement.setTableSource(tableSource);
        }
        
        // 列名
        if (insert.getColumns() != null) {
            for (String column : insert.getColumns()) {
                statement.addColumn(new SQLIdentifierExpr(column));
            }
        }
        
        // VALUES子句
        if (insert.getValues() != null && !insert.getValues().isEmpty()) {
            for (java.util.List<SqlJsonDto.ExprDto> row : insert.getValues()) {
                SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
                for (SqlJsonDto.ExprDto expr : row) {
                    values.addValue(convertExpr(expr));
                }
                statement.addValueCause(values);
            }
        }
        
        // SELECT子句
        if (insert.getSelect() != null) {
            SQLSelectStatement selectStmt = convertSelect(insert.getSelect());
            statement.setQuery(selectStmt.getSelect());
        }
        
        return statement;
    }
    
    private SQLUpdateStatement convertUpdate(SqlJsonDto.UpdateDto update) {
        if (update == null) {
            throw new IllegalArgumentException("UPDATE语句不能为空");
        }
        
        SQLUpdateStatement statement = new SQLUpdateStatement();
        
        // 表名
        if (update.getTable() != null) {
            SQLExprTableSource tableSource = new SQLExprTableSource();
            tableSource.setExpr(new SQLIdentifierExpr(update.getTable()));
            statement.setTableSource(tableSource);
        }
        
        // SET子句
        if (update.getSet() != null) {
            for (Map.Entry<String, SqlJsonDto.ExprDto> entry : update.getSet().entrySet()) {
                SQLUpdateSetItem item = new SQLUpdateSetItem();
                item.setColumn(new SQLIdentifierExpr(entry.getKey()));
                item.setValue(convertExpr(entry.getValue()));
                statement.addItem(item);
            }
        }
        
        // WHERE子句
        if (update.getWhere() != null) {
            statement.setWhere(convertExpr(update.getWhere()));
        }
        
        return statement;
    }
    
    private SQLDeleteStatement convertDelete(SqlJsonDto.DeleteDto delete) {
        if (delete == null) {
            throw new IllegalArgumentException("DELETE语句不能为空");
        }
        
        SQLDeleteStatement statement = new SQLDeleteStatement();
        
        // 表名
        if (delete.getTable() != null) {
            SQLExprTableSource tableSource = new SQLExprTableSource();
            tableSource.setExpr(new SQLIdentifierExpr(delete.getTable()));
            statement.setTableSource(tableSource);
        }
        
        // WHERE子句
        if (delete.getWhere() != null) {
            statement.setWhere(convertExpr(delete.getWhere()));
        }
        
        return statement;
    }
    
    private SQLTableSource convertTableSource(SqlJsonDto.TableSourceDto tableSource) {
        if (tableSource == null) {
            return null;
        }
        
        String type = tableSource.getType();
        if (type == null || "TABLE".equals(type)) {
            SQLExprTableSource exprTable = new SQLExprTableSource();
            if (tableSource.getTable() != null) {
                exprTable.setExpr(new SQLIdentifierExpr(tableSource.getTable()));
            }
            if (tableSource.getAlias() != null) {
                exprTable.setAlias(tableSource.getAlias());
            }
            return exprTable;
        } else if ("JOIN".equals(type)) {
            SQLJoinTableSource joinTable = new SQLJoinTableSource();
            joinTable.setLeft(convertTableSource(tableSource.getLeft()));
            joinTable.setRight(convertTableSource(tableSource.getRight()));
            joinTable.setJoinType(convertJoinType(tableSource.getJoinType()));
            if (tableSource.getCondition() != null) {
                joinTable.setCondition(convertExpr(tableSource.getCondition()));
            }
            if (tableSource.getAlias() != null) {
                joinTable.setAlias(tableSource.getAlias());
            }
            return joinTable;
        } else if ("SUBQUERY".equals(type)) {
            SQLSubqueryTableSource subqueryTable = new SQLSubqueryTableSource();
            if (tableSource.getSubQuery() != null) {
                SQLSelectStatement selectStmt = convertSelect(tableSource.getSubQuery());
                subqueryTable.setSelect(selectStmt.getSelect());
            }
            if (tableSource.getAlias() != null) {
                subqueryTable.setAlias(tableSource.getAlias());
            }
            return subqueryTable;
        }
        
        return null;
    }
    
    private SQLJoinTableSource.JoinType convertJoinType(String joinType) {
        if (joinType == null) {
            return SQLJoinTableSource.JoinType.INNER_JOIN;
        }
        switch (joinType.toUpperCase()) {
            case "INNER":
                return SQLJoinTableSource.JoinType.INNER_JOIN;
            case "LEFT":
                return SQLJoinTableSource.JoinType.LEFT_OUTER_JOIN;
            case "RIGHT":
                return SQLJoinTableSource.JoinType.RIGHT_OUTER_JOIN;
            case "FULL":
                return SQLJoinTableSource.JoinType.FULL_OUTER_JOIN;
            case "CROSS":
                return SQLJoinTableSource.JoinType.CROSS_JOIN;
            default:
                return SQLJoinTableSource.JoinType.INNER_JOIN;
        }
    }
    
    private SQLExpr convertExpr(SqlJsonDto.ExprDto expr) {
        if (expr == null) {
            return null;
        }
        if (expr.getType() == null) {
            return null;
        }
        
        String type = expr.getType().toUpperCase();
        SQLExpr result = null;
        
        switch (type) {
            case "IDENTIFIER":
                result = new SQLIdentifierExpr(expr.getName());
                break;
            case "PROPERTY":
                result = new SQLPropertyExpr(expr.getOwner(), expr.getName());
                break;
            case "LITERAL":
                result = convertLiteral(expr);
                break;
            case "BINARY_OP":
                result = convertBinaryOp(expr);
                break;
            case "FUNCTION":
                result = convertFunction(expr);
                break;
            case "CASE":
                result = convertCase(expr);
                break;
            case "IN":
                result = convertIn(expr);
                break;
            case "BETWEEN":
                result = convertBetween(expr);
                break;
            case "EXISTS":
                result = convertExists(expr);
                break;
            case "SUBQUERY":
                result = convertSubquery(expr);
                break;
            case "PARAMETER":
                result = new SQLVariantRefExpr(expr.getParameterName());
                break;
            default:
                throw new IllegalArgumentException("不支持的表达式类型: " + type);
        }
        
        // 处理NOT操作
        if (result != null && Boolean.TRUE.equals(expr.getNot())) {
            result = new SQLNotExpr(result);
        }
        
        return result;
    }
    
    private SQLExpr convertLiteral(SqlJsonDto.ExprDto expr) {
        String literalType = expr.getLiteralType();
        Object value = expr.getValue();
        
        if (literalType == null) {
            return new SQLNullExpr();
        }
        
        switch (literalType.toUpperCase()) {
            case "STRING":
                return new SQLCharExpr(value != null ? value.toString() : null);
            case "NUMBER":
                if (value instanceof Number) {
                    if (value instanceof Integer || value instanceof Long) {
                        return new SQLIntegerExpr(((Number) value).longValue());
                    } else {
                        return new SQLNumberExpr(((Number) value).doubleValue());
                    }
                }
                return new SQLNumberExpr(value != null ? Double.parseDouble(value.toString()) : 0);
            case "BOOLEAN":
                return new SQLBooleanExpr(Boolean.TRUE.equals(value));
            case "NULL":
                return new SQLNullExpr();
            case "DATE":
                return new SQLDateExpr(value != null ? value.toString() : null);
            case "TIME":
                return new SQLTimeExpr(value != null ? value.toString() : null);
            case "TIMESTAMP":
                return new SQLTimestampExpr(value != null ? value.toString() : null);
            default:
                return new SQLCharExpr(value != null ? value.toString() : null);
        }
    }
    
    private SQLExpr convertBinaryOp(SqlJsonDto.ExprDto expr) {
        SQLBinaryOpExpr binaryOp = new SQLBinaryOpExpr();
        binaryOp.setLeft(convertExpr(expr.getLeft()));
        binaryOp.setRight(convertExpr(expr.getRight()));
        binaryOp.setOperator(convertOperator(expr.getOperator()));
        return binaryOp;
    }
    
    private SQLBinaryOperator convertOperator(String operator) {
        if (operator == null) {
            return null;
        }
        switch (operator) {
            case "=":
                return SQLBinaryOperator.Equality;
            case "!=":
            case "<>":
                return SQLBinaryOperator.NotEqual;
            case "<":
                return SQLBinaryOperator.LessThan;
            case "<=":
                return SQLBinaryOperator.LessThanOrEqual;
            case ">":
                return SQLBinaryOperator.GreaterThan;
            case ">=":
                return SQLBinaryOperator.GreaterThanOrEqual;
            case "AND":
                return SQLBinaryOperator.BooleanAnd;
            case "OR":
                return SQLBinaryOperator.BooleanOr;
            case "+":
                return SQLBinaryOperator.Add;
            case "-":
                return SQLBinaryOperator.Subtract;
            case "*":
                return SQLBinaryOperator.Multiply;
            case "/":
                return SQLBinaryOperator.Divide;
            case "%":
                return SQLBinaryOperator.Modulus;
            case "LIKE":
                return SQLBinaryOperator.Like;
            case "NOT_LIKE":
                return SQLBinaryOperator.NotLike;
            default:
                return SQLBinaryOperator.valueOf(operator);
        }
    }
    
    private SQLExpr convertFunction(SqlJsonDto.ExprDto expr) {
        SQLMethodInvokeExpr function = new SQLMethodInvokeExpr();
        function.setMethodName(expr.getFunctionName());
        if (expr.getArguments() != null) {
            for (SqlJsonDto.ExprDto arg : expr.getArguments()) {
                function.addArgument(convertExpr(arg));
            }
        }
        // DISTINCT选项在Druid中通过其他方式处理
        // 这里暂时忽略，因为Druid API可能不同
        return function;
    }
    
    private SQLExpr convertCase(SqlJsonDto.ExprDto expr) {
        SQLCaseExpr caseExpr = new SQLCaseExpr();
        if (expr.getCaseExpr() != null) {
            caseExpr.setValueExpr(convertExpr(expr.getCaseExpr()));
        }
        if (expr.getWhenList() != null) {
            for (SqlJsonDto.CaseWhenDto when : expr.getWhenList()) {
                SQLCaseExpr.Item item = new SQLCaseExpr.Item();
                item.setConditionExpr(convertExpr(when.getWhen()));
                item.setValueExpr(convertExpr(when.getThen()));
                caseExpr.addItem(item);
            }
        }
        if (expr.getElseExpr() != null) {
            caseExpr.setElseExpr(convertExpr(expr.getElseExpr()));
        }
        return caseExpr;
    }
    
    private SQLExpr convertIn(SqlJsonDto.ExprDto expr) {
        SQLInListExpr inExpr = new SQLInListExpr();
        inExpr.setExpr(convertExpr(expr.getInExpr()));
        if (expr.getInValues() != null) {
            for (SqlJsonDto.ExprDto value : expr.getInValues()) {
                inExpr.addTarget(convertExpr(value));
            }
        }
        inExpr.setNot(Boolean.TRUE.equals(expr.getNot()));
        return inExpr;
    }
    
    private SQLExpr convertBetween(SqlJsonDto.ExprDto expr) {
        SQLBetweenExpr betweenExpr = new SQLBetweenExpr();
        betweenExpr.setTestExpr(convertExpr(expr.getBetweenExpr()));
        betweenExpr.setBeginExpr(convertExpr(expr.getBetweenStart()));
        betweenExpr.setEndExpr(convertExpr(expr.getBetweenEnd()));
        betweenExpr.setNot(Boolean.TRUE.equals(expr.getNot()));
        return betweenExpr;
    }
    
    private SQLExpr convertExists(SqlJsonDto.ExprDto expr) {
        SQLExistsExpr existsExpr = new SQLExistsExpr();
        if (expr.getExistsQuery() != null) {
            SQLSelectStatement selectStmt = convertSelect(expr.getExistsQuery());
            existsExpr.setSubQuery(selectStmt.getSelect());
        }
        existsExpr.setNot(Boolean.TRUE.equals(expr.getNot()));
        return existsExpr;
    }
    
    private SQLExpr convertSubquery(SqlJsonDto.ExprDto expr) {
        if (expr.getSubQuery() != null) {
            SQLSelectStatement selectStmt = convertSelect(expr.getSubQuery());
            return new SQLQueryExpr(selectStmt.getSelect());
        }
        return null;
    }
}

