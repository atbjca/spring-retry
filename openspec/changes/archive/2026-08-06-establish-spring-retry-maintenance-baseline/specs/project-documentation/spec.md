## ADDED Requirements

### Requirement: 项目维护完整的永久文档集

项目 MUST 至少维护以下文档：`README.md`、`SECURITY.md`、`doc/REQUIREMENTS.md`、`doc/GAV_MAPPING.md`、`doc/QUICK_START.md`、`doc/USER_MANUAL.md`、`doc/COMPATIBILITY.md`、`doc/TESTING.md`、`doc/VULNERABILITY_REPORT.md`、`doc/CVE/` 单篇文档、`doc/RELEASE_GUIDE.md` 和 `doc/RELEASE_NOTES.md`。

#### Scenario: 维护基线首次实施

- **WHEN** 本 change 被 apply
- **THEN** 上述文档 MUST 被创建或更新，并从 README 提供有效入口

#### Scenario: 文档暂时描述未来能力

- **WHEN** GAV、兼容性修复或 RELEASE 尚未实施
- **THEN** 文档 MUST 明确标识“当前状态”和“目标状态”，不能提供尚不可用坐标作为正式用法

### Requirement: README 作为轻量入口而非重复手册

README MUST 说明项目用途、维护分支、支持矩阵摘要、当前安全状态、构建入口和完整文档索引，不得复制 User Manual 的全部内容。

#### Scenario: 用户首次进入仓库

- **WHEN** 用户只阅读 README
- **THEN** 用户 MUST 能找到 Quick Start、User Manual、GAV、Compatibility、Testing、Vulnerability Report 和 Release Guide

### Requirement: Quick Start 提供可复制的接入步骤

`doc/QUICK_START.md` MUST 提供 Nexus 前置条件、Maven 坐标、Gradle 坐标、Java 8/Spring 5.3 要求、最小 `@EnableRetry` 示例和依赖树验证命令。示例中的版本 MUST 与已发布状态一致。

#### Scenario: RELEASE 尚未发布

- **WHEN** 文档生成时只有目标 GAV 或开发 SNAPSHOT
- **THEN** Quick Start MUST 明确标记不可用于生产，并区分计划 RELEASE 与当前可用制品

#### Scenario: Maven 用户接入

- **WHEN** 用户按 Quick Start 添加 NES Spring Retry
- **THEN** 示例 MUST 说明如何排除或替换官方 Spring Retry，避免双份 classpath

### Requirement: User Manual 覆盖关键行为和迁移

`doc/USER_MANUAL.md` MUST 覆盖声明式/命令式重试、无状态/有状态重试、RetryListener、断路器、RetryContextCache、容量策略、缓存 Bean 命名、异常和从官方 GAV 迁移的注意事项。

#### Scenario: CVE 缓存修复改变默认行为

- **WHEN** 后续 remediation 修改缓存驱逐或隔离语义
- **THEN** User Manual MUST 在同一 change 中更新默认值、配置示例、权衡和回滚说明

### Requirement: Requirements 与 AI 协作规则可审计

`doc/REQUIREMENTS.md` MUST 固化 OpenSpec、审批、中文注释、TDD、覆盖率、上下文理解、影响分析、安全设计、文档同步、依赖限制和分支安全要求。

#### Scenario: 协作规则发生变化

- **WHEN** 用户新增或修改开发规范
- **THEN** 项目 MUST 创建 OpenSpec change 更新 Requirements 和相关 capability spec

### Requirement: Compatibility 与 Testing 文档提供可重复证据

Compatibility MUST 列出 Java/Spring/Maven 支持矩阵和非支持范围；Testing MUST 列出环境选择、定向/全量命令、TDD 证据、JaCoCo 报告、失败处理和测试产物位置。

#### Scenario: 测试矩阵发生变化

- **WHEN** 项目增加 JDK 或 Spring 版本验证
- **THEN** Compatibility、Testing 和 Release Notes MUST 同步更新

### Requirement: Release 文档区分流程与版本事实

Release Guide MUST 记录通用版本、Nexus、校验、回滚和不可变发布流程；Release Notes MUST 只记录某个具体版本的变化、兼容性影响、CVE、GAV 和升级步骤。

#### Scenario: 发布新 NES patch

- **WHEN** 新 RELEASE 完成远端验证
- **THEN** Release Notes MUST 记录版本、commit、tag、制品坐标、已修复 CVE 和消费者迁移注意事项

### Requirement: SECURITY 文档提供安全维护入口

根目录 `SECURITY.md` MUST 说明支持版本、漏洞报告渠道、响应流程、状态文档入口以及 GAV 重品牌不等于漏洞修复。

#### Scenario: 外部人员报告漏洞

- **WHEN** 维护者收到潜在安全问题
- **THEN** SECURITY MUST 引导其使用批准的私密渠道，并禁止在文档中暴露凭证或未公开漏洞细节

### Requirement: 文档优先使用中文并保护敏感信息

永久文档 MUST 以中文为主，GAV、类名、命令和专业术语可保留英文。文档和示例 MUST NOT 包含真实密码、token、私钥或可复用凭证。

#### Scenario: 展示 Nexus 配置

- **WHEN** Quick Start 或 Release Guide 展示 Maven settings
- **THEN** 示例 MUST 使用占位符或 server id，不得写入真实认证信息
