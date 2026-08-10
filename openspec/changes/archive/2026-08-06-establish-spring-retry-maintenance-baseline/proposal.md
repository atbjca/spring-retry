## Why

当前仓库缺少可审计的维护治理、GAV 规则、漏洞状态规范和交付文档，同时构建元数据仍声明 Java 1.6 与 Spring Framework 4.3，无法准确表达已确认的 Java 8、Spring Framework 5.3.x 维护目标。必须先建立统一的 OpenSpec 与文档基线，再开展兼容性修复、CVE backport、GAV 切换和发布工作。

## What Changes

- 建立强制 OpenSpec 生命周期：探索、完整提案、影响分析、明确审批、TDD 实施、验证、文档同步和归档；任何源码、构建、文档或发布变更均不得绕过该流程。
- 固化人工审批、Git 分支安全检查、中文注释优先、安全设计、测试覆盖率和禁止擅自引入第三方依赖等维护门禁。
- **BREAKING（支持策略）**：目标支持矩阵收敛为 Java 8 与 Spring Framework 5.3.x，不再承诺 Java 6/7 或 Spring Framework 4.x 兼容性。
- 定义 NES GAV 目标：`cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`，开发版本追加 `-SNAPSHOT`；Java package 与 import 保持 `org.springframework.retry.*`。
- 定义 Spring Framework 依赖 GAV 的全链路 NES 映射、官方 5.3.39 隔离兼容验证以及禁止官方/NES 重复 classpath 的规则。本 change 仅建立约束和映射，实际 POM 切换由后续独立 change 实施。
- 建立 CVE 评估、修复和文档规范，统一使用“已修复、免疫、不适用、修复中、已缓解、暂缓”六种状态，并要求状态、源码、测试与制品证据一致。
- 建立永久项目文档清单，包括 `README.md`、`SECURITY.md`、`doc/REQUIREMENTS.md`、`doc/GAV_MAPPING.md`、`doc/QUICK_START.md`、`doc/USER_MANUAL.md`、`doc/COMPATIBILITY.md`、`doc/TESTING.md`、`doc/VULNERABILITY_REPORT.md`、单 CVE 文档、发布指南和发布说明。
- 建立 Maven、覆盖率、本地制品、Nexus RELEASE、下游消费者与不可变发布的验证门禁。
- 本 change 不修改 Java 源码、POM 实际 GAV、生产行为、Nexus 制品或下游仓库。

## Capabilities

### New Capabilities

- `maintenance-governance`: OpenSpec 强制流程、人工审批、TDD、影响分析、分支安全和协作规范。
- `platform-compatibility`: Java 8、Spring Framework 5.3.x、Maven 构建工具及兼容性验证要求。
- `gav-rebranding`: Spring Retry 与 Spring Framework NES GAV 映射、package 保持和依赖闭环规则。
- `cve-assessment`: CVE 信息来源、适用性、攻击路径、优先级和证据化评估要求。
- `cve-documentation`: 漏洞总览、单 CVE 文档、六状态词典和状态同步要求。
- `cve-remediation`: 审批后 TDD backport、最小差异、安全验证和残余风险要求。
- `project-documentation`: Quick Start、User Manual、测试、兼容性、GAV、发布及安全文档要求。
- `component-release`: Maven 制品、Nexus、不可变版本、消费者验证和下游 OpenSpec 联动要求。

### Modified Capabilities

无。当前项目尚无已建立的 OpenSpec capability。

## Impact

- 本 change apply 时仅影响 `openspec/`、`README.md`、`SECURITY.md` 和 `doc/` 文档目录，不触及 `src/`、`pom.xml` 或发布仓库。
- 后续独立 change 将依据本基线修改 Maven GAV、Spring Framework 依赖、Java 版本、监听器初始化逻辑和 CVE 缓存实现。
- Spring Boot 2.7、Spring Kafka 2.9 和直接业务消费者需要在各自仓库建立独立 OpenSpec change 后才能采用 NES Spring Retry 坐标。
- GAV 重品牌只能降低已修复 fork 被按官方坐标误报的概率，不能替代 CVE 修复、测试或制品证据。
