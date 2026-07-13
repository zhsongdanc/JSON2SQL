package com.zhsong.sql.example;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class SQLContentUtil {

    private static final String TMP_TBL_NAME = "TEMP_TBL_7258089599";

    public static String appendIncorrectCondition(String sqlContent) {
        sqlContent = handleSqlSuffix(sqlContent);
        return String.format("select * from ( %s ) temp where 1=2", sqlContent);
    }

    public static String getCountSql(String sqlContent) {
        sqlContent = handleSqlSuffix(sqlContent);
        return String.format("select count(1) from ( %s ) as %s", sqlContent, TMP_TBL_NAME);
    }

    public static String getPageSql(String sqlContent, int pageIndex, int pageSize) {
        sqlContent = handleSqlSuffix(sqlContent);
        int skipSize = (pageIndex - 1) * pageSize;
        return String.format("select * from ( %s ) as %s limit %s,%s",
                sqlContent, TMP_TBL_NAME, skipSize, pageSize);
    }

    public static String handleSqlSuffix(String sqlContent) {
        if (StringUtils.isEmpty(sqlContent)) {
            return "";
        }
        boolean flag = true;
        while (flag) {
            if (sqlContent.endsWith(";")) {
                sqlContent = sqlContent.substring(0, sqlContent.length() - 1);
            } else if (sqlContent.endsWith("\\n")) {
                sqlContent = sqlContent.substring(0, sqlContent.length() - 2);
            } else if (sqlContent.endsWith(" ")) {
                sqlContent = sqlContent.substring(0, sqlContent.length() - 1);
            } else {
                flag = false;
            }
        }
        return sqlContent.trim();
    }

    public static String handleWhereSqlPreSuffix(String sqlContent) {
        if (StringUtils.isEmpty(sqlContent)) {
            return "";
        }
        sqlContent = sqlContent.trim();
        if (sqlContent.toLowerCase().startsWith("where")) {
            sqlContent = sqlContent.substring(5).trim();
        }
        sqlContent = handleSqlSuffix(sqlContent);
        return sqlContent.trim();
    }

    public static String combineSelectAndWhere(String selectAndJoin, String callerWhere, String configWhere, String dbType) {
        if (StringUtils.isEmpty(callerWhere) && StringUtils.isEmpty(configWhere)) {
            return selectAndJoin;
        }

        if (!StringUtils.isEmpty(callerWhere)) {
            selectAndJoin = SQLUtils.addCondition(selectAndJoin, callerWhere, DbType.valueOf(dbType.toUpperCase()));
        }

        if (!StringUtils.isEmpty(configWhere)) {
            selectAndJoin = SQLUtils.addCondition(selectAndJoin, configWhere, DbType.valueOf(dbType.toUpperCase()));
        }

        return selectAndJoin;
    }

    public static String replaceArg(String sql, String arg, String replacement) {
        sql = sql.replace(":" + arg + ")", replacement + ")");
        sql = sql.replace(":" + arg + " ", replacement + " ");
        sql = sql.replace(":" + arg + ",", replacement + ",");
        sql = sql.replaceAll(":" + arg + "\n", replacement + "\n");
        sql = sql.replaceAll(":" + arg + "$", replacement);

        return sql;
    }
}