## Context

本仓库是 Spring Retry 1.3.x 的单模块 NES 维护分支。当前构建事实与已经批准的维护合同存在三处关键偏差：

- 项目仍声明 `org.springframework.retry:spring-retry:1.3.5-SNAPSHOT`、Java 1.6 和 Spring Framework 4.3.29；
- 目标开发坐标应为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`，默认 Framework 应为 NES `5.3.39-nes.patch.1`；
- 使用真实 JDK 8 和官方 Spring Framework 5.3.39 执行 313 个测试时，结果为 1 个 failure、1 个 error、0 个 skipped，失败集中在全局 `RetryListener`/`StatisticsListener` 未被装配。

定向 RED 复现为：

```text
EnableRetryWithListenersTests.vanilla
└── expected 1 but was 0

CircuitBreakerInterceptorStatisticsTests.testCircuitOpenWhenNotRetryable
└── NullPointerException（StatisticsListener 未生效，统计记录不存在）
```

根因是 `RetryConfiguration.afterPropertiesSet()` 在 Spring 5.3 的 advisor/singleton 生命周期中过早查找 `RetryListener`。上游提交 `b33671239bf0b2b0efcd77a95bd7920f52425878` 通过 `SmartInitializingSingleton` 延后该查找；同一提交还把上游构建切到 Java 17 和 Spring 6，这两部分不适用于本维护分支。

本机已验证：

- JDK `8.0.472-amzn` 可用于正式构建；
- Maven `3.8.2` 可复用，仓库不需要 Gradle；
- Maven Central 在 2026-06-05 更新的最新稳定 JaCoCo Maven Plugin 为 `0.8.15`；其插件 POM 使用 `maven.compiler.release=8`，并已在本机 JDK 8 + Maven 3.8.2 下真实启动成功；
- NES Framework BOM、context、core、test、tx 及传递模块 `5.3.39-nes.patch.1` 已存在于本地 Maven repository；
- 临时 POM spike 证明 `groupId + artifactId 前缀 + version` 三个属性可在默认全 NES 与命令行全官方两棵依赖树之间切换，未出现坐标混用。

利益相关方包括本仓库维护者、后续 CVE 修复与发布流程、直接消费者、Spring Boot/Spring Kafka 维护仓库、Nexus 管理者和 SCA/漏洞审计人员。

## Goals / Non-Goals

**Goals:**

- 使默认 Maven 构建使用目标 NES SNAPSHOT GAV、Java 8 和 NES Spring Framework 5.3.39。
- 用隔离的命令行属性运行官方 Spring Framework 5.3.39 全量兼容性测试，且任何一次 classpath 只存在一套 Framework 坐标。
- 按 RED → GREEN → REFACTOR 回移最小 `RetryListener` 生命周期语义，使现有 listener/statistics 回归测试全绿。
- 生成可重复的 JaCoCo HTML/XML 覆盖率证据，并对本次修改的 `RetryConfiguration` 保持至少 60% 行覆盖率。
- 保持 `org.springframework.retry.*`、`org.springframework.classify.*`、公共 API、注解和异常合同不变。
- 使 POM、README、兼容性、测试、GAV、快速入门、用户手册和发布说明与实际验证后的 SNAPSHOT 状态一致。

**Non-Goals:**

- 不修复 CVE-2026-41710，不将其状态改为“已修复”“免疫”或“不适用”。
- 不生成、部署或宣称 `1.3.4-nes.patch.1` RELEASE，不访问或覆盖 Nexus RELEASE，不创建 Git tag。
- 不维护 Spring Framework 4.x 或 Java 6/7 兼容性。
- 不引入 Spring 6、Java 17、JUnit 5 或上游 2.x 的其他结构性变化。
- 不修改 Spring Boot、Spring Kafka 或业务消费者仓库。
- 不引入新的运行时第三方库、框架或设计模式。

## Decisions

### 1. 使用三属性参数化 Spring Framework 坐标

POM SHALL 定义并在 BOM、context、core、test、tx 的直接声明中统一使用：

```text
spring.framework.group-id       = cn.bjca.footstone.bpring
spring.framework.artifact-prefix = bjca-footstone-bpring-
spring.framework.version        = 5.3.39-nes.patch.1
```

默认插值结果为：

```text
cn.bjca.footstone.bpring:bjca-footstone-bpring-framework-bom:5.3.39-nes.patch.1
cn.bjca.footstone.bpring:bjca-footstone-bpring-context:5.3.39-nes.patch.1
cn.bjca.footstone.bpring:bjca-footstone-bpring-core:5.3.39-nes.patch.1
cn.bjca.footstone.bpring:bjca-footstone-bpring-test:5.3.39-nes.patch.1
cn.bjca.footstone.bpring:bjca-footstone-bpring-tx:5.3.39-nes.patch.1
```

官方兼容验证使用同一个 POM，通过命令行覆盖为：

```text
-Dspring.framework.group-id=org.springframework
-Dspring.framework.artifact-prefix=spring-
-Dspring.framework.version=5.3.39
```

选择理由：三项属性同时覆盖 BOM 与全部直接 Framework 依赖，减少漏改单个 artifact 的风险；临时 POM spike 已证明 effective dependency entries 和完整传递依赖树分别形成全 NES、全官方闭环。

备选方案：

- Maven profile：容易把官方 profile 或额外 repository 固化进发布 POM，也增加“激活了哪个 profile”的审计歧义，因此不采用。
- 维护第二份兼容性 POM：隔离更强，但会复制项目依赖和插件配置并产生长期漂移，因此不采用。
- 只覆盖 `spring.framework.version`：不能切换 group/artifact，必然无法验证 NES 重品牌闭环，因此不可用。

### 2. 默认项目身份使用 NES SNAPSHOT，RELEASE 留给独立 change

本 change SHALL 将项目改为：

```text
cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT
```

`revision` 继续作为版本属性，以便未来独立 RELEASE change 在完整门禁通过后显式切换为 `1.3.4-nes.patch.1`。POM 的 name、description、organization、URL、SCM、developer/issue metadata SHALL 指向公共 BJCA fork `https://github.com/atbjca/spring-retry`，同时保留 Apache 2.0 许可证和文档中的上游来源/补丁溯源。

选择理由：SNAPSHOT 能准确表达“已建立构建基线但 CVE 和发布尚未完成”，防止当前制品被误认为正式安全 RELEASE。

### 3. 只回移 `RetryListener` 延迟发现语义

生命周期调整如下：

```text
当前 Spring 5.3 流程
────────────────────────────────────────────────────────────
RetryConfiguration.afterPropertiesSet()
  ├─ 过早查找 RetryListener
  ├─ 未发现或触发 bean 创建时序问题
  └─ 创建不含 globalListeners 的 advice
                         │
                         ▼
运行 @Retryable / @CircuitBreaker
  └─ listener/statistics 缺失 → failure / error

目标流程
────────────────────────────────────────────────────────────
RetryConfiguration.afterPropertiesSet()
  ├─ 查找 RetryContextCache / key generator / sleeper
  └─ 创建 advisor 与 interceptor
                         │
                         ▼
Spring 完成普通 singleton 实例化
                         │
                         ▼
afterSingletonsInstantiated()
  ├─ 一次性发现并排序 RetryListener beans
  └─ 设置到既有 AnnotationAwareRetryOperationsInterceptor
                         │
                         ▼
Context 就绪后的重试调用
  └─ global listener 与 statistics 正常工作
```

实现 SHALL：

- 让 `RetryConfiguration` 实现 Spring 5.3 已提供的 `SmartInitializingSingleton`；
- 从 `afterPropertiesSet()` 移除 `RetryListener` 查找；
- 在 `afterSingletonsInstantiated()` 中查找并设置 listener；
- 将内部 advice 保持为可直接设置 listener 的 `AnnotationAwareRetryOperationsInterceptor`；
- 保留其他 bean 查找、pointcut、order、BeanFactory 注入和 listener 排序语义；
- 不复制上游提交中的 Java 17、Spring 6、菱形语法或其他超出 Java 8/当前代码风格的改动。

选择理由：这是上游已采用的 Spring 生命周期接口，没有新运行时依赖，且比 `@Lazy`、捕获 `BeanCurrentlyInCreationException` 或删除全局 listener 更精确。

备选方案：

- 在 `afterPropertiesSet()` 强制 eager-init listener：会重新引入 bean 创建环和 advisor 尚未就绪风险，不采用。
- 在 `ContextRefreshedEvent` 后设置：时机更晚且引入事件监听结构，不如 `SmartInitializingSingleton` 精确，不采用。
- 禁用或跳过失败测试：掩盖真实运行行为，不符合兼容性合同，禁止。

支持合同从 ApplicationContext 完成 singleton 初始化后开始保证全局 listener。bean 构造期间主动调用 `@Retryable` 方法属于高风险初始化模式，本 change 不扩展该早期调用合同。

### 4. TDD 使用新增生命周期场景和既有端到端回归双重证明

RED 阶段 SHALL 先增加一个 listener 生命周期回归场景，验证一个与 retryable service 存在初始化依赖的全局 `RetryListener` 在 Spring 5.3 context 就绪后仍被应用；当前实现必须因目标生命周期缺陷失败，而不能因依赖下载、语法或环境错误失败。

同时保留并运行：

- `EnableRetryWithListenersTests`：证明默认全局 listener 与显式 listener 覆盖行为；
- `CircuitBreakerInterceptorStatisticsTests`：证明 `StatisticsListener` 能建立断路器统计记录。

GREEN 阶段只实现决策 3 的最小生产代码。REFACTOR 阶段不得删除测试、降低断言、添加 skip 或改变 listener 顺序。

### 5. Java 8 由真实运行时和字节码共同门禁

POM SHALL 将 `java.version` 设为 `1.8`，继续由 Maven Compiler Plugin 的 source/target 生成 Java 8 字节码。正式命令使用 SDKMAN 中的真实 JDK 8，而不是只在 JDK 17 上设置 source/target。

验证 SHALL 同时包含：

- `mvn -version` 显示 JDK 8；
- 编译、测试、JaCoCo 和打包全部在 JDK 8 完成；
- `javap -verbose` 检查代表性公共类的 major version 为 52；
- 使用 JDK 8 启动最小 classpath smoke test；
- JDK 11/17 若执行，仅记为辅助结果。

不引入 Maven Toolchains 或 Enforcer 作为本 change 的必要新插件，以降低构建变化面；真实 JDK 8 命令与字节码检查提供等价且更直观的证据。

### 6. JaCoCo 0.8.15 仅作为构建期质量门禁

POM SHALL 固定 `jacoco-maven-plugin:0.8.15`，绑定：

- `prepare-agent`：测试前挂载 agent；
- `report`：在 `verify` 生成 `target/site/jacoco/index.html` 和 `jacoco.xml`；
- `check`：对本次修改的 `org.springframework.retry.annotation.RetryConfiguration` 执行至少 60% 行覆盖率门禁。

旧代码的全局覆盖率只记录基线，不在本 change 擅自设定统一阈值；如果目标类不足 60%，必须补充有业务意义的测试，不能降低阈值或排除目标类。后续 CVE change 对其修改的安全路径单独设置覆盖证据。

选择 `0.8.15` 的依据是 Maven Central 最新稳定元数据、插件 Java 8 编译目标和本机真实 JDK 8 启动结果。JaCoCo 只出现在 `<build><plugins>`，不得出现在 `<dependencies>` 或发布制品运行时 classpath。

备选的本地缓存版本 0.8.10/0.8.11 虽可用，但不是当前稳定版本；在已验证 0.8.15 可运行的情况下不采用旧版本。

官方 Framework 全量 `clean verify` 首次启用 JaCoCo 插桩后，遗留测试
`RecoverAnnotationRecoveryHandlerTests.multipleQualifyingRecoverMethodsWithNoThrowable`
暴露了一个与本次 listener 生产修改无关的测试非确定性：测试向同时存在
`(String, String)` 与 `(int, String)` 两个无 `Throwable` 的 `@Recover` 重载传入
`null`，而 `null` 无法区分引用类型和 primitive 重载。反射方法进入 `HashMap` 的
顺序并非测试合同，JaCoCo 插桩改变该顺序后可能选择 `int` 重载，最终因向 primitive
参数传入 `null` 抛出 `IllegalArgumentException`。不带 JaCoCo 的 314 个测试全部通过，
且相邻的 `multipleQualifyingRecoverMethodsWithNull` 已专门覆盖 `null` 行为，因此 SHALL
只把该无 `Throwable` 用例输入改为明确的 `String`，使其稳定验证 String overload；
不得修改恢复方法选择的生产逻辑、删除独立 null 用例或降低断言。

### 7. 两套 Spring 5.3 验证必须是独立的 clean invocation

验证流如下：

```text
                    ┌──────────────────────────┐
                    │ 源码/POM apply 人工批准 │
                    └────────────┬─────────────┘
                                 ▼
                      RED → GREEN → REFACTOR
                                 │
                 ┌───────────────┴────────────────┐
                 ▼                                ▼
      默认 NES clean verify             官方 5.3.39 clean verify
      cn.bjca... only                    org.springframework only
                 │                                │
                 ├─ effective POM                 ├─ effective POM
                 ├─ dependency tree               ├─ dependency tree
                 ├─ 全量测试/JaCoCo                └─ 仅兼容验证，禁止 deploy
                 └─ SNAPSHOT 制品
                 └───────────────┬────────────────┘
                                 ▼
                  字节码/JAR/文档/安全状态校验
                                 ▼
                         OpenSpec 严格校验
```

每次矩阵切换前必须执行 `clean`，防止复用上一套 Framework 编译输出。官方验证不得执行 `install`/`deploy` 作为发布证据；最终工作目录 SHALL 再执行一次默认 NES `clean verify`，确保保留的是产品链结果。

dependency tree 判定：

- 默认 NES 树不得出现 `org.springframework:spring-*`；
- 官方树不得出现 `cn.bjca.footstone.bpring:bjca-footstone-bpring-*`；
- 两棵树均不得包含官方与 NES 两份同名 Spring package JAR；
- JaCoCo 不得出现在项目运行时 dependency tree。

默认 NES `install` 完成后 SHALL 在临时目录创建 Maven 与 Gradle 消费者 smoke test。两者使用新 Retry GAV、NES Framework 依赖和原有 `org.springframework.retry.*` import 完成 Java 8 编译/最小运行；Gradle 使用 `~/dev` 中已发现且可在 Java 8 上运行的 7.6.3，仅验证消费者，不把 Gradle 引入本仓库构建。

## Impact Analysis

| 维度 | 受影响内容 | 预期影响 | 验证/控制 |
| --- | --- | --- | --- |
| 上游 | Spring Framework 5.3.39、NES Framework 5.3.39、上游 Spring Retry commit | 使用 Spring 5.3 生命周期接口并回移单一语义 | API 编译、commit diff 对照、禁止 Spring 6 API |
| 本仓库模块 | 单一 Maven 模块、POM、`RetryConfiguration`、listener/statistics 测试 | 构建身份和一个内部生命周期变化 | RED/GREEN、全量测试、Git diff 范围复核 |
| 公共 API | `org.springframework.retry.*`、`org.springframework.classify.*` | 无类名、方法签名、注解或 import 变化 | JAR 内容/API 检查、源码搜索 |
| 构建元数据 | 项目 GAV、Framework GAV、Java 版本、BJCA SCM、JaCoCo 插件 | 消费者必须切换 Maven 坐标；默认 POM 形成 NES 闭环 | effective POM、dependency tree、GAV 文档 |
| 运行行为 | 全局 listener 和 statistics 在 context 就绪后生效 | 修复当前缺失；listener 顺序和显式覆盖规则不变 | listener 与 circuit breaker 回归测试 |
| 下游消费者 | 直接依赖、Spring Boot BOM、Spring Kafka、SCA | 后续需独立 change 替换坐标和排除官方制品 | 临时 Maven/Gradle smoke test；本 change 不跨仓写入 |
| 发布/Nexus | 只生成本地 SNAPSHOT | 不产生 RELEASE 或远端不可逆状态 | 禁止 deploy；RELEASE 留给独立审批 change |
| 数据安全 | 不处理持久数据、身份、权限或业务输入 | SQL 注入、越权、反序列化风险无新增攻击面 | 记录不适用结论，安全测试聚焦依赖与资源行为 |
| 资源耗尽 | listener 一次性发现、测试 agent、重试测试 | 构建耗时和内存略增；不增加运行时循环或线程 | 一次性 lifecycle callback、构建日志与超时观察 |
| 路径处理 | Maven `target/`、JaCoCo 报告、effective POM/tree 文件 | 仅固定构建输出，无用户可控路径拼接 | 使用默认/明确相对路径，不写入源码或凭证目录 |
| 依赖供应链 | 新增 JaCoCo build plugin，Framework 改为 NES GAV | 存在插件来源、依赖混用和 dependency confusion 风险 | 固定版本、使用 Maven settings、两树排他检查、运行时树检查 |
| 漏洞审计 | GAV 改名可能影响 SCA 识别 | 不能因改名误判 CVE 已修复 | CVE-2026-41710 继续“修复中”，文档同步校验 |
| 回滚 | POM、源码、测试和文档均在一个未发布 change 内 | 可恢复到实施前 commit；本地 SNAPSHOT 可被后续覆盖 | 实施前确认分支/工作树，禁止覆盖用户无关改动 |

## Risks / Trade-offs

- [官方/NES 坐标混用导致重复类或运行时链接错误] → 所有 Framework 直接依赖共用三属性；两套 clean dependency tree 做互斥扫描，任一混用立即失败。
- [命令行覆盖遗漏一个属性] → 文档提供完整三属性命令；tasks 将 effective POM 和 dependency tree 同时作为验收，不能只看测试结果。
- [listener 在 singleton 构造期间被调用时尚未设置] → 明确支持合同从 singleton 初始化完成后开始；保留上游最小语义，不扩展高风险早期调用模式。
- [interceptor 已缓存不含 listener 的 delegate] → 新增初始化依赖回归场景，并要求 context 就绪后 listener/statistics 端到端测试；若发现早期缓存路径，停止实施并回到 design 评审，不能擅自扩大补丁。
- [JaCoCo 影响 Surefire `argLine` 或 Java 8 构建] → 已验证插件可在 JDK 8 启动；先做定向测试再做全量 `verify`，插件只在构建期。
- [JaCoCo 插桩改变反射/HashMap 遍历顺序，使含歧义 `null` 的遗留恢复测试偶发选择 primitive 重载] → 保留独立 null 行为测试，只把无 `Throwable` 用例改为明确的 `String` 输入，并在 JaCoCo agent 下定向验证；不得以修改生产匹配算法掩盖测试夹具歧义。
- [目标类覆盖率不足 60%] → 增加针对生命周期分支的测试，不降低阈值、不排除类；全局遗留覆盖率只记录不设硬门禁。
- [GAV 重品牌使 SCA 暂时失去官方组件映射] → 保留上游来源、版本与 CVE 独立文档，禁止把新坐标视作免疫或修复证据。
- [当前工作树已有维护基线文档和未跟踪 AI 配置] → apply 前再次检查分支和 `git status`；只修改 change 明确列出的文件，保留 `.codex/`、`.cursor/` 和用户无关变化。
- [依赖下载或私服波动被误判为产品失败] → RED 必须是断言/目标异常；环境失败单独记录并复用 `~/.m2/settings.xml` 与本地 repository，不输出凭证。
- [官方兼容构建产物被误部署] → 官方 invocation 明确禁止 deploy/install 发布用途；最终再运行默认 NES clean verify，正式发布仍需独立 OpenSpec 和授权。

## Migration Plan

1. 在 apply 前报告当前分支、HEAD、tracked/untracked 状态和可恢复点，取得用户对 `pom.xml`、测试和 Java 源码的明确授权。
2. 使用当前 POM 的官方 `-Dspring.framework.version=5.3.39` 路径先新增并记录 RED listener 生命周期测试；保留既有两个失败作为补充证据。
3. 回移最小 `SmartInitializingSingleton` 语义，运行定向 GREEN 和相关回归；若出现超出设计的早期 delegate 缓存问题，暂停并重新评审。
4. 修改项目 SNAPSHOT GAV、Java 8、Framework 参数化坐标、BJCA POM 元数据和 JaCoCo 构建插件。
5. 分别执行官方与 NES 的独立 clean 验证，保存测试数、失败/错误/skip、effective POM、dependency tree、覆盖率和字节码证据；最后保留默认 NES 构建结果。
6. 将默认 SNAPSHOT 安装到本地 Maven repository，并在临时目录执行 Maven 与 Gradle 7.6.3 Java 8 消费者 smoke test，验证新 GAV、NES Framework 闭环和原 Java import。
7. 更新永久文档，只把已经验证的内容从“目标”改为“当前 SNAPSHOT”；CVE 状态继续“修复中”。
8. 执行 OpenSpec strict 校验、文档链接/敏感信息/GAV/CVE 状态检查和 Git diff 范围复核；所有任务完成后归档本 change。

回滚策略：在任何 RELEASE/Nexus 操作之前，以实施前 commit 为恢复点回退本 change 的独立修改；不删除或覆盖用户已有文件。若仅某个验证矩阵失败，change 保持 active 并修正，不发布部分结果。已生成的本地 `target/` 和 SNAPSHOT 不构成外部不可逆状态。

## Open Questions

当前没有阻塞提案的技术问题。apply 阶段仍需人工确认源码/POM 修改授权；JaCoCo 实际报告若显示 `RetryConfiguration` 低于 60%，按本设计补充测试而不是调整合同。任何需要扩大到 CVE 修复、运行时新依赖、Spring 6、下游仓库或 Nexus 的发现都必须新建或更新 OpenSpec 并重新取得批准。
