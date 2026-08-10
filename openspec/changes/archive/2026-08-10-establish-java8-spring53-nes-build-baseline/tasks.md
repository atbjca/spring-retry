## 1. 人工审批与仓库安全门禁

- [x] 1.1 记录当前分支、HEAD、`git status --short --branch`、active OpenSpec 和可恢复点，明确保留既有 `README.md`/`doc/`/`openspec/` 维护基线变化以及未跟踪的 `.codex/`、`.cursor/`，不得覆盖用户无关内容。
- [x] 1.2 向用户再次说明根因、拟修改的 `pom.xml`/测试/`RetryConfiguration`/文档、Impact Analysis、运行与供应链风险，并取得对本 change 执行测试源码、Java 源码、POM 和依赖修改的明确 apply 授权；未获授权 MUST 停止。
- [x] 1.3 确认正式命令使用 JDK `8.0.472-amzn` 与 `/Users/anan/dev/apache-maven-3.8.2/bin/mvn`，Gradle 仅在消费者 smoke test 使用 `/Users/anan/dev/gradle-7.6.3/bin/gradle`，并记录版本输出且不打印 `~/.m2/settings.xml` 凭证。
- [x] 1.4 复核本 change 不包含 CVE-2026-41710 源码修复、RELEASE、Nexus、Git tag 或下游仓库写入；发现需要扩大范围时先更新 OpenSpec 并重新审批。

## 2. TDD RED：建立 Listener 生命周期失败证据

- [x] 2.1 只修改 listener 测试代码，在 `EnableRetryWithListenersTests` 增加“全局 `RetryListener` 与 retryable service 存在初始化依赖且在 context 就绪后仍被调用”的回归场景，断言重试次数、listener 回调次数和显式 listener 覆盖语义；此步骤不得修改生产代码或 POM。
- [x] 2.2 使用真实 JDK 8 和当前官方坐标运行 `-Dspring.framework.version=5.3.39 -Dtest=EnableRetryWithListenersTests,CircuitBreakerInterceptorStatisticsTests test`，记录新增场景及既有 `vanilla`/statistics 的 RED 结果、测试数、failure/error/skip 和关键堆栈。
- [x] 2.3 确认 RED 由 `RetryConfiguration` 过早发现 listener 的目标缺陷导致，而不是编译错误、依赖下载、私服、JDK 或测试夹具错误；若不是目标缺陷，先修正测试设计并重新取得有效 RED。
- [x] 2.4 用 `git diff` 证明 RED 阶段仅包含测试改动，并在记录有效失败前禁止进入生产实现。

## 3. TDD GREEN 与 REFACTOR：最小生命周期修复

- [x] 3.1 让 `RetryConfiguration` 实现 `SmartInitializingSingleton`，从 `afterPropertiesSet()` 移除 listener 查找，并在 `afterSingletonsInstantiated()` 中发现、排序和设置 listener；仅回移上游提交 `b33671239bf0b2b0efcd77a95bd7920f52425878` 的 Java 8/Spring 5.3 可用语义。
- [x] 3.2 保持 `RetryContextCache`、key generator、new item identifier、sleeper、pointcut、order、BeanFactory 和显式 listener 覆盖行为不变；不得带入 Java 17、Spring 6、JUnit 5 或无关上游重构。
- [x] 3.3 重新运行与 2.2 相同的定向命令，记录 GREEN：新增生命周期测试、`EnableRetryWithListenersTests` 和 `CircuitBreakerInterceptorStatisticsTests` 必须 0 failure、0 error、0 skipped。
- [x] 3.4 在 POM 重品牌前使用官方 Spring Framework 5.3.39 运行全量 `mvn test`，记录完整测试数并确认无 failure、error 或未批准 skip。
- [x] 3.5 复核生产 diff 与上游 patch，确认只包含必要生命周期语义；运行现有格式校验，新增解释性注释/Javadoc 如确有必要 MUST 优先使用中文。

## 4. POM、NES GAV、Java 8 与覆盖率构建

- [x] 4.1 在修改 POM 前复核 1.2 的授权明确覆盖 GAV、Framework 依赖和 JaCoCo 构建插件；授权不完整时 MUST 再次请求确认。
- [x] 4.2 将项目开发 GAV 改为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`，保持 `revision` 属性，并更新 name、description、BJCA organization、公共 URL/SCM/developer/issue metadata，同时保留 Apache 2.0 和上游溯源。
- [x] 4.3 将 `java.version` 改为 `1.8`，保留 Maven Compiler source/target 语义；不得宣称支持 Java 6/7 或以高版本 JDK 代替正式 JDK 8 验证。
- [x] 4.4 增加 Framework groupId、artifactId 前缀和 version 三项属性，默认指向 NES `5.3.39-nes.patch.1`，并让 BOM、context、core、test、tx 全部通过属性构造坐标且保持原 optional/test/import 语义。
- [x] 4.5 固定 `jacoco-maven-plugin:0.8.15` 为仅构建期插件，配置 `prepare-agent`、`report`、`check`，生成 HTML/XML，并对 `RetryConfiguration` 设置至少 60% 行覆盖率门禁；不得添加 `org.jacoco` 项目 dependency。
- [x] 4.6 运行 Maven model/effective POM 基础校验，确认 SNAPSHOT GAV、Java 8、默认 NES 属性、BJCA 元数据和许可证插值正确，且 POM 不含私服凭证或内部认证值。
- [x] 4.7 稳定化 `RecoverAnnotationRecoveryHandlerTests.multipleQualifyingRecoverMethodsWithNoThrowable`：保留独立 null 场景，只将该用例的歧义 `null` 参数改为明确 `String`，在 JDK 8 + 官方 Spring 5.3.39 + JaCoCo agent 下定向验证 0 failure/0 error/0 skip；不得修改恢复选择生产逻辑或弱化断言。

## 5. 官方 Spring Framework 5.3.39 隔离验证

- [x] 5.1 使用真实 JDK 8 执行独立 `clean verify`，完整覆盖 `spring.framework.group-id=org.springframework`、`spring.framework.artifact-prefix=spring-`、`spring.framework.version=5.3.39`，记录测试数、failure/error/skip、JaCoCo check 和构建结果；该 invocation 禁止 deploy。
- [x] 5.2 为官方 invocation 单独生成 effective POM 和 verbose dependency tree，确认 BOM、context、core、test、tx 及传递模块全部为官方 5.3.39，且不含 `cn.bjca.footstone.bpring:bjca-footstone-bpring-*`。
- [x] 5.3 检查官方测试中的新增 lifecycle、既有 listener、statistics 和完整套件结果，确认无测试删除、弱化断言或未批准 skip，并将证据记录到 `doc/TESTING.md`。
- [x] 5.4 明确标记官方构建产物仅为兼容性证据，不执行 RELEASE/install 发布用途；切换到 NES 前执行 clean，避免复用官方编译输出。

## 6. 默认 NES Spring Framework 5.3.39 产品链验证

- [x] 6.1 使用真实 JDK 8 和 POM 默认属性执行独立 `clean verify`，记录完整测试数、failure/error/skip、JaCoCo check 和构建结果，要求全部通过。
- [x] 6.2 生成默认 NES effective POM 和 verbose dependency tree，确认 Framework BOM、context、aop、beans、core、expression、jcl、test、tx 等全部为 `cn.bjca.footstone.bpring:*:5.3.39-nes.patch.1`，且不含正式 `org.springframework:spring-*`。
- [x] 6.3 检查 compile/runtime dependency tree 和发布 POM，确认 JaCoCo agent/report 库未成为项目依赖，官方/NES Framework 未形成双份 classpath。
- [x] 6.4 检查 `target/site/jacoco/index.html`、`jacoco.xml` 和 check 输出，确认 `RetryConfiguration` 行覆盖率至少 60%、新增 singleton 路径已执行，并记录全项目覆盖率基线；不足时只补充测试，不降低门禁。
- [x] 6.5 在所有矩阵验证后再执行一次默认 NES `clean verify`，确保工作目录最终保留的是 NES 产品链编译、测试、覆盖率和 SNAPSHOT 制品结果。

## 7. 制品、字节码与消费者 Smoke Test

- [x] 7.1 检查默认构建生成主 JAR、POM、sources JAR、javadoc JAR，文件名和 Maven 元数据必须使用 `1.3.4-nes.patch.1-SNAPSHOT` 且来自同一源码状态。
- [x] 7.2 使用 `javap -verbose` 检查代表性公共类的 class file major version 为 52，并在真实 JDK 8 上运行最小加载/调用 smoke test。
- [x] 7.3 检查 JAR 内容继续包含 `org/springframework/retry/` 与 `org/springframework/classify/`，不嵌入 Spring Framework、JaCoCo 或凭证，不要求消费者修改 Java import。
- [x] 7.4 使用真实 JDK 8 将默认 NES SNAPSHOT 安装到本地 Maven repository，在临时目录创建 Maven 消费者，以新 Retry GAV、NES Framework 和原 Java import 完成 compile/test，并检查消费者 dependency tree 无官方/NES 双份依赖。
- [x] 7.5 在临时目录使用 `/Users/anan/dev/gradle-7.6.3/bin/gradle` 和真实 JDK 8 创建等价 Gradle 消费者，以 `mavenLocal()` 解析新 GAV并完成 compile/test；Gradle 只用于消费者验证，不向本仓库增加 Gradle 构建文件。
- [x] 7.6 删除或保留临时消费者仅按可恢复策略处理，不提交其构建输出；记录 Maven/Gradle smoke test 命令和结果但不得写入本机凭证或私服真实认证信息。

## 8. 永久文档与安全状态同步

- [x] 8.1 更新 `README.md`、`SECURITY.md`、`doc/REQUIREMENTS.md`、`doc/COMPATIBILITY.md`，将 Java 8、Spring 5.3.x、默认 NES Framework 和目标 SNAPSHOT 从“待实施”改为实际已验证状态，同时明确尚未 RELEASE。
- [x] 8.2 更新 `doc/GAV_MAPPING.md`、`doc/QUICK_START.md`、`doc/USER_MANUAL.md`，写明新 SNAPSHOT/计划 RELEASE GAV、三属性官方隔离命令、Maven 与 Gradle 7.6.3 消费示例、排除官方 Retry/Framework 和保持 Java import 的迁移规则。
- [x] 8.3 更新 `doc/TESTING.md`，记录 JDK/Maven/Gradle/JaCoCo 版本、RED/GREEN 命令与结果、官方/NES 全量矩阵、effective POM/dependency tree、覆盖率、字节码和消费者 smoke test 证据位置。
- [x] 8.4 更新 `doc/VULNERABILITY_REPORT.md` 与 `doc/CVE/CVE-2026-41710.md`，只记录构建/GAV/兼容性基线进展，主状态 MUST 继续为“修复中”，不得因重品牌或 listener 修复声称 CVE 已修复。
- [x] 8.5 更新 `doc/RELEASE_GUIDE.md` 与 `doc/RELEASE_NOTES.md`，说明本 change 仅产出本地 SNAPSHOT 基线、正式 RELEASE 仍受 CVE remediation 和独立 Nexus 授权阻塞，并同步准确构建命令。
- [x] 8.6 校验所有文档相对链接、Markdown 代码块、中文主叙述、GAV/版本/状态一致性，确认未复制参考项目中的内部 Nexus 地址、密码、token、私钥或 settings 认证值。

## 9. 最终回归、范围复核与归档交接

- [x] 9.1 在文档同步后运行最终默认 NES JDK 8 `clean verify`，确认测试、JaCoCo、打包仍全绿，且没有文档或 POM 后续编辑破坏构建。
- [x] 9.2 运行 `openspec validate establish-java8-spring53-nes-build-baseline --type change --strict --no-interactive` 与 `openspec validate --all --strict --no-interactive`，要求全部通过。
- [x] 9.3 运行 `git diff --check`、变更文件清单和范围扫描，确认只修改获批的 OpenSpec、POM、listener 生产/测试代码和永久文档，未触及 CVE 修复代码、下游仓库、`.codex/` 或 `.cursor/` 内容。
- [x] 9.4 汇总分支、HEAD、测试矩阵、覆盖率、effective POM、dependency tree、JAR/字节码、消费者 smoke test、文档和剩余 CVE/RELEASE 阻塞，提交用户验收；任一证据缺失时 change MUST 保持 active。
- [x] 9.5 确认所有实现任务已完成并获得用户验收后，将 change 交给独立 `openspec-archive-change` 流程；归档前不得提前标记完成或启动 CVE remediation/RELEASE change。
