# Druid Lexer/Parser 每日学习计划（面试冲刺版）

> **目标**：7-10 天掌握 Druid 解析核心，能在面试中清晰讲解原理、对比实现、回答扩展问题  
> **策略**：每天 2-3 小时，先理解 Druid 核心 → 实现极简版验证 → 深入细节对比

---

## 📋 学习前准备

### 环境搭建
- [ ] 拉取 Druid 源码：`git clone https://github.com/alibaba/druid.git`
- [ ] 导入 IntelliJ IDEA，确保能编译运行
- [ ] 创建学习项目目录：`/learn/druid-study/`（用于存放自己的实现和笔记）

### 工具准备
- [ ] 安装 PlantUML 插件（用于画流程图，可选）
- [ ] 准备笔记工具（Markdown 或 OneNote）

---

## 📅 Day 1：入口流程与整体架构

### 🎯 今日目标
- 理解 SQL 字符串如何进入 Druid 解析流程
- 掌握 Parser 工厂模式
- 画出完整的调用链路图

### 📖 阅读任务（1.5 小时）

#### 1. 入口类（30 分钟）
**阅读顺序**：
1. `com.alibaba.druid.sql.SQLUtils#parseSingleStatement`
2. `com.alibaba.druid.sql.parser.SQLParserUtils#createSQLStatementParser`
3. `com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser` 构造函数

**重点理解**：
- `SQLUtils` 如何根据 `DbType` 选择 Parser
- `SQLParserUtils` 工厂如何创建 `MySqlStatementParser` / `OracleStatementParser`
- Parser 内部持有哪些组件（Lexer、ExprParser）

#### 2. 调试实践（30 分钟）
**任务**：运行并单步调试以下代码

```java
String sql = "SELECT id, name FROM users WHERE id = 1";
SQLStatement stmt = SQLUtils.parseSingleStatement(sql, JdbcConstants.MYSQL);
System.out.println(stmt.getClass().getName());
```

**断点位置**：
- `SQLUtils.parseSingleStatement()` 第一行
- `SQLParserUtils.createSQLStatementParser()` 返回语句
- `MySqlStatementParser` 构造函数

**观察记录**：
- [ ] 记录调用栈（从 `SQLUtils` 到 `MySqlStatementParser` 的完整路径）
- [ ] 记录 `MySqlStatementParser` 内部初始化了哪些字段

#### 3. 架构梳理（30 分钟）
**任务**：画出类关系图

```
SQLUtils
  └─> SQLParserUtils.createSQLStatementParser()
      └─> MySqlStatementParser (持有 MySqlLexer + MySqlExprParser)
          └─> parseStatement() → SQLSelectStatement
```

### ✅ 今日任务清单

- [ ] **任务 1**：在笔记中画出“SQL 字符串 → Parser 实例 → AST”的流程图
- [ ] **任务 2**：写一个测试类 `Day1Test.java`，测试不同 `DbType`（mysql/oracle）的 Parser 创建
- [ ] **任务 3**：回答以下问题（写在笔记里）：
  - 为什么 Druid 要区分 `SQLExprParser` 和 `SQLStatementParser`？
  - 如果我要支持一个新数据库方言，需要实现哪些类？

### 🎤 面试准备检查点

**能回答**：
- ✅ "Druid 如何根据数据库类型选择 Parser？"（工厂模式 + DbType 映射）
- ✅ "Parser 内部有哪些核心组件？"（Lexer + ExprParser + StatementParser）

---

## 📅 Day 2：Lexer 核心机制

### 🎯 今日目标
- 理解 Lexer 如何逐字符识别 Token
- 掌握关键字识别、标识符扫描、字符串处理
- 能解释 Lexer 的状态机本质

### 📖 阅读任务（2 小时）

#### 1. 基础 Lexer（45 分钟）
**阅读顺序**：
1. `com.alibaba.druid.sql.parser.Lexer` 类结构
   - 字段：`ch`（当前字符）、`token`（当前 Token）、`pos`（位置）
   - 方法：`nextToken()`、`next()`、`scanIdentifier()`
2. `com.alibaba.druid.sql.parser.SQLLexer`
   - `scanIdentifier()`：如何识别关键字 vs 普通标识符
   - `scanString()`：字符串字面量处理（转义、引号）
   - `scanNumber()`：数字识别

**重点理解**：
- `nextToken()` 的主循环逻辑：根据 `ch` 的类型分发到不同的 `scanXxx()` 方法
- 关键字表：`SQLLexer` 如何判断 `SELECT` 是关键字而不是标识符

#### 2. 方言扩展（30 分钟）
**阅读**：`com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexer`
- 对比 `SQLLexer`，看 MySQL 扩展了什么：
  - 反引号标识符（`\`table\``）
  - Hint 语法（`/*+ HINT */`）
  - 双问号（`??`）

#### 3. 调试实践（45 分钟）
**任务**：单步调试 Lexer

```java
String sql = "SELECT `user`.id, name FROM users WHERE id = 1";
MySqlLexer lexer = new MySqlLexer(sql);
lexer.nextToken(); // 反复调用，观察 token 变化
while (lexer.token != Token.EOF) {
    System.out.println(lexer.token + " -> " + lexer.stringVal());
    lexer.nextToken();
}
```

**断点位置**：
- `Lexer.nextToken()` 第一行
- `SQLLexer.scanIdentifier()` 内部
- `MySqlLexer.scanIdentifier()`（如果有重写）

**观察记录**：
- [ ] 记录每个 Token 的生成过程（字符 → 方法调用 → Token 类型）
- [ ] 画出 `SELECT` 被识别为关键字的流程

### ✅ 今日任务清单

- [ ] **任务 1**：实现一个极简 Lexer（`SimpleLexer.java`）
  - 支持：关键字（SELECT, FROM, WHERE）、标识符、数字、`=`、`,`、EOF
  - 要求：能解析 `SELECT id FROM users WHERE id = 1` 并输出 Token 序列
- [ ] **任务 2**：在笔记中整理 Token 类型表
  ```
  Token类型 | 识别方法 | 示例
  SELECT   | scanIdentifier() + 关键字表 | SELECT
  IDENTIFIER | scanIdentifier() | users
  LITERAL_INT | scanNumber() | 1
  ```
- [ ] **任务 3**：回答以下问题：
  - Lexer 为什么需要维护 `ch`、`token`、`pos` 这些状态？
  - 如果我要添加一个新关键字（如 `FETCH`），需要改哪里？

### 🎤 面试准备检查点

**能回答**：
- ✅ "Lexer 和 Parser 的区别是什么？"（Lexer：字符 → Token；Parser：Token → AST）
- ✅ "Lexer 如何识别关键字？"（扫描标识符 → 查关键字表 → 返回对应 Token）
- ✅ "如果遇到 `SELECT`，Lexer 怎么知道它是关键字而不是表名？"（关键字表匹配）

---

## 📅 Day 3：表达式 Parser（递归下降核心）

### 🎯 今日目标
- 深入理解递归下降解析
- 掌握表达式解析的优先级处理
- 能解释 `expr()` → `primary()` → `binaryOpRest()` 的调用关系

### 📖 阅读任务（2.5 小时）

#### 1. 表达式解析入口（1 小时）
**阅读顺序**：
1. `com.alibaba.druid.sql.parser.SQLExprParser#expr()`
   - 理解：`expr()` 调用 `primary()` 获取最小单元，再调用 `binaryOpRest()` 处理运算符
2. `com.alibaba.druid.sql.parser.SQLExprParser#primary()`
   - 看如何根据 Token 类型分发：
     - 标识符 → `parseIdentifier()`
     - 字面量 → `parsePrimary()`
     - `(` → 递归调用 `expr()`（括号表达式）
     - 函数名 → `methodRest()`
3. `com.alibaba.druid.sql.parser.SQLExprParser#binaryOpRest()`
   - 理解优先级处理：通过递归层级控制运算符结合顺序

**重点理解**：
- 为什么 `primary()` 是“最小单元”？
- `binaryOpRest()` 如何通过递归实现优先级（乘除优先于加减）？

#### 2. 调试实践（1.5 小时）
**任务**：单步调试表达式解析

```java
String sql = "id = 1 AND status = 'ACTIVE'";
MySqlStatementParser parser = new MySqlStatementParser(sql);
SQLExprParser exprParser = parser.getExprParser();
SQLExpr expr = exprParser.expr();
```

**断点位置**：
- `SQLExprParser.expr()` 第一行
- `SQLExprParser.primary()` 内部
- `SQLExprParser.binaryOpRest()` 循环内

**观察记录**：
- [ ] 画出调用栈：`expr()` → `primary()` → `binaryOpRest()` → `expr()`（递归）
- [ ] 记录 `id = 1` 如何被解析成 `BinaryOpExpr(left=id, op==, right=1)`
- [ ] 记录 `AND` 和 `=` 的优先级如何影响 AST 结构

### ✅ 今日任务清单

- [ ] **任务 1**：实现 `SimpleExprParser.java`
  - 支持：标识符、数字、`=`、`AND`、`OR`
  - 要求：能解析 `id = 1 AND status = 2`，输出 AST（可用简单类表示）
  - **关键**：必须用递归下降实现优先级（`AND` 优先级低于 `=`）
- [ ] **任务 2**：在笔记中画出递归下降的调用图
  ```
  expr()
    └─> primary() → IdentifierExpr("id")
    └─> binaryOpRest()
        └─> 看到 "="，优先级高
        └─> primary() → LiteralExpr(1)
        └─> 组成 BinaryOpExpr(id, =, 1)
        └─> 看到 "AND"，优先级低，返回
  ```
- [ ] **任务 3**：回答以下问题：
  - 为什么递归下降能自动处理优先级？（函数调用层级 = 优先级层级）
  - 如果要支持 `BETWEEN ... AND ...`，应该在哪里添加代码？

### 🎤 面试准备检查点

**能回答**：
- ✅ "递归下降解析是什么？"（文法规则 → 函数调用，通过递归层级实现优先级）
- ✅ "如何解析 `1 + 2 * 3`？"（`parseExpr()` 处理 `+`，`parseTerm()` 处理 `*`，层级控制优先级）
- ✅ "Druid 的 `SQLExprParser.expr()` 是怎么工作的？"（调用 `primary()` 获取最小单元，`binaryOpRest()` 处理运算符）

---

## 📅 Day 4：语句级 Parser（SELECT 解析）

### 🎯 今日目标
- 理解 SELECT 语句的完整解析流程
- 掌握 FROM、WHERE、ORDER BY、LIMIT 的解析顺序
- 能画出 SELECT AST 的完整结构

### 📖 阅读任务（2.5 小时）

#### 1. SELECT 解析入口（1 小时）
**阅读顺序**：
1. `com.alibaba.druid.sql.parser.SQLStatementParser#parseSelect()`
   - 看整体流程：WITH → SELECT → FROM → WHERE → GROUP → ORDER → LIMIT
2. `com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser#parseSelect()`
   - 对比基类，看 MySQL 扩展了什么（如 `LIMIT` 语法）

**重点理解**：
- `parseSelect()` 如何按顺序解析各个子句
- 每个子句解析后如何组装成 `SQLSelectStatement` → `SQLSelectQueryBlock`

#### 2. 子句解析细节（1 小时）
**阅读**：
- `parseSelectList()`：如何解析 `SELECT id, name`
- `parseTableSource()`：如何解析 `FROM users` 或 `FROM users u JOIN orders o`
- `parseWhere()`：调用 `exprParser.expr()` 解析 WHERE 条件
- `parseOrderBy()`：如何解析 `ORDER BY id ASC`

#### 3. 调试实践（30 分钟）
**任务**：单步调试完整 SELECT

```java
String sql = "SELECT id, name FROM users WHERE id = 1 ORDER BY id LIMIT 10";
MySqlStatementParser parser = new MySqlStatementParser(sql);
SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
```

**断点位置**：
- `MySqlStatementParser.parseSelect()` 第一行
- `parseSelectList()` 内部
- `parseTableSource()` 内部
- `parseWhere()` 内部

**观察记录**：
- [ ] 画出 AST 结构：`SQLSelectStatement` → `SQLSelectQueryBlock` → `selectList`/`from`/`where`/`orderBy`/`limit`
- [ ] 记录每个子句解析后如何设置到 `SQLSelectQueryBlock` 的字段

### ✅ 今日任务清单

- [ ] **任务 1**：实现 `SimpleSelectParser.java`
  - 支持：`SELECT col1, col2 FROM table WHERE expr ORDER BY col LIMIT n`
  - 要求：输出简单 AST（可用类表示：`SelectStmt { selectList, from, where, orderBy, limit }`）
- [ ] **任务 2**：在笔记中整理 SELECT 解析顺序表
  ```
  子句 | 解析方法 | AST节点
  SELECT | parseSelectList() | SQLSelectQueryBlock.selectList
  FROM | parseTableSource() | SQLSelectQueryBlock.from
  WHERE | parseWhere() | SQLSelectQueryBlock.where
  ORDER BY | parseOrderBy() | SQLSelectQueryBlock.orderBy
  LIMIT | parseLimit() | SQLSelectQueryBlock.limit
  ```
- [ ] **任务 3**：回答以下问题：
  - 为什么 `parseSelect()` 要按照 WITH → SELECT → FROM → WHERE 的顺序解析？
  - 如果要支持 `SELECT ... FOR UPDATE`，应该在哪里添加代码？

### 🎤 面试准备检查点

**能回答**：
- ✅ "SELECT 语句的解析流程是什么？"（按子句顺序解析，组装成 `SQLSelectQueryBlock`）
- ✅ "FROM 子句如何解析 JOIN？"（`parseTableSource()` 递归解析左表、右表、JOIN 类型、条件）
- ✅ "WHERE 条件如何解析？"（调用 `exprParser.expr()`，返回 `SQLExpr`）

---

## 📅 Day 5：其他语句（INSERT/UPDATE/DELETE）

### 🎯 今日目标
- 理解 INSERT/UPDATE/DELETE 的解析流程
- 对比不同语句的解析差异
- 掌握 VALUES、SET 子句的解析

### 📖 阅读任务（2 小时）

#### 1. INSERT 解析（45 分钟）
**阅读**：`com.alibaba.druid.sql.parser.SQLStatementParser#parseInsert()`
- 看如何解析：`INSERT INTO table (col1, col2) VALUES (val1, val2)`
- 理解 `parseValues()` 如何解析多行 VALUES

#### 2. UPDATE 解析（30 分钟）
**阅读**：`com.alibaba.druid.sql.parser.SQLStatementParser#parseUpdate()`
- 看如何解析：`UPDATE table SET col1 = val1, col2 = val2 WHERE expr`
- 理解 `parseSet()` 如何解析 SET 子句

#### 3. DELETE 解析（15 分钟）
**阅读**：`com.alibaba.druid.sql.parser.SQLStatementParser#parseDelete()`
- 看如何解析：`DELETE FROM table WHERE expr`

#### 4. 对比总结（30 分钟）
**任务**：在笔记中对比三种语句的解析差异

### ✅ 今日任务清单

- [ ] **任务 1**：扩展 `SimpleSelectParser`，支持 INSERT/UPDATE/DELETE
  - INSERT：`INSERT INTO table (col1, col2) VALUES (val1, val2)`
  - UPDATE：`UPDATE table SET col1 = val1 WHERE expr`
  - DELETE：`DELETE FROM table WHERE expr`
- [ ] **任务 2**：在笔记中整理语句解析对比表
- [ ] **任务 3**：回答以下问题：
  - INSERT 的 VALUES 子句如何解析多行数据？
  - UPDATE 的 SET 子句如何解析多个赋值？

### 🎤 面试准备检查点

**能回答**：
- ✅ "INSERT 语句如何解析 VALUES？"（`parseValues()` 解析多行，每行是表达式列表）
- ✅ "UPDATE 的 SET 子句如何解析？"（解析 `col = val` 对，组装成 Map）

---

## 📅 Day 6：方言差异与扩展机制

### 🎯 今日目标
- 理解 MySQL vs Oracle 的解析差异
- 掌握如何扩展新语法
- 能回答“如何支持新数据库方言”

### 📖 阅读任务（2 小时）

#### 1. 方言对比（1 小时）
**阅读**：
- `MySqlStatementParser` vs `OracleStatementParser`
- `MySqlExprParser` vs `OracleExprParser`
- 找出至少 3 个差异点（如 LIMIT vs ROWNUM、字符串连接符）

#### 2. 扩展机制（1 小时）
**阅读**：
- `SQLParserFeature`：Feature 开关如何控制解析行为
- 看一个实际扩展案例：MySQL 的 `/*+ HINT */` 语法如何实现

### ✅ 今日任务清单

- [ ] **任务 1**：在笔记中整理方言差异表
  ```
  特性 | MySQL | Oracle
  LIMIT | LIMIT n | ROWNUM <= n
  字符串连接 | CONCAT() | ||
  分页 | LIMIT offset, count | ROWNUM + 子查询
  ```
- [ ] **任务 2**：设计一个“如何支持 PostgreSQL 方言”的方案
  - 列出需要实现的类
  - 列出需要扩展的语法点
- [ ] **任务 3**：回答以下问题：
  - 如果要添加一个新关键字（如 `FETCH`），需要改哪些地方？
  - 如果要支持新的 Hint 语法，需要改哪些地方？

### 🎤 面试准备检查点

**能回答**：
- ✅ "MySQL 和 Oracle 的 Parser 有什么区别？"（继承不同基类，重写特定方法）
- ✅ "如何扩展 Druid 支持新语法？"（Lexer 加 Token → Parser 加分支 → AST 扩展节点）
- ✅ "如果要支持 PostgreSQL，需要实现哪些类？"（`PostgreSqlStatementParser`、`PostgreSqlExprParser`、`PostgreSqlLexer`）

---

## 📅 Day 7：综合实战与面试准备

### 🎯 今日目标
- 整合前 6 天的知识
- 完成一个完整的极简解析器
- 准备面试常见问题

### 📖 复习任务（1 小时）

#### 1. 知识梳理（30 分钟）
- 回顾前 6 天的笔记
- 画出完整的“SQL → Token → AST”流程图
- 整理核心类和方法清单

#### 2. 代码整合（30 分钟）
- 整合 `SimpleLexer` + `SimpleExprParser` + `SimpleSelectParser`
- 确保能完整解析：`SELECT id, name FROM users WHERE id = 1 ORDER BY id LIMIT 10`

### ✅ 今日任务清单

- [ ] **任务 1**：完成极简解析器项目
  - 包含：`SimpleLexer`、`SimpleExprParser`、`SimpleSelectParser`
  - 能解析完整的 SELECT 语句
  - 输出简单 AST（可用 JSON 或 toString 表示）
- [ ] **任务 2**：写一份“学习总结.md”
  - 包含：核心概念、关键类、调用流程、扩展方法
  - 用于面试前快速复习
- [ ] **任务 3**：模拟面试问答
  - 自己提问，自己回答（录音或写下来）
  - 确保能流畅讲解 Lexer/Parser 的区别、递归下降原理、扩展方法

### 🎤 面试准备检查点

**必须能回答的问题**：
1. ✅ "解释一下 Lexer 和 Parser 的区别"
2. ✅ "递归下降解析是怎么工作的？"
3. ✅ "Druid 如何解析 `SELECT id FROM users WHERE id = 1`？"
4. ✅ "如果要扩展新语法，怎么做？"
5. ✅ "MySQL 和 Oracle 的 Parser 有什么区别？"
6. ✅ "你实现过解析器吗？能讲讲你的实现思路吗？"

---

## 📅 Day 8-10：深入细节（可选，按需）

### Day 8：复杂语法（JOIN、子查询、CTE）
- 阅读 `parseTableSource()` 的 JOIN 解析
- 阅读子查询解析（`parseSelect()` 递归调用）
- 阅读 CTE 解析（WITH 子句）

### Day 9：性能优化与边界处理
- 阅读 Druid 的缓存机制
- 阅读错误处理（语法错误如何抛出）
- 阅读 SQL 注入防护（WallFilter 相关）

### Day 10：实战项目整合
- 将 Druid 知识应用到你的 JSON2SQL 项目
- 思考如何优化现有转换逻辑
- 准备面试项目介绍

---

## 📝 每日学习模板

### 每日学习记录格式

```markdown
## Day X 学习记录

### 今日完成
- [ ] 阅读任务
- [ ] 调试实践
- [ ] 代码实现
- [ ] 笔记整理

### 关键理解
1. [核心概念 1]
2. [核心概念 2]

### 遇到的问题
- [问题 1]：解决方案
- [问题 2]：解决方案

### 明日计划
- [ ] 任务 1
- [ ] 任务 2
```

---

## 🎯 面试冲刺检查清单

### 第 1 周结束前必须掌握
- [ ] 能画出“SQL → Lexer → Parser → AST”的完整流程图
- [ ] 能解释递归下降解析的原理（用自己实现的代码举例）
- [ ] 能对比 Druid 的实现和自己的实现
- [ ] 能回答“如何扩展新语法”
- [ ] 能解释 MySQL 和 Oracle 的差异

### 面试前 1 天
- [ ] 复习所有笔记
- [ ] 重新运行一遍自己的极简解析器
- [ ] 模拟面试问答（至少 10 个问题）
- [ ] 准备项目介绍（如何将 Druid 知识应用到 JSON2SQL 项目）

---

## 💡 学习技巧

### 高效阅读源码
1. **先看方法签名和注释**，再看实现细节
2. **用调试器单步跟踪**，比纯读代码快 10 倍
3. **画图辅助理解**（类关系图、调用栈图、AST 结构图）

### 记忆技巧
1. **对比记忆**：自己的实现 vs Druid 的实现
2. **场景记忆**：用具体 SQL 例子理解抽象概念
3. **输出记忆**：写笔记、画图、讲给别人听

### 面试技巧
1. **先说原理，再说实现**：先讲“递归下降是什么”，再说“Druid 怎么做的”
2. **用例子说话**：用 `1 + 2 * 3` 解释优先级处理
3. **对比突出理解**：说“我实现过简单版，也看过 Druid 的实现，它用了 X 方式处理 Y 问题”

---

## 📚 参考资源

### 官方文档
- Druid GitHub: https://github.com/alibaba/druid
- Druid Wiki: https://github.com/alibaba/druid/wiki

### 学习资料
- 《编译原理》（龙书）：第 2-4 章（Lexer/Parser 基础）
- 《ANTLR 4 权威指南》：理解 Parser 生成器（可选）

### 面试题库
- 准备 20+ 个常见问题（见每日“面试准备检查点”）

---

