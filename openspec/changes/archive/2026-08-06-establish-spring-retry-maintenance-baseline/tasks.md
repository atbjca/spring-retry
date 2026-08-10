## 1. 实施前门禁与 OpenSpec 项目规则

- [x] 1.1 检查并记录当前 Git 分支、tracked/untracked 变更和可恢复点，确认仅修改 `openspec/config.yaml`、`README.md`、`SECURITY.md` 与 `doc/`，保留所有无关工作树内容且不触及 `pom.xml`、`src/`、测试源码、Nexus 或下游仓库
- [x] 1.2 更新 `openspec/config.yaml` 的项目上下文，固化 Java 8、Spring Framework 5.3.x、Maven、目标 NES GAV、`org.springframework.retry.*` package 保持和当前仓库边界
- [x] 1.3 更新 `openspec/config.yaml` 的工件规则，强制完整 OpenSpec、明确审批、Impact Analysis、安全设计、Java 代码 TDD、60% 覆盖率、中文注释优先、文档同步、六种 CVE 状态和归档门禁

## 2. 维护治理与安全入口

- [x] 2.1 创建 `doc/REQUIREMENTS.md`，记录人机协作、变更确认、分支安全、影响分析、安全设计、TDD、覆盖率、依赖限制、文档交付和 OpenSpec 生命周期要求
- [x] 2.2 创建根目录 `SECURITY.md`，说明支持范围、私密漏洞报告入口、响应流程、漏洞文档索引、敏感信息保护以及 GAV 重品牌不等于漏洞修复

## 3. GAV、兼容性与使用文档

- [x] 3.1 创建 `doc/GAV_MAPPING.md`，分别记录当前坐标、目标 SNAPSHOT/RELEASE 坐标、Spring Framework 官方/NES 直接依赖映射、scope、迁移方式及尚未实施/发布的状态
- [x] 3.2 创建 `doc/COMPATIBILITY.md`，区分当前构建声明与 Java 8/Spring Framework 5.3.x 目标支持矩阵，明确 Spring 4.x 和 Java 6/7 不在承诺范围，并说明官方与 NES Framework 隔离验证规则
- [x] 3.3 创建 `doc/TESTING.md`，记录 JDK/Maven 选择、定向与全量测试、RED/GREEN/REFACTOR 证据、JaCoCo 报告、失败处理和产物位置，并将本次纯文档/声明式配置变更的代码覆盖率标记为不适用
- [x] 3.4 创建 `doc/QUICK_START.md`，提供 Maven 与 Gradle 消费坐标、Nexus 前置条件、Java 8/Spring 5.3 要求、最小 `@EnableRetry` 示例和依赖树去重命令，同时明确目标坐标尚不可作为已发布生产版本使用
- [x] 3.5 创建 `doc/USER_MANUAL.md`，覆盖声明式/命令式、无状态/有状态重试、`RetryListener`、断路器、`RetryContextCache`、当前容量行为、缓存配置和 GAV 迁移，并将尚未实现的修复语义明确标记为目标状态

## 4. CVE 评估与漏洞文档基线

- [x] 4.1 按实施日期复核 Spring 官方信息、GitHub Reviewed Advisory、NVD/OSV、上游 issue/commit 和当前分支源码，记录 CVE-2026-41710 的标识符、版本范围、CVSS/CWE、攻击路径、源码证据及信息差异，不修改漏洞代码
- [x] 4.2 创建 `doc/CVE/CVE-2026-41710.md`，记录适用性、影响分析、当前源码、上游修复、拟议处置、待完成测试/制品证据、残余风险、参考链接和状态历史，主状态保持“修复中”并注明实际阶段
- [x] 4.3 创建 `doc/VULNERABILITY_REPORT.md`，包含更新时间、分支/commit、当前与目标 GAV、平台基线、六状态词典、CVE 表、状态统计、下一步和单篇文档链接，确保统计与单篇状态一致

## 5. 发布文档与项目导航

- [x] 5.1 创建 `doc/RELEASE_GUIDE.md`，记录通用版本策略、本地门禁、effective POM/依赖/JAR/消费者检查、Nexus 单独授权、目标不存在检查、远端复验、Git tag 和不可变失败处理，且不写入真实凭证
- [x] 5.2 创建 `doc/RELEASE_NOTES.md`，以未发布状态记录本维护线的目标 GAV、兼容性变化、已知 CVE 状态和后续升级步骤，不虚构 RELEASE、commit、tag 或 Nexus 结果
- [x] 5.3 更新 `README.md`，提供项目用途、维护分支、当前与目标支持矩阵、安全状态、Maven 构建入口和全部永久文档索引，避免重复 User Manual 内容

## 6. 静态验证与变更范围复核

- [x] 6.1 校验全部要求文档存在、README 与文档间链接有效、Markdown 代码块和示例结构完整，并确认文档以中文为主且未包含密码、token、私钥或私服敏感值
- [x] 6.2 校验所有 CVE 主状态仅使用“已修复、免疫、不适用、修复中、已缓解、暂缓”，总览统计与单篇文档一致，当前/目标 GAV 和当前/目标能力在各文档中无矛盾
- [x] 6.3 执行 OpenSpec 严格校验并修正所有错误，确认 capability specs、design、tasks 与永久文档交付范围一致
- [x] 6.4 检查最终 Git diff 和工作树，确认未修改 `pom.xml`、`src/`、测试源码或无关用户文件，并记录本次未运行 Maven/JaCoCo 的原因是无可执行逻辑变更
