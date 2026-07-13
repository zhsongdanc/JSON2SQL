package com.zhsong.sql.example;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SQLLineageVisitor extends SQLASTVisitorAdapter {

    private List<String> sourceTableNames = new ArrayList<>();

    public List<String> getSourceTableNames() {
        return sourceTableNames;
    }

    @Override
    public void endVisit(SQLSelectStatement select) {
        if (isSubQueryClause(select)) {
            return;
        }

        SQLSelectQuery sqlSelectQuery = select.getSelect().getQuery();
        if (sqlSelectQuery == null) {
            return;
        }

        if (sqlSelectQuery instanceof SQLUnionQuery) {
            sqlSelectQuery = ((SQLUnionQuery) sqlSelectQuery).getRelations().get(0);
        }

        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            SQLTableSource tableSource = sqlSelectQueryBlock.getFrom();
            extractTableNames(tableSource);
        }
    }

    private void extractTableNames(SQLTableSource tableSource) {
        if (tableSource == null) {
            return;
        }

        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
            String tableName = exprTableSource.getTableName();
            if (!sourceTableNames.contains(tableName)) {
                sourceTableNames.add(tableName);
            }
        } else if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
            extractTableNames(joinTableSource.getLeft());
            extractTableNames(joinTableSource.getRight());
        } else if (tableSource instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource subqueryTableSource = (SQLSubqueryTableSource) tableSource;
            SQLSelectQuery subQuery = subqueryTableSource.getSelect().getQuery();
            if (subQuery instanceof SQLSelectQueryBlock) {
                extractTableNames(((SQLSelectQueryBlock) subQuery).getFrom());
            }
        } else if (tableSource instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource unionQueryTableSource = (SQLUnionQueryTableSource) tableSource;
            for (SQLSelectQuery unionQuery : unionQueryTableSource.getUnion().getRelations()) {
                if (unionQuery instanceof SQLSelectQueryBlock) {
                    extractTableNames(((SQLSelectQueryBlock) unionQuery).getFrom());
                }
            }
        }
    }

    @Override
    public void endVisit(SQLWithSubqueryClause x) {
        for (SQLWithSubqueryClause.Entry entry : x.getEntries()) {
            SQLSelectQuery sqlSelectQuery = entry.getSubQuery().getQuery();
            if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
                extractTableNames(((SQLSelectQueryBlock) sqlSelectQuery).getFrom());
            }
        }
    }

    private boolean isSubQueryClause(SQLObject sqlObject) {
        if (sqlObject == null) {
            return false;
        }

        while (sqlObject != null) {
            if (sqlObject instanceof SQLWithSubqueryClause || sqlObject instanceof SQLSubqueryTableSource) {
                return true;
            }
            sqlObject = sqlObject.getParent();
        }

        return false;
    }
}