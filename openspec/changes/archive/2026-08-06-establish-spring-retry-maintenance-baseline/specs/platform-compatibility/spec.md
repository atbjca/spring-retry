## ADDED Requirements

### Requirement: Java 8 是最低运行与编译基线

项目 SHALL 将 Java 8 作为最低支持版本，生产字节码目标 MUST 为 1.8。正式验证 MUST 使用本地可用的真实 JDK 8，不能仅依赖高版本 JDK 的 source/target 模拟。

#### Scenario: 编译正式候选制品

- **WHEN** 后续 change 构建可发布 JAR
- **THEN** 编译 MUST 使用 JDK 8 或经验证等价的 Java 8 toolchain，并验证生成字节码可在 JDK 8 运行

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

兼容性 change MUST 至少验证官方 Spring Framework 5.3.39；GAV 重品牌实施后，产品链验证 MUST 使用 NES Framework `5.3.39-nes.patch.1`。两套验证必须隔离，不能让官方与 NES Framework 同时进入同一 classpath。

#### Scenario: 官方 5.3.39 兼容性测试

- **WHEN** 验证 Spring Retry 对标准 Spring Framework 5.3 API 的兼容性
- **THEN** 测试 classpath MUST 只包含官方 5.3.39 Spring 制品并运行完整相关测试

#### Scenario: NES 产品链测试

- **WHEN** 验证待发布 NES Spring Retry
- **THEN** 测试 classpath MUST 只包含 NES Framework 5.3.39 制品，并验证无官方 Spring 类重复出现

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
