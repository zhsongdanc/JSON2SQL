# Druid Lexer/Parser 实战引导

> 目标：像老师带读源码一样，结合实际类与调试建议，帮助你在最短时间内理解“Lexer 逐字符识别 token、Parser 递归构建 AST”的链路，并能亲自改造或扩展。

---

## 学前提示
- **背景知识够用**：掌握 Java、基础 SQL 语法即可；有限状态机、LL 解析只需概念层理解。
- **环境准备**：拉取 `druid` 仓库，导入 IDE（推荐 IntelliJ），保证能运行单元测试 `DruidTest`/`WallTest`。
- **学习方式**：每一节先“概念扫描”，再“源码定位”，最后“调试/练习”。

---

## 第 1 课：总览 —— SQL 字符串如何进入解析流程
1. **概念扫描**  
   - Druid 入口几乎都在 `SQLUtils`：统一封装 `parseSingleStatement`、`parseStatements`。
   - Parser 的选择依赖 `DbType`，通过 `SQLParserUtils` 工厂创建。
2. **源码定位**  
   - `com.alibaba.druid.sql.SQLUtils#parseSingleStatement`  
   - `com.alibaba.druid.sql.parser.SQLParserUtils#createSQLStatementParser`
3. **实作**  
   - 在 IDE 中打开 `SQLUtils.parseSingleStatement`，添加断点。  
   - 运行：`SQLUtils.parseSingleStatement("select * from users", JdbcConstants.MYSQL);`  
   - 单步查看：参数进入 `SQLParserUtils.createSQLStatementParser` → 返回 `MySqlStatementParser`。  
4. **思考问题**  
   - 为什么工厂要区分 `SQLExprParser` 与 `SQLStatementParser`？  
   - 如果传入 `oracle`，实例化链路有何差异？

---

## 第 2 课：Lexer —— 逐字符生成 token
1. **概念扫描**  
   - Lexer 本质：维护游标 `ch`，在 `nextToken()` 中根据字符类别切换状态，输出 `Token`。  
   - MySQL/Oracle 通过子类扩展关键字、Hint、转义规则。
2. **源码定位**  
   - `com.alibaba.druid.sql.parser.Lexer`：基础状态、`nextToken()` 主循环。  
   - `com.alibaba.druid.sql.parser.SQLLexer`: 识别数字、字符串、注释、变量。  
   - `com.alibaba.druid.sql.dialect.mysql.parser.MySqlLexer`: 处理反引号、`/*! hint */` 等。
3. **手把手练习**  
   - 创建测试：  
     ```java
     Lexer lexer = new MySqlLexer("SELECT /*+ HINT */ name FROM `user` WHERE id = :id");
     lexer.nextToken(); // 反复调用，观察 token 变化
     ```  
   - 在 `Lexer.nextToken()` 和 `scanIdentifier()` 设断点，观察：  
     - 如何识别 `SELECT`（关键字 → `Token.SELECT`）。  
     - 如何处理反引号包裹的标识符。  
     - 遇到 `:id` 进入 `scanVariable()`。
4. **教师笔记**  
   - 记录一个表：字符模式 → 调用方法 → 产生 Token。  
   - 特别留意 `mark()`/`reset()`、`subString()` 等用于回退的细节。

---

## 第 3 课：表达式 Parser —— 递归下降骨架
1. **概念扫描**  
   - `SQLExprParser` 负责解析表达式、子查询、函数等。  
   - 核心套路：`expr()` 处理优先级，`primary()` 构建基本节点，再由 `binaryOpRest()` 拼二元表达式。
2. **源码定位**  
   - `com.alibaba.druid.sql.parser.SQLExprParser#expr` / `primary` / `binaryOpRest`.  
   - 方言扩展：`MySqlExprParser`（JSON 函数、LIMIT 子句）等。
3. **练习步骤**  
   - 输入 SQL：`SELECT CASE WHEN status = 'ACTIVE' THEN upper(name) ELSE name END FROM users`  
   - 在 `MySqlExprParser.primary()` 设置断点，关注：  
     - `CASE` 如何进入 `parseCase()`。  
     - `upper(name)` 如何生成 `SQLMethodInvokeExpr`。  
     - `status = 'ACTIVE'` 如何递归调用 `binaryOpRest()`。  
   - 记录 AST：`SQLCaseExpr`、`SQLBinaryOpExpr`、`SQLCharExpr` 等节点字段。
4. **延伸**  
   - 尝试修改 `MySqlExprParser.methodRest()`，为某函数增加自定义语法（例如允许 `MYFUNC(arg1 := value)`），观察所需条件。

---

## 第 4 课：语句 Parser —— 组装 SELECT/INSERT/UPDATE/DELETE
1. **概念扫描**  
   - `SQLStatementParser` 以语句为单位解析，构建 `SQLSelectStatement`、`SQLInsertStatement` 等。  
   - 解析顺序一般是：WITH → QueryBlock → UNION → ORDER/LIMIT。
2. **源码定位**  
   - `com.alibaba.druid.sql.parser.SQLStatementParser#parseStatementList`  
   - `#parseSelect`、`#parseInsert`、`#parseUpdate`、`#parseDelete`  
   - 方言重写：`MySqlStatementParser#parseStatementListDialect`、`OracleStatementParser#parseMerge`
3. **课堂演示**  
   - 解析 SQL：  
     ```
     WITH recent AS (
       SELECT user_id, count(*) cnt FROM orders GROUP BY user_id
     )
     SELECT u.id, recent.cnt FROM users u LEFT JOIN recent ON u.id = recent.user_id
     ORDER BY recent.cnt DESC LIMIT 10
     ```  
   - 跟随 `parseSelect()` 调试：  
     1. `parseWith()` 构建 `SQLWithSubqueryClause`.  
     2. `query()` 进入 `SQLSelectQueryBlock`。  
     3. `parseTableSource()` 识别 JOIN。  
     4. `parseOrderBy()`、`parseLimit()` 收尾。  
   - 将生成的 AST 用 `SQLUtils.toSQLString(stmt)` 打印，验证 round-trip。
4. **作业**  
   - 选择一条包含 INSERT 多值、UPDATE 子查询、DELETE EXISTS 的 SQL，分别单步跟踪 `parseInsert` 等方法，记录关键节点。

---

## 第 5 课：扩展与实战
1. **Feature 控制**  
   - 阅读 `SQLParserFeature`，了解如何启用 `KeepComments`、`UseInsertColumnsCache` 等特性。  
   - 实验：传入 `EnumSet.of(SQLParserFeature.KeepComments)`，观察注释是否保留。
2. **自定义语法案例**  
   - 需求：所有 SELECT 自动追加租户过滤。  
   - 步骤：  
     1. 复写 `MySqlStatementParser.parseSelect()`，在 `where` 为空时插入 `tenant_id = ?`.  
     2. 或通过 Visitor（`MySqlASTVisitorAdapter`）遍历 AST 并改写。  
     3. 输出 SQL，确认语法合法。  
   - 总结扩展 checklist：Lexer（新关键字）→ ExprParser（语法规则）→ StatementParser（落地语句）→ Visitor/序列化。

---

## 学习节奏建议
- **每日回顾**：写下当日阅读的类、关键方法、遇到的问题；用自己的话复述解析流程。  
- **定期实战**：至少完成一次“解析 SQL → 改 AST → 输出 SQL”的练习。  
- **查阅社区**：关注 GitHub issue 中与 Parser 相关的 bugfix/PR，学习他人如何定位与修复语法问题。

坚持按照以上路线，你将真正理解 Druid Lexer/Parser 的内部机制，具备调试、扩展和在 JSON/SQL 项目中灵活运用的能力。祝学习顺利！


