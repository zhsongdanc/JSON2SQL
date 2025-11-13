package com.zhsong.sql.converter;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.zhsong.sql.dto.SqlJsonDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AST转JSON转换器
 * 使用访问者模式遍历AST节点
 */
public class AstToJsonConverter extends SQLASTVisitorAdapter {
    
    @Getter
    private SqlJsonDto result;
    
    @Override
    public boolean visit(SQLSelectStatement x) {
        result = new SqlJsonDto();
        result.setType("SELECT");
        result.setSelect(new SqlJsonDto.SelectDto());
        x.getSelect().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(SQLInsertStatement x) {
        result = new SqlJsonDto();
        result.setType("INSERT");
        SqlJsonDto.InsertDto insert = new SqlJsonDto.InsertDto();
        result.setInsert(insert);
        
        if (x.getTableSource() != null) {
            insert.setTable(getTableName(x.getTableSource()));
        }
        
        if (x.getColumns() != null && !x.getColumns().isEmpty()) {
            List<String> columns = new ArrayList<>();
            for (SQLExpr column : x.getColumns()) {
                columns.add(column.toString());
            }
            insert.setColumns(columns);
        }
        
        if (x.getValuesList() != null && !x.getValuesList().isEmpty()) {
            List<List<SqlJsonDto.ExprDto>> valuesList = new ArrayList<>();
            for (SQLInsertStatement.ValuesClause values : x.getValuesList()) {
                List<SqlJsonDto.ExprDto> row = new ArrayList<>();
                for (SQLExpr expr : values.getValues()) {
                    row.add(convertExpr(expr));
                }
                valuesList.add(row);
            }
            insert.setValues(valuesList);
        }
        
        if (x.getQuery() != null) {
            SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
            SQLSelectQuery query = x.getQuery().getQuery();
            if (query instanceof SQLSelectQueryBlock) {
                convertSelect((SQLSelectQueryBlock) query, select);
            }
            insert.setSelect(select);
        }
        
        return false;
    }
    
    @Override
    public boolean visit(SQLUpdateStatement x) {
        result = new SqlJsonDto();
        result.setType("UPDATE");
        SqlJsonDto.UpdateDto update = new SqlJsonDto.UpdateDto();
        result.setUpdate(update);
        
        if (x.getTableSource() != null) {
            update.setTable(getTableName(x.getTableSource()));
        }
        
        if (x.getItems() != null && !x.getItems().isEmpty()) {
            Map<String, SqlJsonDto.ExprDto> setMap = new HashMap<>();
            for (SQLUpdateSetItem item : x.getItems()) {
                String column = item.getColumn().toString();
                SqlJsonDto.ExprDto value = convertExpr(item.getValue());
                setMap.put(column, value);
            }
            update.setSet(setMap);
        }
        
        if (x.getWhere() != null) {
            update.setWhere(convertExpr(x.getWhere()));
        }
        
        return false;
    }
    
    @Override
    public boolean visit(SQLDeleteStatement x) {
        result = new SqlJsonDto();
        result.setType("DELETE");
        SqlJsonDto.DeleteDto delete = new SqlJsonDto.DeleteDto();
        result.setDelete(delete);
        
        if (x.getTableSource() != null) {
            delete.setTable(getTableName(x.getTableSource()));
        }
        
        if (x.getWhere() != null) {
            delete.setWhere(convertExpr(x.getWhere()));
        }
        
        return false;
    }
    
    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        if (result != null && result.getSelect() != null) {
            convertSelect(x, result.getSelect());
        }
        return false;
    }
    
    private void convertSelect(SQLSelectQueryBlock x, SqlJsonDto.SelectDto select) {
        // SELECT列表
        if (x.getSelectList() != null) {
            List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
            for (SQLSelectItem item : x.getSelectList()) {
                SqlJsonDto.SelectItemDto dto = new SqlJsonDto.SelectItemDto();
                dto.setExpr(convertExpr(item.getExpr()));
                if (item.getAlias() != null) {
                    dto.setAlias(item.getAlias());
                }
                selectList.add(dto);
            }
            select.setSelectList(selectList);
        }
        
        // FROM子句
        if (x.getFrom() != null) {
            select.setFrom(convertTableSource(x.getFrom()));
        }
        
        // WHERE子句
        if (x.getWhere() != null) {
            select.setWhere(convertExpr(x.getWhere()));
        }
        
        // GROUP BY子句
        if (x.getGroupBy() != null) {
            SqlJsonDto.GroupByDto groupBy = new SqlJsonDto.GroupByDto();
            List<SqlJsonDto.ExprDto> items = new ArrayList<>();
            for (SQLExpr expr : x.getGroupBy().getItems()) {
                items.add(convertExpr(expr));
            }
            groupBy.setItems(items);
            if (x.getGroupBy().getHaving() != null) {
                groupBy.setHaving(convertExpr(x.getGroupBy().getHaving()));
            }
            select.setGroupBy(groupBy);
        }
        
        // ORDER BY子句
        if (x.getOrderBy() != null) {
            SqlJsonDto.OrderByDto orderBy = new SqlJsonDto.OrderByDto();
            List<SqlJsonDto.OrderItemDto> items = new ArrayList<>();
            for (SQLSelectOrderByItem item : x.getOrderBy().getItems()) {
                SqlJsonDto.OrderItemDto dto = new SqlJsonDto.OrderItemDto();
                dto.setExpr(convertExpr(item.getExpr()));
                dto.setDirection(item.getType() == SQLOrderingSpecification.DESC ? "DESC" : "ASC");
                items.add(dto);
            }
            orderBy.setItems(items);
            select.setOrderBy(orderBy);
        }
        
        // LIMIT子句
        if (x.getLimit() != null) {
            SqlJsonDto.LimitDto limit = new SqlJsonDto.LimitDto();
            if (x.getLimit().getOffset() != null) {
                SQLExpr offsetExpr = x.getLimit().getOffset();
                if (offsetExpr instanceof SQLIntegerExpr) {
                    limit.setOffset(((SQLIntegerExpr) offsetExpr).getNumber().intValue());
                } else if (offsetExpr instanceof SQLNumberExpr) {
                    limit.setOffset(((SQLNumberExpr) offsetExpr).getNumber().intValue());
                }
            }
            if (x.getLimit().getRowCount() != null) {
                SQLExpr rowCountExpr = x.getLimit().getRowCount();
                if (rowCountExpr instanceof SQLIntegerExpr) {
                    limit.setRowCount(((SQLIntegerExpr) rowCountExpr).getNumber().intValue());
                } else if (rowCountExpr instanceof SQLNumberExpr) {
                    limit.setRowCount(((SQLNumberExpr) rowCountExpr).getNumber().intValue());
                }
            }
            select.setLimit(limit);
        }
    }
    
    private SqlJsonDto.TableSourceDto convertTableSource(SQLTableSource tableSource) {
        SqlJsonDto.TableSourceDto dto = new SqlJsonDto.TableSourceDto();
        
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource exprTable = (SQLExprTableSource) tableSource;
            dto.setType("TABLE");
            dto.setTable(getTableName(exprTable));
            if (exprTable.getAlias() != null) {
                dto.setAlias(exprTable.getAlias());
            }
        } else if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTable = (SQLJoinTableSource) tableSource;
            dto.setType("JOIN");
            dto.setLeft(convertTableSource(joinTable.getLeft()));
            dto.setRight(convertTableSource(joinTable.getRight()));
            dto.setJoinType(getJoinType(joinTable.getJoinType()));
            if (joinTable.getCondition() != null) {
                dto.setCondition(convertExpr(joinTable.getCondition()));
            }
        } else if (tableSource instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource subqueryTable = (SQLSubqueryTableSource) tableSource;
            dto.setType("SUBQUERY");
            SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
            convertSelect((SQLSelectQueryBlock) subqueryTable.getSelect().getQuery(), select);
            dto.setSubQuery(select);
            if (subqueryTable.getAlias() != null) {
                dto.setAlias(subqueryTable.getAlias());
            }
        }
        
        return dto;
    }
    
    private String getJoinType(SQLJoinTableSource.JoinType joinType) {
        if (joinType == null) {
            return "INNER";
        }
        switch (joinType) {
            case INNER_JOIN:
                return "INNER";
            case LEFT_OUTER_JOIN:
                return "LEFT";
            case RIGHT_OUTER_JOIN:
                return "RIGHT";
            case FULL_OUTER_JOIN:
                return "FULL";
            case CROSS_JOIN:
                return "CROSS";
            default:
                return "INNER";
        }
    }
    
    private String getTableName(SQLTableSource tableSource) {
        if (tableSource instanceof SQLExprTableSource) {
            SQLExpr expr = ((SQLExprTableSource) tableSource).getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                return ((SQLIdentifierExpr) expr).getName();
            } else if (expr instanceof SQLPropertyExpr) {
                return expr.toString();
            }
        }
        return tableSource.toString();
    }
    
    private SqlJsonDto.ExprDto convertExpr(SQLExpr expr) {
        if (expr == null) {
            return null;
        }
        
        SqlJsonDto.ExprDto dto = new SqlJsonDto.ExprDto();
        
        if (expr instanceof SQLIdentifierExpr) {
            dto.setType("IDENTIFIER");
            dto.setName(((SQLIdentifierExpr) expr).getName());
        } else if (expr instanceof SQLPropertyExpr) {
            dto.setType("PROPERTY");
            SQLPropertyExpr prop = (SQLPropertyExpr) expr;
            dto.setOwner(prop.getOwner().toString());
            dto.setName(prop.getName());
        } else if (expr instanceof SQLCharExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("STRING");
            dto.setValue(((SQLCharExpr) expr).getText());
        } else if (expr instanceof SQLIntegerExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("NUMBER");
            dto.setValue(((SQLIntegerExpr) expr).getNumber());
        } else if (expr instanceof SQLNumberExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("NUMBER");
            dto.setValue(((SQLNumberExpr) expr).getNumber());
        } else if (expr instanceof SQLBooleanExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("BOOLEAN");
            dto.setValue(((SQLBooleanExpr) expr).getBooleanValue());
        } else if (expr instanceof SQLNullExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("NULL");
            dto.setValue(null);
        } else if (expr instanceof SQLDateExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("DATE");
            dto.setValue(((SQLDateExpr) expr).getLiteral());
        } else if (expr instanceof SQLTimeExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("TIME");
            dto.setValue(((SQLTimeExpr) expr).getLiteral());
        } else if (expr instanceof SQLTimestampExpr) {
            dto.setType("LITERAL");
            dto.setLiteralType("TIMESTAMP");
            dto.setValue(((SQLTimestampExpr) expr).getLiteral());
        } else if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOp = (SQLBinaryOpExpr) expr;
            dto.setType("BINARY_OP");
            dto.setOperator(getOperator(binaryOp.getOperator()));
            dto.setLeft(convertExpr(binaryOp.getLeft()));
            dto.setRight(convertExpr(binaryOp.getRight()));
        } else if (expr instanceof SQLMethodInvokeExpr) {
            SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) expr;
            dto.setType("FUNCTION");
            dto.setFunctionName(method.getMethodName());
            if (method.getArguments() != null) {
                List<SqlJsonDto.ExprDto> args = new ArrayList<>();
                for (SQLExpr arg : method.getArguments()) {
                    args.add(convertExpr(arg));
                }
                dto.setArguments(args);
            }
            // 检查是否有DISTINCT选项
            try {
                Object option = method.getAttribute("DISTINCT");
                dto.setDistinct(option != null);
            } catch (Exception e) {
                dto.setDistinct(false);
            }
        } else if (expr instanceof SQLCaseExpr) {
            SQLCaseExpr caseExpr = (SQLCaseExpr) expr;
            dto.setType("CASE");
            if (caseExpr.getValueExpr() != null) {
                dto.setCaseExpr(convertExpr(caseExpr.getValueExpr()));
            }
            if (caseExpr.getItems() != null) {
                List<SqlJsonDto.CaseWhenDto> whenList = new ArrayList<>();
                for (SQLCaseExpr.Item item : caseExpr.getItems()) {
                    SqlJsonDto.CaseWhenDto whenDto = new SqlJsonDto.CaseWhenDto();
                    whenDto.setWhen(convertExpr(item.getConditionExpr()));
                    whenDto.setThen(convertExpr(item.getValueExpr()));
                    whenList.add(whenDto);
                }
                dto.setWhenList(whenList);
            }
            if (caseExpr.getElseExpr() != null) {
                dto.setElseExpr(convertExpr(caseExpr.getElseExpr()));
            }
        } else if (expr instanceof SQLInListExpr) {
            SQLInListExpr inExpr = (SQLInListExpr) expr;
            dto.setType("IN");
            dto.setInExpr(convertExpr(inExpr.getExpr()));
            if (inExpr.getTargetList() != null) {
                List<SqlJsonDto.ExprDto> values = new ArrayList<>();
                for (SQLExpr value : inExpr.getTargetList()) {
                    values.add(convertExpr(value));
                }
                dto.setInValues(values);
            }
            dto.setNot(inExpr.isNot());
        } else if (expr instanceof SQLBetweenExpr) {
            SQLBetweenExpr betweenExpr = (SQLBetweenExpr) expr;
            dto.setType("BETWEEN");
            dto.setBetweenExpr(convertExpr(betweenExpr.getTestExpr()));
            dto.setBetweenStart(convertExpr(betweenExpr.getBeginExpr()));
            dto.setBetweenEnd(convertExpr(betweenExpr.getEndExpr()));
            dto.setNot(betweenExpr.isNot());
        } else if (expr instanceof SQLExistsExpr) {
            SQLExistsExpr existsExpr = (SQLExistsExpr) expr;
            dto.setType("EXISTS");
            if (existsExpr.getSubQuery() != null) {
                SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
                convertSelect((SQLSelectQueryBlock) existsExpr.getSubQuery().getQuery(), select);
                dto.setExistsQuery(select);
            }
            dto.setNot(existsExpr.isNot());
        } else if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) expr;
            dto.setType("SUBQUERY");
            SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
            convertSelect((SQLSelectQueryBlock) queryExpr.getSubQuery().getQuery(), select);
            dto.setSubQuery(select);
        } else if (expr instanceof SQLVariantRefExpr) {
            SQLVariantRefExpr varExpr = (SQLVariantRefExpr) expr;
            dto.setType("PARAMETER");
            dto.setParameterName(varExpr.getName());
        } else if (expr instanceof SQLNotExpr) {
            SQLNotExpr notExpr = (SQLNotExpr) expr;
            SqlJsonDto.ExprDto inner = convertExpr(notExpr.getExpr());
            if (inner != null) {
                inner.setNot(true);
                return inner;
            }
        }
        
        return dto;
    }
    
    private String getOperator(SQLBinaryOperator operator) {
        if (operator == null) {
            return null;
        }
        switch (operator) {
            case Equality:
                return "=";
            case NotEqual:
                return "!=";
            case LessThan:
                return "<";
            case LessThanOrEqual:
                return "<=";
            case GreaterThan:
                return ">";
            case GreaterThanOrEqual:
                return ">=";
            case BooleanAnd:
                return "AND";
            case BooleanOr:
                return "OR";
            case Add:
                return "+";
            case Subtract:
                return "-";
            case Multiply:
                return "*";
            case Divide:
                return "/";
            case Modulus:
                return "%";
            case Like:
                return "LIKE";
            case NotLike:
                return "NOT_LIKE";
            default:
                return operator.name;
        }
    }
    
    // 内部类，用于表示表达式DTO
    @lombok.Data
    public static class ExprDto {
        private String type;
        private String name;
        private String owner;
        private Object value;
        private String literalType;
        private String operator;
        private ExprDto left;
        private ExprDto right;
        private String functionName;
        private List<ExprDto> arguments;
        private Boolean distinct;
        private ExprDto caseExpr;
        private List<SqlJsonDto.CaseWhenDto> whenList;
        private ExprDto elseExpr;
        private ExprDto inExpr;
        private List<ExprDto> inValues;
        private Boolean not;
        private ExprDto betweenExpr;
        private ExprDto betweenStart;
        private ExprDto betweenEnd;
        private SqlJsonDto.SelectDto existsQuery;
        private SqlJsonDto.SelectDto subQuery;
        private String parameterName;
    }
}

