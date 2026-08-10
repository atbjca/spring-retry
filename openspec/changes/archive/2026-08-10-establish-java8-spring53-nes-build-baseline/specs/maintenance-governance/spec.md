## ADDED Requirements

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
