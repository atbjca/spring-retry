# Spring Retry NES 发布说明

## 1.3.4-nes.patch.1（已发布）

> 状态：Nexus 与 Git 已验证
>
> 最后更新：2026-08-17
>
> Release commit：`1f6dc7a9a02a1b19662c4f1a099fd7dd4069c291`
>
> Git tag：`v1.3.4-nes.patch.1`，annotated tag object `d96b2fa6a19b0c0642d45141eb2ffdcb01663d0f`
>
> Nexus：`http://192.168.131.36:8088/repository/releases/`，四件套已验证

### 坐标

正式坐标：

```text
cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1
```

该 RELEASE 已通过本地 Maven install、生成 POM 扫描、单次 Nexus deploy、四件套重新下载、SHA-256、JDK 8 RELEASE-only consumer 和远端 Git ref 验证。

### 已复用的开发验证

- 项目 GAV 切换为 BJCA NES SNAPSHOT，保留 `org.springframework.retry.*` 与 `org.springframework.classify.*` package/import；
- 编译与运行最低基线切换为真实 Java 8，代表性 class file major version 为 52；
- 默认 Framework 切换为 NES `5.3.39-nes.patch.1`；
- 通过三属性完整覆盖，官方 Spring Framework 5.3.39 独立 `clean verify` 全绿；
- 修复 Spring 5.3 下全局 `RetryListener`/`StatisticsListener` 过早发现问题，新增生命周期测试和既有 listener/statistics 回归均通过；
- 固定 JaCoCo Maven Plugin 0.8.15；CVE 目标类均超过 60%，全项目行覆盖率 86.5%；
- 生成并检查主 JAR、POM、sources JAR、javadoc JAR、effective POM、dependency tree 和 JDK 8 字节码；
- 回移 CVE-2026-41710 必要语义：普通缓存默认有界访问顺序 LRU、已有 key 可更新、严格模式保留 fail-fast、普通/断路器缓存隔离；
- 新增 `setCircuitBreakerRetryContextCache(...)`，支持 `retryContextCache` 与 `circuitBreakerRetryContextCache` 两个约定 Bean 名；
- 新增根 `Makefile`，提供 JDK 8 下的 `test`、NES/官方 `verify`、本地 SNAPSHOT 安装、OpenSpec strict 及 SNAPSHOT/RELEASE deploy；RELEASE 必须 `ALLOW_RELEASE_DEPLOY=true`，不包含 Git tag/push；
- 官方与 NES Framework 5.3.39 各 328 tests 全绿；Maven/JDK 8 与 Gradle 7.6.3/JDK 8 消费者 smoke test 各 2/2 通过，原 Java import 无需修改。
- 当前 SNAPSHOT 已完成完整 `make clean deploy`；隔离下载的 POM、主 JAR、sources、javadoc 与本地候选摘要一致，远端 JDK 8 消费者 1/1 通过。

### 兼容性影响

- Java 6/7 不在目标支持范围；
- Spring Framework 4.x 不在目标支持范围；
- Spring Framework 6.x API 不在目标支持范围；
- 消费者只修改 GAV 和依赖排除，不修改 Java import；
- 默认普通 cache 从满容量 fail-fast 改为 LRU；不能接受自动驱逐的消费者应使用 `(capacity, false)` 或自定义 cache；
- 旧的唯一未命名 cache Bean 升级后只回退普通重试，断路器如需自定义必须提供 `circuitBreakerRetryContextCache`；
- 官方/NES Spring Retry 或 Spring Framework 不能在同一 classpath 并存；
- 本地 SNAPSHOT 的默认产品链只使用 NES Framework `5.3.39-nes.patch.1`，官方 5.3.39 仅用于隔离兼容性验证。

### CVE 状态

| CVE | 主状态 | 当前阶段 | 目标处置 |
| --- | --- | --- | --- |
| CVE-2026-41710 | 已修复 | `1.3.4-nes.patch.1` Nexus/Git 闭环完成 | 下游采用该 RELEASE 并评估有状态重试缓存语义 |

listener 生命周期修复、GAV 重品牌、JaCoCo 或全量测试通过不能改变该状态。

### 发布验证

- `make install-local` 与实际 `make deploy ALLOW_RELEASE_DEPLOY=true` 均在 JDK 8、单线程 Maven 下成功，328 tests 全绿；
- POM SHA-256：`c32a3858422583fbf17fc0b21da35350f7e6db6c0af1179a15e867dbb66d1235`；
- 主 JAR SHA-256：`be3fe5aac3fc9dbe42668ef9ae76cd59f0f0e9b992a573f8b13966c76a1c9972`；
- sources SHA-256：`47160da016ba5acae219cd59a31013647465d15c408e7303f009b4953acedece`；
- javadoc SHA-256：`3e30f165af865d33521ea0cf3462c944b874ea8f6a3e150a1e53ba7d139aef77`；
- 隔离 consumer 使用全新 Maven local、禁用 SNAPSHOT 的 repository policy，1/1 测试通过；
- 远端 branch、tag object 和 peeled commit 已独立核验。

### 消费者迁移准备

1. 找出并排除官方 `org.springframework.retry:spring-retry`；
2. 使用已发布的 `1.3.4-nes.patch.1`，不要在生产依赖中保留旧 SNAPSHOT；
3. 确保 Spring Framework 全部来自 NES `5.3.39-nes.patch.1`；
4. 保持现有 Java import；
5. 如自定义有状态缓存，同时提供 `retryContextCache` 与 `circuitBreakerRetryContextCache`，并评估 LRU 驱逐权衡；
6. 运行 dependency tree 和业务 smoke test；
7. 在消费者仓库同步 GAV、Quick Start、漏洞状态和自身 Release Notes。

采用动作必须在消费者仓库中独立记录 dependency tree、业务 smoke test 和回滚方案。
