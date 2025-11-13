# JSON2SQL

一个基于Druid AST的SQL和JSON双向转换工具，支持MySQL和Oracle数据库。

## 功能特性

- ✅ SQL转JSON：将SQL语句转换为结构化的JSON格式
- ✅ JSON转SQL：将JSON格式转换为SQL语句
- ✅ 支持多种SQL语句类型：SELECT、INSERT、UPDATE、DELETE
- ✅ 支持多种数据库方言：MySQL、Oracle
- ✅ 支持复杂的SQL语法：
  - 子查询
  - JOIN操作（INNER、LEFT、RIGHT、FULL、CROSS）
  - 聚合函数
  - CASE表达式
  - IN、EXISTS、BETWEEN表达式
  - GROUP BY、ORDER BY、LIMIT等子句

## 技术架构

### 设计模式

1. **策略模式**：用于支持不同的数据库方言（MySQL、Oracle）
2. **访问者模式**：用于遍历Druid AST节点，实现AST到JSON的转换
3. **构建器模式**：用于构建AST节点，实现JSON到AST的转换
4. **工厂模式**：用于创建数据库方言实例

### 项目结构

```
src/main/java/com/zhsong/sql/
├── dto/                    # JSON格式定义
│   └── SqlJsonDto.java
├── converter/              # 转换器
│   ├── AstToJsonConverter.java    # AST转JSON
│   └── JsonToAstConverter.java    # JSON转AST
├── dialect/                # 数据库方言
│   ├── DatabaseDialect.java
│   └── DatabaseDialectFactory.java
├── service/                # 服务层
│   └── SqlJsonConverterService.java
├── controller/             # 控制器
│   ├── SqlJsonController.java
│   └── GlobalExceptionHandler.java
└── exception/              # 异常处理
    └── ConverterException.java
```

## 快速开始

### 1. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8477` 启动。

### 2. API接口

#### SQL转JSON

**接口**: `POST /api/sql-json/sql-to-json`

**请求示例**:
```json
{
  "sql": "SELECT id, name FROM users WHERE id = 1 AND age > 18",
  "dbType": "mysql"
}
```

**响应示例**:
```json
{
  "type": "SELECT",
  "select": {
    "selectList": [
      {
        "expr": {
          "type": "IDENTIFIER",
          "name": "id"
        }
      },
      {
        "expr": {
          "type": "IDENTIFIER",
          "name": "name"
        }
      }
    ],
    "from": {
      "type": "TABLE",
      "table": "users"
    },
    "where": {
      "type": "BINARY_OP",
      "operator": "AND",
      "left": {
        "type": "BINARY_OP",
        "operator": "=",
        "left": {
          "type": "IDENTIFIER",
          "name": "id"
        },
        "right": {
          "type": "LITERAL",
          "literalType": "NUMBER",
          "value": 1
        }
      },
      "right": {
        "type": "BINARY_OP",
        "operator": ">",
        "left": {
          "type": "IDENTIFIER",
          "name": "age"
        },
        "right": {
          "type": "LITERAL",
          "literalType": "NUMBER",
          "value": 18
        }
      }
    }
  }
}
```

#### JSON转SQL

**接口**: `POST /api/sql-json/json-to-sql`

**请求示例**:
```json
{
  "json": {
    "type": "SELECT",
    "select": {
      "selectList": [
        {
          "expr": {
            "type": "IDENTIFIER",
            "name": "id"
          }
        },
        {
          "expr": {
            "type": "IDENTIFIER",
            "name": "name"
          }
        }
      ],
      "from": {
        "type": "TABLE",
        "table": "users"
      },
      "where": {
        "type": "BINARY_OP",
        "operator": "=",
        "left": {
          "type": "IDENTIFIER",
          "name": "id"
        },
        "right": {
          "type": "LITERAL",
          "literalType": "NUMBER",
          "value": 1
        }
      }
    }
  },
  "dbType": "mysql"
}
```

**响应示例**:
```json
{
  "sql": "SELECT id, name\nFROM users\nWHERE id = 1"
}
```

## JSON格式说明

### SELECT语句

```json
{
  "type": "SELECT",
  "select": {
    "selectList": [...],      // 查询字段列表
    "from": {...},            // FROM子句
    "where": {...},           // WHERE条件
    "groupBy": {...},         // GROUP BY子句
    "orderBy": {...},         // ORDER BY子句
    "limit": {...},           // LIMIT子句
    "having": {...}           // HAVING子句
  }
}
```

### INSERT语句

```json
{
  "type": "INSERT",
  "insert": {
    "table": "users",
    "columns": ["id", "name", "age"],
    "values": [
      [
        {"type": "LITERAL", "literalType": "NUMBER", "value": 1},
        {"type": "LITERAL", "literalType": "STRING", "value": "John"},
        {"type": "LITERAL", "literalType": "NUMBER", "value": 25}
      ]
    ]
  }
}
```

### UPDATE语句

```json
{
  "type": "UPDATE",
  "update": {
    "table": "users",
    "set": {
      "name": {"type": "LITERAL", "literalType": "STRING", "value": "Jane"},
      "age": {"type": "LITERAL", "literalType": "NUMBER", "value": 26}
    },
    "where": {...}
  }
}
```

### DELETE语句

```json
{
  "type": "DELETE",
  "delete": {
    "table": "users",
    "where": {...}
  }
}
```

### 表达式类型

- **IDENTIFIER**: 标识符（如列名）
- **PROPERTY**: 属性表达式（如 `table.column`）
- **LITERAL**: 字面量（字符串、数字、布尔值、NULL、日期等）
- **BINARY_OP**: 二元运算符（=, !=, <, >, AND, OR, +, -, *, /等）
- **FUNCTION**: 函数调用
- **CASE**: CASE表达式
- **IN**: IN表达式
- **BETWEEN**: BETWEEN表达式
- **EXISTS**: EXISTS表达式
- **SUBQUERY**: 子查询
- **PARAMETER**: 参数（如 `?` 或 `:name`）

## 支持的SQL语法

### 基本查询
- SELECT字段列表
- FROM子句（单表、多表、子查询）
- WHERE条件
- GROUP BY和HAVING
- ORDER BY
- LIMIT/OFFSET

### JOIN操作
- INNER JOIN
- LEFT JOIN
- RIGHT JOIN
- FULL JOIN
- CROSS JOIN

### 表达式
- 算术运算（+, -, *, /, %）
- 比较运算（=, !=, <, <=, >, >=）
- 逻辑运算（AND, OR, NOT）
- LIKE/NOT LIKE
- IN/NOT IN
- BETWEEN/NOT BETWEEN
- EXISTS/NOT EXISTS
- CASE WHEN
- 函数调用
- 子查询

### 数据操作
- INSERT（VALUES和SELECT两种形式）
- UPDATE
- DELETE

## 使用示例

### 复杂查询示例

**SQL**:
```sql
SELECT u.id, u.name, COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE u.age > 18
GROUP BY u.id, u.name
HAVING COUNT(o.id) > 0
ORDER BY order_count DESC
LIMIT 10
```

转换为JSON后，可以通过编程方式修改查询条件，然后再转换回SQL。

## 注意事项

1. 某些复杂的SQL语法可能不完全支持，建议先测试
2. 数据库方言差异可能导致转换结果略有不同
3. 参数化查询中的参数名会被保留
4. 注释在转换过程中可能会丢失

## 依赖

- Spring Boot 2.7.4
- Druid 1.2.11
- Lombok

## 许可证

见 LICENSE 文件
