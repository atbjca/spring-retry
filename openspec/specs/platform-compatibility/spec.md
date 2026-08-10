# platform-compatibility Specification

## Purpose
TBD - created by archiving change establish-spring-retry-maintenance-baseline. Update Purpose after archive.
## Requirements
### Requirement: Java 8 是最低运行与编译基线

项目 SHALL 将 Java 8 作为最低支持版本，生产字节码目标 MUST 为 1.8（class file major version 52）。正式验证 MUST 使用本地可用的真实 JDK 8 执行编译、测试、覆盖率和打包，不能仅依赖高版本 JDK 的 source/target 模拟。

#### Scenario: 编译正式候选制品

- **WHEN** 后续 change 构建可发布 JAR
- **THEN** 编译 MUST 使用 JDK 8 或经验证等价的 Java 8 toolchain，并验证生成字节码可在 JDK 8 运行

#### Scenario: 检查 Java 8 字节码

- **WHEN** 对候选 JAR 中代表性公共类执行 `javap -verbose`
- **THEN** class file major version MUST 为 52，且最小 JDK 8 smoke test MUST 成功加载并调用制品

#### Scenario: 使用 JDK 11 或 17 进行辅助验证

- **WHEN** 维护者在高版本 JDK 上运行额外测试
- **THEN** 结果 MUST 仅作为兼容性补充，且 MUST NOT 替代 JDK 8 主基线

### Requirement: Spring Framework 5.3.x 是唯一受承诺的 Spring 主线

项目 SHALL 面向 Spring Framework 5.3.x 维护，不再承诺 Spring Framework 4.x。生产代码 MUST 避免依赖超出 5.3.x 合同的 Spring 6 API。

#### Scenario: Spring 4.x 测试失败

- **WHEN** 仅 Spring Framework 4.x 环境出现失败而 Spring 5.3 验收矩阵全部通过
- **THEN** 该失败 MUST 记录为非支持范围，且不得阻断 5.3.x 发布

#### Scenario: 实现需要 Spring 6 API

- **WHEN** 候选实现调用仅存在于 Spring Framework 6 的 API
- **THEN** 实现 MUST 被拒绝或改写为 Spring 5.3.x 可用方案

### Requirement: 兼容性验证覆盖官方与 NES Framework

兼容性 change MUST 在真实 JDK 8 上分别验证官方 Spring Framework 5.3.39 和 NES Framework `5.3.39-nes.patch.1`。默认 POM MUST 使用 NES Framework；官方验证 MUST 通过完整的 groupId、artifactId 前缀和 version 命令行覆盖执行。两套验证 MUST 使用独立的 clean Maven invocation，不能让官方与 NES Framework 同时进入同一 classpath，也不能复用上一套坐标编译的输出作为另一套验证证据。

#### Scenario: 官方 5.3.39 兼容性测试

- **WHEN** 验证 Spring Retry 对标准 Spring Framework 5.3 API 的兼容性
- **THEN** 测试 classpath MUST 只包含官方 5.3.39 Spring 制品、运行完整相关测试，并将该 invocation 标记为禁止部署的兼容性验证

#### Scenario: NES 产品链测试

- **WHEN** 验证待发布 NES Spring Retry
- **THEN** 测试 classpath MUST 只包含 NES Framework 5.3.39 制品，并验证无官方 Spring 类重复出现

#### Scenario: 切换验证矩阵

- **WHEN** 构建从官方坐标切换到 NES 坐标或从 NES 坐标切换到官方坐标
- **THEN** Maven invocation MUST 先清理上一套编译与测试输出，并为当前 effective POM、dependency tree 和测试结果生成独立证据

#### Scenario: 发现双份 Framework 坐标

- **WHEN** 任一 dependency tree 同时出现 `org.springframework:spring-*` 和 `cn.bjca.footstone.bpring:bjca-footstone-bpring-*`
- **THEN** 该兼容性场景 MUST 失败，且不得继续生成发布候选或把结果记录为全绿

### Requirement: Spring 5.3 测试基线必须全绿

后续兼容性或发布 change MUST 使 Spring Framework 5.3 验收测试不存在失败、错误或未批准跳过。现有全局 RetryListener 与 StatisticsListener 失败 MUST 被视为真实基线缺陷，不得通过删除、禁用或放宽断言规避。

#### Scenario: 监听器测试仍失败

- **WHEN** `EnableRetryWithListenersTests` 或 `CircuitBreakerInterceptorStatisticsTests` 在 Spring 5.3 下失败
- **THEN** 兼容性基线 MUST 判定为未恢复，后续 CVE RELEASE 不得开始

#### Scenario: 测试被跳过

- **WHEN** 候选构建新增或保留未经批准的 skipped test
- **THEN** 验收 MUST 失败并记录跳过原因

### Requirement: Maven 是本项目唯一构建系统

项目 SHALL 使用 Maven 构建，不要求 Gradle。构建命令 MUST 优先复用本地 `~/dev` 中合适的 Maven 或已缓存 Wrapper；依赖解析可使用用户 `~/.m2/settings.xml` 与本地 repository。

#### Scenario: Gradle 环境检查

- **WHEN** 执行本项目的编译、测试或发布
- **THEN** 不得因未选择 Gradle 版本而阻塞，因为仓库不包含 Gradle 构建

#### Scenario: Maven Wrapper 未缓存

- **WHEN** `./mvnw` 需要慢速外网下载而本地已有兼容 Maven
- **THEN** 维护者 MUST 使用本地 Maven，并在验证记录中注明具体版本

### Requirement: 支持矩阵文档与构建元数据一致

README、Compatibility、Quick Start、User Manual、POM 和发布说明中的 Java/Spring 支持声明 MUST 一致。目标尚未实施时 MUST 明确区分当前值与目标值。

#### Scenario: 文档与 POM 不一致

- **WHEN** POM 仍声明旧基线而文档描述目标 Java 8/Spring 5.3
- **THEN** 文档 MUST 标记为“目标状态，待后续 change 实施”，不得暗示当前制品已经满足

### Requirement: 全局 RetryListener 在 singleton 初始化完成后装配

`RetryConfiguration` MUST 在 Spring 完成普通 singleton 实例化后发现、排序并设置全局 `RetryListener`，且 MUST 在 ApplicationContext 就绪后的 `@Retryable` 与 `@CircuitBreaker` 调用中应用这些 listener。实现 MUST 使用 Spring Framework 5.3 可用的生命周期 API，MUST NOT 引入 Spring 6 API，也 MUST NOT 改变显式 listener 覆盖全局 listener 的既有语义。

#### Scenario: ApplicationContext 就绪后调用 retryable service

- **WHEN** Spring Framework 5.3 ApplicationContext 定义全局 `RetryListener` 和 `@Retryable` service，并完成 singleton 初始化
- **THEN** retryable 调用 MUST 执行该全局 listener，listener 回调次数和重试次数 MUST 符合既有合同

#### Scenario: StatisticsListener 参与断路器调用

- **WHEN** Spring Framework 5.3 ApplicationContext 使用 `StatisticsListener` 监听 `@CircuitBreaker` service
- **THEN** listener MUST 建立并更新统计记录，测试 MUST NOT 因统计记录为空而抛出 `NullPointerException`

#### Scenario: 显式 listener 覆盖全局 listener

- **WHEN** `@Retryable` 通过 `listeners` 属性指定一个 listener bean
- **THEN** 指定 listener MUST 按既有行为生效，未指定的全局 listener MUST NOT 被错误附加

#### Scenario: 回移上游生命周期修复

- **WHEN** 实施者参考上游提交 `b33671239bf0b2b0efcd77a95bd7920f52425878`
- **THEN** 回移 MUST 仅包含 Java 8/Spring 5.3 可用的 listener 延迟发现语义，MUST NOT 带入 Java 17、Spring 6 或其他无关结构变更

### Requirement: 双缓存配置入口必须保持 Java 8 与 Spring 5.3 兼容

安全 backport MUST 保持 `RetryContextCache` 接口和既有 `setRetryContextCache` 行为可编译，并 SHALL 提供与上游 1.3.5 同源的 `setCircuitBreakerRetryContextCache` 入口。实现 MUST 使用 Java 8 和 Spring Framework 5.3.x 可用 API，不得改变 `org.springframework.retry.*` package/import，也不得新增运行时第三方依赖。

#### Scenario: 既有命令式消费者重新编译

- **WHEN** Java 8 消费者继续只调用 `RetryTemplate.setRetryContextCache(...)`
- **THEN** 源码 MUST 无需修改即可编译运行，所提供缓存 MUST 只替换普通有状态重试缓存，断路器 MUST 保持独立默认缓存

#### Scenario: 消费者显式配置断路器缓存

- **WHEN** Java 8 消费者调用 `setCircuitBreakerRetryContextCache(...)`
- **THEN** 指定缓存 MUST 仅承载全局断路器上下文，普通有状态重试 MUST 继续使用其独立缓存

### Requirement: 注解配置必须按约定名称选择缓存 Bean

`RetryConfiguration` MUST 优先选择名为 `retryContextCache` 的 `RetryContextCache` Bean 供普通有状态重试使用，并选择名为 `circuitBreakerRetryContextCache` 的 Bean 供断路器使用。若只有一个未使用约定名称的 cache Bean，MUST 为兼容性回退到普通缓存；MUST NOT 将其同时隐式复用为断路器缓存。存在多个未命名且无约定名称的 cache Bean 时 MUST 使用各自安全默认值而不得任意选择。

#### Scenario: 两个约定名称 Bean

- **WHEN** Spring 5.3 ApplicationContext 同时定义 `retryContextCache` 与 `circuitBreakerRetryContextCache`
- **THEN** 注解式普通有状态重试和 `@CircuitBreaker` MUST 分别使用对应 Bean

#### Scenario: 唯一未命名缓存 Bean

- **WHEN** ApplicationContext 只有一个其他名称的 `RetryContextCache` Bean
- **THEN** 该 Bean MUST 继续应用于普通有状态重试，断路器 MUST 使用独立严格默认缓存

#### Scenario: 多个无约定名称缓存 Bean

- **WHEN** ApplicationContext 有多个 `RetryContextCache` Bean 且没有约定名称
- **THEN** 配置 MUST NOT 随机或按注册顺序选择，普通重试与断路器 MUST 使用各自安全默认缓存

### Requirement: Makefile 必须提供可审计的 Java 8 本地入口

仓库根 `Makefile` MUST 使用可覆盖变量定位 SDKMAN JDK 8 与 `~/dev` 中已验证的 Maven，MUST 提供 `help`、`clean`、`test`、`verify`、`verify-official`、`install-local`、`deploy` 和 `validate` target。`verify-official` MUST 同时覆盖官方 Spring Framework 的 groupId、artifact 前缀和版本；`install-local` MUST 只安装 SNAPSHOT 到本地 Maven repository。`deploy` MUST 同时支持 `-SNAPSHOT` 与 RELEASE，MUST 使用完整 Maven deploy 生命周期且 MUST NOT 跳过测试、覆盖率、Javadoc 或 classifier。SNAPSHOT MUST 校验 `${nexusSnapshotUrl}`；RELEASE MUST 校验 `${nexusReleaseUrl}` 且 MUST 要求调用方显式设置 `ALLOW_RELEASE_DEPLOY=true`。Makefile MUST NOT 提供 Git tag、push，也 MUST NOT 读取、打印或固化 Maven settings 凭证。

#### Scenario: 默认 NES 一键验证

- **WHEN** 维护者在已配置 JDK 8 和 Maven 的环境运行 `make verify`
- **THEN** Makefile MUST 使用默认 NES Framework 执行 `clean verify`，并 MUST 保留 Maven 测试、JaCoCo、Javadoc 和打包门禁

#### Scenario: 官方 Spring 5.3 隔离验证

- **WHEN** 维护者运行 `make verify-official`
- **THEN** Makefile MUST 使用官方 `org.springframework:spring-*:5.3.39` 三属性组合执行独立 `clean verify`，MUST NOT install 或 deploy NES 制品

#### Scenario: SNAPSHOT 部署选择 snapshots

- **WHEN** 维护者运行 `make deploy`
- **THEN** Makefile MUST 在版本以 `-SNAPSHOT` 结尾时确认 `${nexusSnapshotUrl}` 可由 settings 解析，再执行完整 deploy

#### Scenario: RELEASE 默认拒绝误发

- **WHEN** 项目版本不是 `-SNAPSHOT` 且维护者未设置 `ALLOW_RELEASE_DEPLOY=true`
- **THEN** `make deploy` MUST 在 Maven deploy 生命周期开始前 fail-fast

#### Scenario: RELEASE 显式确认后选择 releases

- **WHEN** 项目版本不是 `-SNAPSHOT`、`ALLOW_RELEASE_DEPLOY=true` 且 `${nexusReleaseUrl}` 可由 settings 解析
- **THEN** `make deploy` MUST 允许 Maven 完整 deploy 生命周期使用 repository id `releases`；该能力 MUST NOT 代替实际发布 change、目标不存在检查或部署授权

#### Scenario: Git 发布操作不可由当前 Makefile 触发

- **WHEN** 维护者检查帮助、目标列表和命令展开
- **THEN** Makefile MUST NOT 暴露 tag/push target，也 MUST NOT 在未显式确认时允许 RELEASE deploy

### Requirement: Nexus SNAPSHOT 配置必须属性化并完成远端验证

POM `distributionManagement` SHALL 使用 repository id `releases`/`snapshots` 与 `${nexusReleaseUrl}`/`${nexusSnapshotUrl}` 属性，不得包含真实内部 URL或认证值。本 change 获准实际部署的 GAV MUST 仅为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`；部署后 MUST 从隔离本地 repository 重新解析远端 POM、主 JAR、sources 和 javadoc，并验证 Java 8、修复 API、NES-only 依赖与制品内容。RELEASE deploy 能力 SHALL 可用，但本 change MUST NOT 实际写入 RELEASE repository。

#### Scenario: 当前 SNAPSHOT 部署成功

- **WHEN** `make deploy` 完成且 Nexus 返回成功
- **THEN** 隔离消费者 MUST 能强制更新解析同一 SNAPSHOT GAV，四件套、摘要、字节码、缓存修复类/API 和 dependency tree MUST 与本地批准候选一致

#### Scenario: SNAPSHOT 已部署但 RELEASE 尚未发布

- **WHEN** Nexus SNAPSHOT 远端验证完成而目标 RELEASE 尚不存在
- **THEN** CVE 主状态 MUST 保持“修复中”，阶段 SHALL 为“源码与 Nexus SNAPSHOT 已验证，待 RELEASE”
