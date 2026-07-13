package com.zhsong.sql.example;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.zhsong.sql.dto.SqlJsonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InterfaceSQLStructureVisitor extends SQLASTVisitorAdapter {

    private SqlJsonDto result;

    public SqlJsonDto getResult() {
        return result;
    }

    @Override
    public boolean visit(SQLSelectStatement x) {
        result = new SqlJsonDto();
        result.setType("SELECT");
        result.setSelect(new SqlJsonDto.SelectDto());
        return true;
    }

    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        if (result != null && result.getSelect() != null) {
            convertSelectBlock(x, result.getSelect());
        }
        return false;
    }

    private void convertSelectBlock(SQLSelectQueryBlock x, SqlJsonDto.SelectDto select) {
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

        if (x.getFrom() != null) {
            select.setFrom(convertTableSource(x.getFrom()));
        }

        if (x.getWhere() != null) {
            select.setWhere(convertExpr(x.getWhere()));
        }

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

        if (x.getLimit() != null) {
            SqlJsonDto.LimitDto limit = new SqlJsonDto.LimitDto();
            if (x.getLimit().getOffset() != null) {
                SQLExpr offsetExpr = x.getLimit().getOffset();
                if (offsetExpr instanceof SQLIntegerExpr) {
                    limit.setOffset(((SQLIntegerExpr) offsetExpr).getNumber().intValue());
                }
            }
            if (x.getLimit().getRowCount() != null) {
                SQLExpr rowCountExpr = x.getLimit().getRowCount();
                if (rowCountExpr instanceof SQLIntegerExpr) {
                    limit.setRowCount(((SQLIntegerExpr) rowCountExpr).getNumber().intValue());
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
            if (subqueryTable.getSelect().getQuery() instanceof SQLSelectQueryBlock) {
                convertSelectBlock((SQLSelectQueryBlock) subqueryTable.getSelect().getQuery(), select);
            }
            dto.setSubQuery(select);
            if (subqueryTable.getAlias() != null) {
                dto.setAlias(subqueryTable.getAlias());
            }
        } else if (tableSource instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource unionQueryTableSource = (SQLUnionQueryTableSource) tableSource;
            dto.setType("SUBQUERY");
            SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
            if (unionQueryTableSource.getUnion().getRelations().size() > 0) {
                SQLSelectQuery firstQuery = unionQueryTableSource.getUnion().getRelations().get(0);
                if (firstQuery instanceof SQLSelectQueryBlock) {
                    convertSelectBlock((SQLSelectQueryBlock) firstQuery, select);
                }
            }
            dto.setSubQuery(select);
            if (unionQueryTableSource.getAlias() != null) {
                dto.setAlias(unionQueryTableSource.getAlias());
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

    private String getTableName(SQLExprTableSource tableSource) {
        SQLExpr expr = tableSource.getExpr();
        if (expr instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) expr).getName();
        } else if (expr instanceof SQLPropertyExpr) {
            return expr.toString();
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
                if (existsExpr.getSubQuery().getQuery() instanceof SQLSelectQueryBlock) {
                    convertSelectBlock((SQLSelectQueryBlock) existsExpr.getSubQuery().getQuery(), select);
                }
                dto.setExistsQuery(select);
            }
            dto.setNot(existsExpr.isNot());
        } else if (expr instanceof SQLQueryExpr) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) expr;
            dto.setType("SUBQUERY");
            SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
            if (queryExpr.getSubQuery().getQuery() instanceof SQLSelectQueryBlock) {
                convertSelectBlock((SQLSelectQueryBlock) queryExpr.getSubQuery().getQuery(), select);
            }
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
}