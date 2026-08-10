# 兼容性说明

## 1. 当前已验证合同

| 维度 | 当前事实 | 验证状态 |
| --- | --- | --- |
| Java 编译与运行 | Java 8，POM `source/target=1.8` | 真实 JDK `8.0.472-amzn` 构建；代表类 major version 52 |
| Spring Framework | 仅承诺 5.3.x | 官方 5.3.39 与 NES `5.3.39-nes.patch.1` 双矩阵全绿 |
| 默认产品链 | NES Framework `5.3.39-nes.patch.1` | effective POM 与依赖树为全 NES 闭环 |
| 官方兼容场景 | 官方 Framework 5.3.39 | 三属性覆盖、独立 `clean verify`，仅作兼容性证据 |
| Maven | `/Users/anan/dev/apache-maven-3.8.2/bin/mvn` | 正式维护验证基线 |
| Gradle | 仓库无 Gradle 构建 | Gradle 7.6.3 + JDK 8 消费者 smoke test 已通过 |
| Java package | `org.springframework.retry.*`、`org.springframework.classify.*` | JAR 和消费者编译验证保持不变 |
| 当前制品 | `1.3.4-nes.patch.1-SNAPSHOT` | 本地构建并安装；Nexus SNAPSHOT 已部署和隔离验证 |

当前 SNAPSHOT 已验证 CVE-2026-41710 源码修复和 Nexus 远端制品，但不是安全 RELEASE；CVE 仍处于“修复中（源码与 Nexus SNAPSHOT 已验证，待 RELEASE）”。

## 2. 支持范围

当前维护合同承诺：

- Java 8；
- Spring Framework 5.3.x；
- Maven 构建；
- 现有 `org.springframework.retry.*`、`org.springframework.classify.*` package 和公共 API 身份；
- 官方 Spring Framework 5.3.39 API 兼容性；
- NES Spring Framework `5.3.39-nes.patch.1` 产品链兼容性。

明确不承诺：

- Java 6 或 Java 7；
- Spring Framework 4.x；
- Spring Framework 6.x API；
- 使用 Gradle 构建本仓库；
- 同一 classpath 混用官方与 NES Spring Framework/Spring Retry；
- 把 SNAPSHOT 当作已发布、已修复 CVE 的生产版本。

Spring 4.x 的单独失败不会阻断目标 5.3.x 发布，但不能通过删除测试掩盖 Spring 5.3 回归。

## 3. 已执行验收矩阵

| 场景 | JDK | Spring Framework | 坐标集合 | 结果 | 用途 |
| --- | --- | --- | --- | --- | --- |
| 默认产品链 | 真实 JDK 8 | NES 5.3.39 | 仅 NES | 328/328 tests，全绿；JaCoCo check 通过 | SNAPSHOT 产品链与 CVE 修复验证 |
| 官方 API 兼容 | 真实 JDK 8 | 官方 5.3.39 | 仅官方 | 328/328 tests，全绿；JaCoCo check 通过 | 兼容性证据，禁止部署 |
| Maven 消费者 | 真实 JDK 8 | NES 5.3.39 | 仅 NES | 2/2 tests，全绿 | 旧/新 setter、双命名 Bean、GAV/POM/import 验证 |
| Gradle 消费者 | Gradle 7.6.3 + JDK 8 | NES 5.3.39 | 仅 NES | 2/2 tests，全绿 | `mavenLocal()` 等价消费验证 |

两套 Spring 5.3 验证使用独立 `clean` invocation。官方依赖树不含 NES Framework；默认 NES 依赖树不含 `org.springframework:spring-*`；运行时依赖树不含 JaCoCo。

## 4. Spring 5.3 listener 兼容性结果

原问题是 `RetryConfiguration.afterPropertiesSet()` 在 Spring 5.3 生命周期中过早发现全局 `RetryListener`，导致默认 listener 未生效、`StatisticsListener` 无统计记录。当前实现已最小回移上游 commit `b33671239bf0b2b0efcd77a95bd7920f52425878` 的 Java 8/Spring 5.3 语义：

```text
afterPropertiesSet()
  └─ 建立 advisor/interceptor 和其他依赖
Spring 完成普通 singleton 实例化
  └─ afterSingletonsInstantiated() 发现、排序并设置全局 listener
```

新增 lifecycle 场景、既有 `vanilla`、显式 listener 覆盖和 circuit breaker statistics 均已通过；没有带入 Java 17、Spring 6 或 JUnit 5。

## 5. API 与行为兼容原则

- GAV 变化不要求消费者修改 Java import。
- 实现不得调用仅 Spring Framework 6 提供的 API。
- 新增公共 API、默认行为或异常变化必须在 design、User Manual 和 Release Notes 中说明。
- 上游 2.x 补丁 backport 到 1.3.x 时，只移植必要语义，不能顺带采用 Java 17、JUnit 5 或 Spring 6 结构。
- 既有 `RetryTemplate.setRetryContextCache(...)` 保持可编译，只替换普通有状态重试缓存；新增 `setCircuitBreakerRetryContextCache(...)` 独立配置断路器缓存。
- `MapRetryContextCache` 与 `SoftReferenceMapRetryContextCache` 的无参/单容量构造器默认使用有界访问顺序 LRU；新增 `(capacity, false)` 保留严格 fail-fast。
- 注解配置优先使用 `retryContextCache` 与 `circuitBreakerRetryContextCache` 两个约定 Bean 名；唯一未命名 Bean 仅回退普通缓存。

## 6. 正式 RELEASE 仍需满足

- 将已验证的 CVE-2026-41710 源码修复冻结到 RELEASE 候选；
- 最终 RELEASE POM 不含内部 SNAPSHOT；
- 使用 RELEASE 候选重新执行双矩阵、覆盖率、JAR 和消费者验证；
- Nexus 目标不存在检查、单独部署授权和远端重新下载验证完成；
- README、Quick Start、User Manual、Testing、Vulnerability Report 和 Release Notes 同步为真实 RELEASE 状态。
