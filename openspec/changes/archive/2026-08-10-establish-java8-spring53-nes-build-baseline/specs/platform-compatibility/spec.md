## ADDED Requirements

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

## MODIFIED Requirements

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
