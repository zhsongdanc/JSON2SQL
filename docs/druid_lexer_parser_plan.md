# Druid 语法解析主干学习计划（Lexer + Parser）

> 目标：7-10 天掌握 Druid 从 SQL 字符串到 AST 的核心流程，聚焦 Lexer、ExprParser、StatementParser，形成可扩展的实战能力。

---

## Day 1：入口分发与整体流程
- **阅读顺序**：`SQLUtils` → `SQLParserUtils`
- **任务**：
  1. 跟踪 `SQLUtils.parseSingleStatement / parseStatements`，弄清如何依据 `DbType` 选择具体 Parser。
  2. 查看 `SQLParserUtils.createSQLStatementParser`，确认 MySQL/Oracle 等方言 Parser 的工厂逻辑。
  3. 画出“SQL 字符串 → Parser 实例 → AST”的流程图。
- **调试建议**：用简单 SQL（SELECT/INSERT），设置断点查看 Parser 创建与调用栈。

## Day 2：基础 Lexer 机制
- **核心类**：`Lexer`、`SQLLexer`、`MySqlLexer`、`OracleLexer`
- **任务**：
  1. 阅读 `Lexer` 字段（`ch`、`token`、`pos`）和 `nextToken()` 主流程。
  2. 梳理 `SQLLexer` 如何识别关键字、数字、字符串、注释。
  3. 对照 `MySqlLexer` 的扩展（反引号、hint、双问号等），记录与基础 Lexer 的差异点。
- **实践**：调试 `nextToken()`，用包含字符串、注释、参数的 SQL，观察 token 生成顺序。
- **笔记**：整理 Token 类型表（Identifier/Literal/Variant/Hint/...），写下特殊分支的处理逻辑。

## Day 3-4：表达式 Parser（SQLExprParser）
- **核心类**：`SQLExprParser` 及其方言子类 `MySqlExprParser`、`OracleExprParser`
- **任务拆解**：
  1. 阅读 `expr()`、`primary()`、`binaryOpRest()`，理解递归下降的整体骨架。
  2. 按语法片段分析对应方法：`parseIdentifier()`、`methodRest()`、`parseSelect()`、`parseCase()`、`parseInterval()` 等。
  3. 关注如何生成常见 AST 节点（函数、CASE、子查询、参数、JSON 特性）。
- **实践**：对多个复杂表达式（函数嵌套、CASE WHEN、IN、BETWEEN）设置断点，看调用栈如何逐层构建节点。
- **输出**：编写一张“语法片段 → 入口方法 → AST 节点”对照表，帮助记忆。

## Day 5-6：语句级 Parser（SQLStatementParser）
- **核心类**：`SQLStatementParser` 基类，方言实现（`MySqlStatementParser`、`OracleStatementParser`）
- **学习步骤**：
  1. 从基类入手：`parseStatementList()`、`parseSelect()`、`parseInsert()`、`parseUpdate()`、`parseDelete()`，梳理每个语句的解析顺序（WITH → QueryBlock → ORDER/LIMIT）。
  2. 阅读方言重写点：`parseStatementListDialect()`、MySQL 的 `parseTableSourceRest()`、Oracle 的 `parseMerge()` 等。
  3. 选一条包含 CTE、JOIN、UNION、LIMIT 的 SQL，单步调试 `parseSelect()`，观察 `SQLSelectStatement`、`SQLSelectQueryBlock`、`SQLTableSource` 的构建过程。
- **记录**：整理常见节点间的组合关系（例如 QueryBlock 内 selectList/from/where/group/having/order/limit）。

## Day 7：扩展与自定义
- **阅读**：`SQLParserFeature`、`SQLStatementParser.parseStatementList(List, int)`，理解 Feature 位与自定义特性开关。
- **实战**：
  1. 在自己的 demo 中直接实例化 `MySqlStatementParser`，尝试解析并打印 AST。
  2. 通过继承 `MySqlExprParser`，增加一个自定义关键字或语法分支（例如新函数或 hint），观察所需改动：Lexer 增 token、Parser 增分支、AST 扩展字段。
  3. 编写 mini Visitor 验证自定义语法是否生效（可借助 `SQLUtils.toSQLString` 做 round-trip）。
- **输出**：形成“新增语法 Checklist”，列出：Lexer 处理 → Parser 分支 → AST 节点 → Visitor/序列化，方便未来快速扩展。

## 辅助策略
- **调试习惯**：统一在 `Lexer.nextToken()`、`SQLExprParser.expr()`、`SQLStatementParser.parseSelect()` 设置断点，用不同 SQL 输入反复验证。
- **笔记模板**：
  - 类关系图：Lexer ↔ ExprParser ↔ StatementParser ↔ AST。
  - 方法卡片：记录每个关键方法的“输入/输出/依赖/易错点”。
  - 异常归纳：总结语法错误或不支持语法的抛错路径，方便排障。
- **资料补充**：关注 GitHub issues/PR 中与 Parser 相关的讨论，学习社区是如何修复语法问题、引入新特性的。

通过该计划，你将形成对 Druid 语法解析主干的系统理解，并掌握调试、扩展、实战的具体方法，可直接为 JSON/SQL 转换等场景提供支持。


