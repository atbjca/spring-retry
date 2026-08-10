# maintenance-governance Specification

## Purpose
TBD - created by archiving change establish-spring-retry-maintenance-baseline. Update Purpose after archive.
## Requirements
### Requirement: 所有变更使用完整 OpenSpec 流程

项目 SHALL 对源码、测试、构建、依赖、GAV、文档、发布和需求变更建立独立 OpenSpec change。每个 change MUST 在实施前完成 proposal、design、capability specs 和 tasks，并在实现、验证及文档同步完成后方可归档。

#### Scenario: 新需求进入项目

- **WHEN** 用户提出任何会改变仓库或发布制品的需求
- **THEN** 项目 MUST 先进入 explore 并创建完整 OpenSpec 工件，未达到 apply-ready 前不得实施

#### Scenario: 小型配置或文档变更

- **WHEN** 变更仅涉及一项配置或一份文档
- **THEN** 项目仍 MUST 使用完整 OpenSpec 流程，不得以改动较小为由绕过

#### Scenario: 归档变更

- **WHEN** 任一 task、测试证据、覆盖率证据或文档同步尚未完成
- **THEN** change MUST 保持 active，禁止归档或将未完成任务标记为完成

### Requirement: 人工审批门禁

项目 MUST 在执行任何 change 前说明问题原因、修改建议、预期影响和风险，并获得用户明确同意。Java 源码、POM、依赖、发布或跨仓库任务 MUST 在 tasks 中标注审批点。

#### Scenario: 提案尚未获批

- **WHEN** change 工件已完成但用户尚未明确批准 apply
- **THEN** 维护者 MUST 停止在提案阶段，不得修改正式源码、配置或项目文档

#### Scenario: 源码任务即将开始

- **WHEN** 已批准 change 即将执行首个 Java 生产代码任务
- **THEN** 维护者 MUST 再次确认影响分析、分支状态和源码修改授权

### Requirement: 变更影响分析

每个 design MUST 列出受影响的上游、下游、模块、公共 API、构建元数据、运行行为、数据安全、兼容性和回滚风险；当三个及以上组件或步骤存在依赖关系时 MUST 提供流程图、架构图或等价可视化。

#### Scenario: GAV 变化影响多个消费者

- **WHEN** change 修改 Spring Retry 的 Maven 坐标
- **THEN** design MUST 明确 Spring Boot、Spring Kafka、直接消费者、Nexus 和 SCA 的影响及迁移路径

#### Scenario: 局部实现无外部影响

- **WHEN** 分析确认变更仅影响单个内部实现
- **THEN** design MUST 记录该结论及证据，而不是省略影响分析

### Requirement: 后端代码严格执行 TDD

所有 Java 生产代码变更 MUST 先建立可复现的失败测试（RED），再编写最小实现使测试通过（GREEN），最后在测试持续通过的前提下重构（REFACTOR）。

#### Scenario: 修复回归缺陷

- **WHEN** change 修复 Spring 5.3 兼容性或 CVE
- **THEN** tasks MUST 将失败测试、生产实现和重构验证拆成有序步骤，并记录 RED 与 GREEN 命令结果

#### Scenario: 纯文档变更

- **WHEN** change 不修改生产代码或可执行构建逻辑
- **THEN** 可豁免 RED/GREEN，但 MUST 执行文档链接、格式和 OpenSpec 校验

### Requirement: 测试覆盖率可审计

新增核心业务逻辑和复杂算法 MUST 达到至少 60% 的测试覆盖率，并由可重复工具生成证据。纯配置、简单数据模型或无可执行逻辑的文档变更 SHALL 将覆盖率标记为不适用，并 MUST 在 design/tasks 中说明原因及替代验证方式。

#### Scenario: 新增缓存算法

- **WHEN** change 新增或修改 LRU、缓存路由或断路器状态逻辑
- **THEN** 相关类或变更范围的覆盖率 MUST 达到 60% 及以上，并记录报告位置

#### Scenario: 覆盖率豁免

- **WHEN** 任务仅创建 Markdown 文档或声明式配置
- **THEN** tasks MUST 标记覆盖率不适用并执行对应的静态验证

### Requirement: 注释和代码风格保持一致

新增代码 MUST 遵循仓库现有命名、异常、日志、格式和 API 风格。所有新增解释性注释与 Javadoc MUST 优先使用中文，字段名、协议名、专业术语和代码关键字可保留英文。

#### Scenario: Backport 上游英文注释

- **WHEN** 上游补丁包含需要保留的解释性英文注释
- **THEN** 实施者 MUST 在不改变专业含义的前提下优先转换为中文，并保持许可证头和标准标签正确

### Requirement: 大范围或破坏性操作前检查仓库安全

实施大范围、跨模块、发布、删除、覆盖或历史修改操作前，维护者 MUST 检查当前分支、tracked/untracked 变化、active OpenSpec 和备份条件，并向用户提示确认。

#### Scenario: 工作树存在无关修改

- **WHEN** 检查发现与当前 change 无关的 tracked 修改
- **THEN** 实施者 MUST 保留这些修改并停止可能覆盖它们的操作，直至获得用户指示

#### Scenario: 发布或批量重品牌即将开始

- **WHEN** change 即将批量修改 GAV 或发布 RELEASE
- **THEN** 实施者 MUST 再次报告分支和工作树状态，并确认可恢复点

### Requirement: 第三方依赖和安全设计前置审批

项目 MUST 在设计阶段识别资源耗尽、注入、越权、路径处理、反序列化和依赖供应链风险。未经明确讨论与批准，不得引入项目此前未使用的运行时第三方库、框架或设计模式。

#### Scenario: 提议引入新运行时库

- **WHEN** 实现方案依赖新的运行时第三方库
- **THEN** design MUST 比较无新依赖方案并获得用户明确批准后才能加入 tasks

#### Scenario: 引入覆盖率构建插件

- **WHEN** change 使用 JaCoCo 证明覆盖率
- **THEN** design MUST 将其标记为仅构建期工具，并验证它不会进入运行时依赖树

### Requirement: JaCoCo 覆盖率基线必须在 Java 8 上可重复

项目 SHALL 固定使用 `jacoco-maven-plugin:0.8.15` 作为仅构建期工具，并 MUST 在真实 JDK 8 的 `verify` 生命周期中准备 agent、生成 HTML/XML 报告和执行覆盖率检查。JaCoCo MUST NOT 作为项目运行时依赖发布。

#### Scenario: 执行默认 verify

- **WHEN** 维护者在真实 JDK 8 上执行默认 NES `mvn clean verify`
- **THEN** 构建 MUST 生成 `target/site/jacoco/index.html` 和 `target/site/jacoco/jacoco.xml`，且 JaCoCo check MUST 成功

#### Scenario: 检查本次修改类的覆盖率

- **WHEN** JaCoCo 分析 `org.springframework.retry.annotation.RetryConfiguration`
- **THEN** 该类的行覆盖率 MUST 达到 60% 及以上，并且新增 singleton 生命周期路径 MUST 由回归测试实际执行

#### Scenario: 覆盖率低于门禁

- **WHEN** `RetryConfiguration` 行覆盖率低于 60% 或新增生命周期分支未被测试执行
- **THEN** `verify` MUST 失败，维护者 MUST 增加有意义的测试，MUST NOT 降低阈值、排除目标类或删除关键断言

#### Scenario: 检查运行时 dependency tree

- **WHEN** 生成 compile/runtime dependency tree 或检查发布 POM
- **THEN** `org.jacoco` 制品 MUST NOT 作为项目 dependency 出现，agent 与报告库 MUST 仅存在于 Maven 插件执行环境

#### Scenario: 记录遗留项目覆盖率

- **WHEN** 本 change 首次生成全项目 JaCoCo 报告
- **THEN** 维护者 MUST 记录全项目覆盖率作为后续基线，但 MUST NOT 在未分析遗留测试结构前擅自声明新的全局阈值
