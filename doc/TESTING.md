# 测试与验证指南

## 1. 当前验证结论

`1.3.4-nes.patch.1` RELEASE 候选复用以下已经在真实 JDK 8 上完成的开发验证：

- listener 生命周期与 CVE-2026-41710 严格 TDD：有效 RED、最小 GREEN 和全量回归；
- 官方 Spring Framework 5.3.39 隔离 `clean verify`；
- 默认 NES Framework `5.3.39-nes.patch.1` 独立 `clean verify`；
- JaCoCo 0.8.15 HTML/XML/report/check；
- effective POM、完整依赖树和运行时依赖排他检查；
- 主 JAR、POM、sources、javadoc、Java 8 字节码和 JAR 内容检查；
- Maven 与 Gradle 7.6.3 消费者 smoke test。
- 当前 SNAPSHOT 的完整 `make clean deploy`、Nexus 隔离重新下载、摘要/API/依赖树和 JDK 8 远端消费者 smoke test。

这些结果证明 CVE 攻击路径已在源码与已验证的开发制品中消除。版本冻结没有修改生产源码、测试或构建逻辑，因此宽泛测试可复用；RELEASE 本地安装、元数据扫描、消费者和远端闭环仍须单独记录。

## 2. 工具链

| 工具 | 正式使用版本 | 用途 |
| --- | --- | --- |
| JDK | SDKMAN `8.0.472-amzn` | 编译、测试、JaCoCo、打包、安装和消费者验证 |
| Maven | `/Users/anan/dev/apache-maven-3.8.2/bin/mvn`，3.8.2 | 本仓库全部构建与 Maven 消费者 |
| Make | 系统 `make` + 根 `Makefile` | 封装 JDK 8、NES/官方验证、本地安装、受控部署与 OpenSpec 静态门禁 |
| Gradle | `/Users/anan/dev/gradle-7.6.3/bin/gradle`，7.6.3 | 仅用于临时消费者，不是本仓库构建系统 |
| JaCoCo | `jacoco-maven-plugin:0.8.15` | 仅构建期 agent、HTML/XML、check |
| 测试框架 | JUnit 4 | 保持 1.3.x 现有测试栈 |

实测 Maven 环境：

```text
Apache Maven 3.8.2
Java version: 1.8.0_472, vendor: Amazon.com Inc.
```

实测 Gradle 环境：

```text
Gradle 7.6.3
JVM: 1.8.0_472 (Amazon.com Inc.)
```

依赖解析允许使用用户 `~/.m2/settings.xml` 和本地 repository，但不得把 settings 内容、凭证或实际私服认证值写入日志摘录和文档。

Makefile 默认读取 `JAVA8_HOME=$(HOME)/.sdkman/candidates/java/8.0.472-amzn` 与 `MAVEN=$(HOME)/dev/apache-maven-3.8.2/bin/mvn`，均可覆盖。`make deploy` 根据版本选择 snapshots/releases，RELEASE 必须 `ALLOW_RELEASE_DEPLOY=true`；它不提供 Git tag/push。Gradle 消费者命令仍独立执行。

## 3. Listener 生命周期 TDD

### RED

生产代码和 POM 修改前，在 `EnableRetryWithListenersTests` 增加 `listenerDependingOnRetryableBean`，与既有 `vanilla`、`overrideListener` 和 circuit breaker statistics 一起执行：

```bash
mvn \
  -Dspring.framework.version=5.3.39 \
  -Dtest=EnableRetryWithListenersTests,CircuitBreakerInterceptorStatisticsTests \
  test
```

结果：

```text
Tests run: 4, Failures: 2, Errors: 1, Skipped: 0
```

- `vanilla`：全局 listener 回调计数为 0；
- `listenerDependingOnRetryableBean`：初始化依赖场景未装配全局 listener；
- `testCircuitOpenWhenNotRetryable`：`StatisticsListener` 未生效，统计记录为空并触发错误；
- `overrideListener` 保持通过。

失败来自 `RetryConfiguration.afterPropertiesSet()` 过早发现 listener，而不是编译、依赖下载、JDK 或夹具错误。RED 阶段 diff 只有测试变更。

### GREEN / REFACTOR

最小生产修改：

- `RetryConfiguration` 实现 `SmartInitializingSingleton`；
- 从 `afterPropertiesSet()` 移除 listener 查找；
- 在 `afterSingletonsInstantiated()` 中发现、排序并设置 listener；
- 不改变 cache、key generator、sleeper、pointcut、order、BeanFactory 或显式 listener 覆盖语义。

使用同一组定向测试复验：

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

POM 重品牌前，官方 Spring Framework 5.3.39 全量 `mvn test` 为 314 tests、0 failure、0 error、0 skipped。

## 4. CVE-2026-41710 TDD

### RED

生产缓存与路由代码修改前，分层增加 JUnit 4 测试：

- policy：已有 key 满容量更新、访问顺序 LRU、严格模式、固定 WARN、soft-reference 清理和并发容量边界；
- support：普通/断路器相同 raw key 隔离、成功/耗尽清理、持续唯一普通 key；
- annotation：两个命名 Bean、唯一未命名 Bean 回退、多个未命名 Bean 的安全默认值。

第一阶段缺少双参数构造器和断路器 setter，测试按预期无法编译；补齐测试夹具后，行为断言继续因旧 HashMap fail-fast、单缓存和唯一 Bean 选择逻辑失败。每个 RED 阶段均用文件级 diff 确认只修改测试，失败不来自 JDK、依赖或环境。

### GREEN / REFACTOR

最小实现回移上游 commit `6f351edae3d3575fffbde3c0f62fef963dacd152` 的必要语义，并增加同 raw key 的 policy-aware 读取加固：

- 新增 `AbstractMapRetryContextCache`，普通缓存默认有界 LRU，`(capacity, false)` 保留严格 fail-fast；
- `RetryTemplate` 使用普通/断路器双缓存；
- interceptor 与 configuration 分别装配两个缓存和两个约定 Bean；
- 严格容量复合操作使用同一 map monitor；不新增运行时依赖。

官方 Spring Framework 5.3.39 定向组合结果：47 tests、0 failure、0 error、0 skipped。并发测试证明严格缓存中成功保存的不同 key 数不超过 capacity，已有 key 并发更新不会被错误拒绝。

## 5. JaCoCo 暴露的遗留测试非确定性

首次在全量套件启用 JaCoCo agent 时，`RecoverAnnotationRecoveryHandlerTests.multipleQualifyingRecoverMethodsWithNoThrowable` 向 `(String, String)` 与 `(int, String)` 两个 `@Recover` 重载传入歧义 `null`。反射/HashMap 顺序变化后可能选择 primitive 重载并抛 `IllegalArgumentException`。

处理原则：

- 保留相邻的独立 null 行为测试；
- 只把无 `Throwable` 用例输入改为明确的 `"Randell"`；
- 不修改生产恢复方法选择逻辑，不降低断言。

JDK 8、官方 Spring 5.3.39、JaCoCo agent 定向结果：

```text
RecoverAnnotationRecoveryHandlerTests
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
```

## 6. Spring Framework 双矩阵

### 官方 5.3.39 隔离兼容性

```bash
/Users/anan/dev/apache-maven-3.8.2/bin/mvn \
  -Dspring.framework.group-id=org.springframework \
  -Dspring.framework.artifact-prefix=spring- \
  -Dspring.framework.version=5.3.39 \
  clean verify
```

### 默认 NES 产品链

```bash
/Users/anan/dev/apache-maven-3.8.2/bin/mvn clean verify
```

| 场景 | Tests | Failures | Errors | Skipped | JaCoCo check | Build |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| 官方 Spring Framework 5.3.39 | 328 | 0 | 0 | 0 | 通过 | SUCCESS |
| 默认 NES Framework `5.3.39-nes.patch.1` | 328 | 0 | 0 | 0 | 通过 | SUCCESS |
| 默认 NES `mvn install` | 328 | 0 | 0 | 0 | 通过 | SUCCESS |

官方 invocation 只产生临时兼容性证据，未执行 install/deploy。切换到 NES 前重新 `clean`，最终工作目录保留默认 NES 编译结果。

## 7. 覆盖率

报告位置：

```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
```

最终默认 NES 报告：

| 范围 | Covered | Missed | 行覆盖率 |
| --- | ---: | ---: | ---: |
| `AbstractMapRetryContextCache` | 25 | 0 | 100.0% |
| `MapRetryContextCache` | 10 | 0 | 100.0% |
| `SoftReferenceMapRetryContextCache` | 19 | 0 | 100.0% |
| `RetryTemplate` | 148 | 14 | 91.4% |
| `AnnotationAwareRetryOperationsInterceptor` | 181 | 15 | 92.3% |
| `RetryConfiguration` | 62 | 3 | 95.4% |
| 全项目 | 1805 | 282 | 86.5% |

所有本 change 新增/修改核心类均超过 60% 行覆盖率；`verify` 输出 `All coverage checks have been met.`。JaCoCo 只在 `<build><plugins>` 中出现，运行时 dependency list 不含 `org.jacoco`。

## 8. Effective POM 与依赖树

默认 NES：

```bash
mvn help:effective-pom -Doutput=target/effective-pom.xml
mvn dependency:tree -Dverbose -DoutputFile=target/dependency-tree.txt
mvn dependency:list -DincludeScope=runtime -DoutputFile=target/dependency-list-runtime.txt
```

结果：

- 开发验证时项目 GAV 为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`；当前 RELEASE 候选为同坐标的 `1.3.4-nes.patch.1`；
- Framework BOM、context、aop、beans、core、expression、jcl、test、tx 全部为 NES `5.3.39-nes.patch.1`；
- 默认依赖树不含 `org.springframework:spring-*`；
- 官方依赖树全部为 `org.springframework:spring-*:5.3.39`，不含 NES Framework；
- 运行时依赖只包含 NES context/aop/beans/core/expression/jcl，不含 JaCoCo；
- effective POM 中由用户 Maven settings 注入的 repository 信息不是仓库 POM 内容，禁止复制到永久文档。

## 9. 制品与字节码

RELEASE 候选 `make install-local` 应生成：

```text
bjca-footstone-bpring-retry-1.3.4-nes.patch.1.jar
bjca-footstone-bpring-retry-1.3.4-nes.patch.1.pom
bjca-footstone-bpring-retry-1.3.4-nes.patch.1-sources.jar
bjca-footstone-bpring-retry-1.3.4-nes.patch.1-javadoc.jar
```

`javap -verbose` 检查新增/修改代表类：

```text
major version: 52
```

主 JAR 内容检查：

- 144 个条目；
- 112 个 `org/springframework/retry/` 条目；
- 23 个 `org/springframework/classify/` 条目；
- 无嵌入的 Spring Framework 实现包；
- 无 `org/jacoco/`；
- 无 settings、私钥、credentials、password、token 等可疑文件名；
- 内嵌 Maven POM 与当前根 POM 一致。
- 包含 `AbstractMapRetryContextCache`、两个双参数构造器、`RetryTemplate.setCircuitBreakerRetryContextCache(...)` 和 interceptor 同名 setter；旧构造器与 `setRetryContextCache(...)` 保留。

## 10. 消费者 smoke test

### Maven/JDK 8

临时消费者使用新 Retry GAV、NES context 和原 `org.springframework.retry.*` import：

```bash
/Users/anan/dev/apache-maven-3.8.2/bin/mvn -o clean test
```

结果：2 tests、0 failure、0 error、0 skipped。测试覆盖旧普通缓存 setter、新断路器 setter 和两个命名 Bean；dependency tree 只含新 Retry GAV 与 NES context/aop/beans/core/expression/jcl，没有官方 Spring 或 JaCoCo。

### Gradle 7.6.3/JDK 8

临时消费者使用 `mavenLocal()`：

```bash
JAVA_HOME=/Users/anan/.sdkman/candidates/java/8.0.472-amzn \
  /Users/anan/dev/gradle-7.6.3/bin/gradle \
  -Dorg.gradle.native=false --no-daemon --offline --console=plain clean test dependencies
```

结果：2 tests、0 failure、0 error、0 skipped，`BUILD SUCCESSFUL`。runtime classpath 只包含新 Retry GAV 与 NES Framework；消费者测试类 major version 52。

### Nexus SNAPSHOT/JDK 8

用户授权后执行 `make clean deploy`，完整生命周期再次运行 328 tests、JaCoCo、Javadoc、主 JAR、POM、sources 和 javadoc 上传，结果为 `BUILD SUCCESS`。随后使用空的隔离 Maven local repository 强制重新解析同一 SNAPSHOT：

- 远端四件套与本地候选逐件 SHA-256 一致；
- `AbstractMapRetryContextCache`、双参数缓存构造器和 `setCircuitBreakerRetryContextCache(...)` 均存在；
- 代表性 class major version 为 52；
- 远端 POM dependency tree 只含 NES Framework `5.3.39-nes.patch.1`，不含官方 Spring；
- JDK 8 临时远端消费者 1 test、0 failure、0 error、0 skipped。

RELEASE 门禁只验证了版本选择和显式确认逻辑，本 change 没有实际部署 RELEASE。

## 11. 本次证据位置

Maven 默认 `target/` 会被后续 `clean` 覆盖，因此本次矩阵证据同时复制到本机临时目录：

```text
/tmp/spring-retry-baseline-evidence-20260806/official/
/tmp/spring-retry-baseline-evidence-20260806/nes/
```

本次 CVE remediation 的主要临时证据：

| 证据 | 位置 |
| --- | --- |
| 官方 effective POM | `/tmp/spring-retry-official-effective-pom.xml` |
| 官方 dependency tree | `/tmp/spring-retry-official-dependency-tree.txt` |
| NES effective POM | `/tmp/spring-retry-nes-effective-pom.xml` |
| NES dependency tree | `/tmp/spring-retry-nes-dependency-tree.txt` |
| Maven/Gradle 消费者 | `/tmp/spring-retry-consumers.Yd1rc5/` |
| Maven 消费者 dependency tree | `/tmp/spring-retry-consumers.Yd1rc5/maven-dependency-tree.txt` |
| Gradle Surefire XML | `/tmp/spring-retry-consumers.Yd1rc5/gradle/build/test-results/test/` |
| Nexus SNAPSHOT 隔离仓库与远端消费者 | `/tmp/spring-retry-nexus-verify.2sAWFe/` |

这些 `/tmp` 文件是本机可恢复证据，不提交仓库，也不包含 settings 凭证。长期可重复证据仍以本文命令、OpenSpec tasks 和最终构建结果为准。

早期本地消费者首次非离线 Maven invocation 因用户 settings 自动读取一次 SNAPSHOT metadata；后续已按用户授权完成正式 SNAPSHOT deploy 与隔离远端验证。永久文档不记录真实私服 URL或认证值。

## 12. 环境性失败记录

首次官方 `clean verify` 在受限执行环境中已完成 314/314 测试，但 javadoc 插件缺失依赖需要写入 `~/.m2`，受沙箱只读限制而失败。获准写入 Maven cache 后，在未修改产品代码的情况下完整重跑并成功。该事件归类为构建环境失败，不是产品回归。

环境或私服错误必须与测试失败分开记录；不得用跳过 javadoc、关闭 JaCoCo、增加 skip 或重跑偶然成功来掩盖问题。

## 13. 最终验收命令

RELEASE change 的最小重新验证为：

```bash
make install-local
make validate OPEN_SPEC_CHANGE=release-1-3-4-nes-patch-1
```

本次已完成：`make install-local` 成功，生成 POM 扫描 `clean=true` 且无内部 SNAPSHOT，代表性 Maven/JDK 8 consumer 1/1 通过。`make validate` 会运行 active change strict、全项目 strict 和 `git diff --check`。宽泛 `make verify`/`make test` 只在源码、测试或构建逻辑变化使开发证据失效时重跑；门禁测试不得实际执行 RELEASE deploy。

任何测试、覆盖率、坐标排他、制品、消费者、文档或 OpenSpec 证据失败时，change 保持 active。
