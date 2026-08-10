# Spring Retry NES 发布说明

## 1.3.4-nes.patch.1（未发布）

> 状态：未发布，Nexus SNAPSHOT 已验证
>
> 最后更新：2026-08-10
>
> Release commit：尚未创建
>
> Git tag：尚未创建
> Nexus：SNAPSHOT 已部署并隔离验证；RELEASE 尚未部署

### 坐标

计划正式坐标：

```text
cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1
```

当前开发坐标：

```text
cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT
```

当前 SNAPSHOT 已通过本地 Maven install、Nexus deploy、隔离四件套和 JDK 8 远端消费者验证，并包含已验证的 CVE-2026-41710 源码修复；它尚未完成 RELEASE 远端闭环，不得作为生产安全版本。

### 当前 SNAPSHOT 已完成

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
| CVE-2026-41710 | 修复中 | 源码与 Nexus SNAPSHOT 已验证，待 RELEASE | 独立 release change、不可变 RELEASE 部署、远端重新下载/摘要/POM/依赖/消费者验证 |

listener 生命周期修复、GAV 重品牌、JaCoCo 或全量测试通过不能改变该状态。

### 当前发布阻断项

- RELEASE 版本切换、无 SNAPSHOT 依赖复核和 release commit 尚未执行；
- Nexus RELEASE 目标不存在检查、单独部署授权、远端重新下载和远端消费者验证尚不存在；
- Git annotated tag 尚未创建。

### 消费者迁移准备

1. 找出并排除官方 `org.springframework.retry:spring-retry`；
2. 联调阶段可使用已验证的 Nexus SNAPSHOT，生产升级等待目标 RELEASE 的 Nexus 远端验证；
3. 确保 Spring Framework 全部来自 NES `5.3.39-nes.patch.1`；
4. 保持现有 Java import；
5. 如自定义有状态缓存，同时提供 `retryContextCache` 与 `circuitBreakerRetryContextCache`，并评估 LRU 驱逐权衡；
6. 运行 dependency tree 和业务 smoke test；
7. 在消费者仓库同步 GAV、Quick Start、漏洞状态和自身 Release Notes。

正式发布后，本节必须替换为真实 release commit、tag、制品摘要、Nexus URL 和远端验证结果，不能保留计划性陈述冒充发布事实。
