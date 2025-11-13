package com.zhsong.sql.converter;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.zhsong.sql.dto.SqlJsonDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON转AST转换器测试
 */
class JsonToAstConverterTest {
    
    private JsonToAstConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new JsonToAstConverter();
    }
    
    @Test
    void testSimpleSelect() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        
        SqlJsonDto.SelectItemDto item1 = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr1 = new SqlJsonDto.ExprDto();
        expr1.setType("IDENTIFIER");
        expr1.setName("id");
        item1.setExpr(expr1);
        selectList.add(item1);
        
        SqlJsonDto.SelectItemDto item2 = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr2 = new SqlJsonDto.ExprDto();
        expr2.setType("IDENTIFIER");
        expr2.setName("name");
        item2.setExpr(expr2);
        selectList.add(item2);
        
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithWhere() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("BINARY_OP");
        where.setOperator("=");
        
        SqlJsonDto.ExprDto left = new SqlJsonDto.ExprDto();
        left.setType("IDENTIFIER");
        left.setName("id");
        where.setLeft(left);
        
        SqlJsonDto.ExprDto right = new SqlJsonDto.ExprDto();
        right.setType("LITERAL");
        right.setLiteralType("NUMBER");
        right.setValue(1);
        where.setRight(right);
        
        select.setWhere(where);
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithJoin() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto join = new SqlJsonDto.TableSourceDto();
        join.setType("JOIN");
        join.setJoinType("INNER");
        
        SqlJsonDto.TableSourceDto left = new SqlJsonDto.TableSourceDto();
        left.setType("TABLE");
        left.setTable("users");
        left.setAlias("u");
        join.setLeft(left);
        
        SqlJsonDto.TableSourceDto right = new SqlJsonDto.TableSourceDto();
        right.setType("TABLE");
        right.setTable("orders");
        right.setAlias("o");
        join.setRight(right);
        
        SqlJsonDto.ExprDto condition = new SqlJsonDto.ExprDto();
        condition.setType("BINARY_OP");
        condition.setOperator("=");
        
        SqlJsonDto.ExprDto condLeft = new SqlJsonDto.ExprDto();
        condLeft.setType("PROPERTY");
        condLeft.setOwner("u");
        condLeft.setName("id");
        condition.setLeft(condLeft);
        
        SqlJsonDto.ExprDto condRight = new SqlJsonDto.ExprDto();
        condRight.setType("PROPERTY");
        condRight.setOwner("o");
        condRight.setName("user_id");
        condition.setRight(condRight);
        
        join.setCondition(condition);
        select.setFrom(join);
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithGroupBy() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("user_id");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("orders");
        select.setFrom(from);
        
        SqlJsonDto.GroupByDto groupBy = new SqlJsonDto.GroupByDto();
        List<SqlJsonDto.ExprDto> items = new ArrayList<>();
        SqlJsonDto.ExprDto groupExpr = new SqlJsonDto.ExprDto();
        groupExpr.setType("IDENTIFIER");
        groupExpr.setName("user_id");
        items.add(groupExpr);
        groupBy.setItems(items);
        select.setGroupBy(groupBy);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithOrderBy() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.OrderByDto orderBy = new SqlJsonDto.OrderByDto();
        List<SqlJsonDto.OrderItemDto> orderItems = new ArrayList<>();
        SqlJsonDto.OrderItemDto orderItem = new SqlJsonDto.OrderItemDto();
        SqlJsonDto.ExprDto orderExpr = new SqlJsonDto.ExprDto();
        orderExpr.setType("IDENTIFIER");
        orderExpr.setName("id");
        orderItem.setExpr(orderExpr);
        orderItem.setDirection("DESC");
        orderItems.add(orderItem);
        orderBy.setItems(orderItems);
        select.setOrderBy(orderBy);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithLimit() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.LimitDto limit = new SqlJsonDto.LimitDto();
        limit.setRowCount(10);
        limit.setOffset(20);
        select.setLimit(limit);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithFunction() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("FUNCTION");
        expr.setFunctionName("COUNT");
        List<SqlJsonDto.ExprDto> args = new ArrayList<>();
        SqlJsonDto.ExprDto arg = new SqlJsonDto.ExprDto();
        arg.setType("IDENTIFIER");
        arg.setName("*");
        args.add(arg);
        expr.setArguments(args);
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithCaseExpression() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("CASE");
        
        List<SqlJsonDto.CaseWhenDto> whenList = new ArrayList<>();
        SqlJsonDto.CaseWhenDto when1 = new SqlJsonDto.CaseWhenDto();
        SqlJsonDto.ExprDto whenExpr = new SqlJsonDto.ExprDto();
        whenExpr.setType("BINARY_OP");
        whenExpr.setOperator("<");
        SqlJsonDto.ExprDto whenLeft = new SqlJsonDto.ExprDto();
        whenLeft.setType("IDENTIFIER");
        whenLeft.setName("age");
        whenExpr.setLeft(whenLeft);
        SqlJsonDto.ExprDto whenRight = new SqlJsonDto.ExprDto();
        whenRight.setType("LITERAL");
        whenRight.setLiteralType("NUMBER");
        whenRight.setValue(18);
        whenExpr.setRight(whenRight);
        when1.setWhen(whenExpr);
        
        SqlJsonDto.ExprDto thenExpr = new SqlJsonDto.ExprDto();
        thenExpr.setType("LITERAL");
        thenExpr.setLiteralType("STRING");
        thenExpr.setValue("minor");
        when1.setThen(thenExpr);
        whenList.add(when1);
        
        expr.setWhenList(whenList);
        
        SqlJsonDto.ExprDto elseExpr = new SqlJsonDto.ExprDto();
        elseExpr.setType("LITERAL");
        elseExpr.setLiteralType("STRING");
        elseExpr.setValue("adult");
        expr.setElseExpr(elseExpr);
        
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithInExpression() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("IN");
        SqlJsonDto.ExprDto inExpr = new SqlJsonDto.ExprDto();
        inExpr.setType("IDENTIFIER");
        inExpr.setName("id");
        where.setInExpr(inExpr);
        
        List<SqlJsonDto.ExprDto> inValues = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            SqlJsonDto.ExprDto value = new SqlJsonDto.ExprDto();
            value.setType("LITERAL");
            value.setLiteralType("NUMBER");
            value.setValue(i);
            inValues.add(value);
        }
        where.setInValues(inValues);
        select.setWhere(where);
        
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testSelectWithBetweenExpression() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("SELECT");
        
        SqlJsonDto.SelectDto select = new SqlJsonDto.SelectDto();
        List<SqlJsonDto.SelectItemDto> selectList = new ArrayList<>();
        SqlJsonDto.SelectItemDto item = new SqlJsonDto.SelectItemDto();
        SqlJsonDto.ExprDto expr = new SqlJsonDto.ExprDto();
        expr.setType("IDENTIFIER");
        expr.setName("*");
        item.setExpr(expr);
        selectList.add(item);
        select.setSelectList(selectList);
        
        SqlJsonDto.TableSourceDto from = new SqlJsonDto.TableSourceDto();
        from.setType("TABLE");
        from.setTable("users");
        select.setFrom(from);
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("BETWEEN");
        SqlJsonDto.ExprDto betweenExpr = new SqlJsonDto.ExprDto();
        betweenExpr.setType("IDENTIFIER");
        betweenExpr.setName("age");
        where.setBetweenExpr(betweenExpr);
        
        SqlJsonDto.ExprDto start = new SqlJsonDto.ExprDto();
        start.setType("LITERAL");
        start.setLiteralType("NUMBER");
        start.setValue(18);
        where.setBetweenStart(start);
        
        SqlJsonDto.ExprDto end = new SqlJsonDto.ExprDto();
        end.setType("LITERAL");
        end.setLiteralType("NUMBER");
        end.setValue(65);
        where.setBetweenEnd(end);
        
        select.setWhere(where);
        dto.setSelect(select);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testInsertWithValues() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("INSERT");
        
        SqlJsonDto.InsertDto insert = new SqlJsonDto.InsertDto();
        insert.setTable("users");
        
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("name");
        columns.add("age");
        insert.setColumns(columns);
        
        List<List<SqlJsonDto.ExprDto>> values = new ArrayList<>();
        List<SqlJsonDto.ExprDto> row = new ArrayList<>();
        
        SqlJsonDto.ExprDto id = new SqlJsonDto.ExprDto();
        id.setType("LITERAL");
        id.setLiteralType("NUMBER");
        id.setValue(1);
        row.add(id);
        
        SqlJsonDto.ExprDto name = new SqlJsonDto.ExprDto();
        name.setType("LITERAL");
        name.setLiteralType("STRING");
        name.setValue("John");
        row.add(name);
        
        SqlJsonDto.ExprDto age = new SqlJsonDto.ExprDto();
        age.setType("LITERAL");
        age.setLiteralType("NUMBER");
        age.setValue(25);
        row.add(age);
        
        values.add(row);
        insert.setValues(values);
        dto.setInsert(insert);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testUpdate() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("UPDATE");
        
        SqlJsonDto.UpdateDto update = new SqlJsonDto.UpdateDto();
        update.setTable("users");
        
        Map<String, SqlJsonDto.ExprDto> set = new HashMap<>();
        SqlJsonDto.ExprDto name = new SqlJsonDto.ExprDto();
        name.setType("LITERAL");
        name.setLiteralType("STRING");
        name.setValue("Jane");
        set.put("name", name);
        
        SqlJsonDto.ExprDto age = new SqlJsonDto.ExprDto();
        age.setType("LITERAL");
        age.setLiteralType("NUMBER");
        age.setValue(26);
        set.put("age", age);
        
        update.setSet(set);
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("BINARY_OP");
        where.setOperator("=");
        SqlJsonDto.ExprDto left = new SqlJsonDto.ExprDto();
        left.setType("IDENTIFIER");
        left.setName("id");
        where.setLeft(left);
        SqlJsonDto.ExprDto right = new SqlJsonDto.ExprDto();
        right.setType("LITERAL");
        right.setLiteralType("NUMBER");
        right.setValue(1);
        where.setRight(right);
        update.setWhere(where);
        
        dto.setUpdate(update);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testDelete() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("DELETE");
        
        SqlJsonDto.DeleteDto delete = new SqlJsonDto.DeleteDto();
        delete.setTable("users");
        
        SqlJsonDto.ExprDto where = new SqlJsonDto.ExprDto();
        where.setType("BINARY_OP");
        where.setOperator("=");
        SqlJsonDto.ExprDto left = new SqlJsonDto.ExprDto();
        left.setType("IDENTIFIER");
        left.setName("id");
        where.setLeft(left);
        SqlJsonDto.ExprDto right = new SqlJsonDto.ExprDto();
        right.setType("LITERAL");
        right.setLiteralType("NUMBER");
        right.setValue(1);
        where.setRight(right);
        delete.setWhere(where);
        
        dto.setDelete(delete);
        
        SQLStatement statement = converter.convert(dto);
        assertNotNull(statement);
    }
    
    @Test
    void testInvalidType() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType("INVALID");
        
        assertThrows(IllegalArgumentException.class, () -> converter.convert(dto));
    }
    
    @Test
    void testNullType() {
        SqlJsonDto dto = new SqlJsonDto();
        dto.setType(null);
        
        assertThrows(IllegalArgumentException.class, () -> converter.convert(dto));
    }
    
    @Test
    void testNullDto() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
    }
}

