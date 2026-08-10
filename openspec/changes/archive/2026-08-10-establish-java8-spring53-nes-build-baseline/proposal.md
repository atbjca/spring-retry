## Why

当前 POM 仍使用官方 `org.springframework.retry:spring-retry:1.3.5-SNAPSHOT`、Java 1.6 和 Spring Framework 4.3.29，既不符合已批准的 NES GAV 与 Java 8/Spring 5.3.x 合同，也会在 Spring Framework 5.3.39 下因全局 `RetryListener` 发现时机过早产生 1 个测试失败和 1 个测试错误。必须先建立可重复的构建、兼容性和覆盖率基线，后续 CVE 修复与 RELEASE 才有可信的验证基础。

## What Changes

- **BREAKING（构建坐标）**：将开发制品切换为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`；Java package、import 和公共 API 身份保持不变。
- **BREAKING（支持基线）**：将最低编译与运行基线从 POM 当前的 Java 1.6 提升到 Java 8，并只承诺 Spring Framework 5.3.x；Spring 4.x、Java 6/7 不再作为发布门禁。
- 将默认 Spring Framework BOM、context、core、test、tx 切换到 NES `5.3.39-nes.patch.1`，并用统一的 Maven 属性允许命令行隔离切换到官方 `5.3.39` 兼容性验证。
- 严格按 TDD 增加 `RetryListener` 生命周期回归测试，再最小回移上游提交 `b33671239bf0b2b0efcd77a95bd7920f52425878` 中适用于 Java 8/Spring 5.3 的延迟发现语义；不回移 Java 17 或 Spring 6 配置。
- 引入仅构建期的 `jacoco-maven-plugin:0.8.15`，生成 HTML/XML 报告并为后续核心逻辑变更提供至少 60% 覆盖率证据；插件不得进入运行时依赖树。
- 更新 POM 的 BJCA fork 元数据及 README、Compatibility、Testing、GAV Mapping、Quick Start、User Manual、Release Notes，使文档从“目标待实施”同步为经验证的 SNAPSHOT 构建状态。
- 验证真实 JDK 8 下的默认 NES 全量测试、官方 5.3.39 隔离全量测试、effective POM、两套 dependency tree、Java 8 字节码、JAR package、sources/javadoc 和文档一致性。
- 范围外：CVE-2026-41710 修复与状态转为“已修复”、正式 `1.3.4-nes.patch.1` RELEASE、Nexus 部署、Git tag、Spring Boot/Spring Kafka 或其他消费者仓库改动。

## Capabilities

### New Capabilities

- 无。

### Modified Capabilities

- `platform-compatibility`：增加 Spring 5.3 全局 `RetryListener` 必须在 singleton 初始化完成后发现和应用的行为合同，并细化真实 JDK 8、字节码和双矩阵验证门禁。
- `gav-rebranding`：增加默认 NES 与命令行官方坐标参数化切换、effective POM 和 dependency tree 单一坐标集合的可验证合同。
- `maintenance-governance`：增加 JaCoCo 0.8.15 仅构建期接入、报告产物和本 change 覆盖率判定规则。

## Impact

- 直接修改范围：`pom.xml`、`RetryConfiguration`、监听器相关测试以及构建/兼容性/GAV/使用与发布说明文档。
- 上游依赖：官方 Spring Framework 5.3.39 API、NES Spring Framework `5.3.39-nes.patch.1` BOM 与模块、上游 Spring Retry 生命周期修复提交。
- 下游影响：直接 Maven/Gradle 消费者、Spring Boot BOM、Spring Kafka、Nexus 元数据和 SCA 规则后续需要切换新 GAV；本 change 只提供迁移文档和本仓库制品证据，不直接修改下游。
- 运行行为：修复全局 `RetryListener`/`StatisticsListener` 在 Spring 5.3 下未装配的问题；不改变重试公共 API、注解、异常合同或 Java package。
- 依赖与供应链：默认产品 classpath 从官方 Spring Framework GAV 改为 NES GAV；最大风险是官方/NES 混用或传递依赖回流，必须由两套隔离 dependency tree 和重复坐标检查阻断。
- 安全状态：本 change 不修复 CVE-2026-41710，漏洞总览和单篇状态继续保持“修复中”；GAV 变化和兼容性修复不得用作漏洞修复证据。
- 回滚：源码修复和 POM 变更可按本 change 的独立提交回退到当前基线；已经安装到本地仓库的 SNAPSHOT 不是正式发布，不涉及覆盖 Nexus RELEASE。
