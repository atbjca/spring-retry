## ADDED Requirements

### Requirement: Spring Retry 使用固定 NES GAV

Spring Retry 正式发布坐标 SHALL 为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`；同一修订的开发版本 MUST 使用 `1.3.4-nes.patch.1-SNAPSHOT`。

#### Scenario: 开发构建

- **WHEN** GAV 实施 change 尚未进入正式 RELEASE
- **THEN** 生成的本地候选制品 MUST 使用 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`

#### Scenario: 正式发布

- **WHEN** 所有修复、测试和发布门禁通过
- **THEN** Nexus RELEASE 中的坐标 MUST 精确为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`

### Requirement: Java package 和公共 API 身份保持不变

GAV 重品牌 MUST NOT 修改 `org.springframework.retry.*`、`org.springframework.classify.*` package、公共类名、方法签名、注解名或业务 import。单模块项目 MUST NOT 无需求地增加独立 Retry BOM。

#### Scenario: 业务源码迁移

- **WHEN** 消费者从官方 Spring Retry 坐标切换到 NES 坐标
- **THEN** 消费者仅需修改构建坐标，不得要求修改 Java import 或类引用

#### Scenario: 提议修改 package 以规避 SCA

- **WHEN** 方案要求将 `org.springframework.retry` 改为其他 package
- **THEN** 方案 MUST 被拒绝，因为它破坏源码与反射兼容性

### Requirement: Spring Framework 依赖形成 NES GAV 闭环

默认发布元数据 MUST 将 Spring Framework BOM、context、core、test 和 tx 映射到 `cn.bjca.footstone.bpring` 下的 `bjca-footstone-bpring-*` 制品，版本为 `5.3.39-nes.patch.1`。依赖的 optional/test 语义 MUST 与上游保持一致。

#### Scenario: 生成默认 effective POM

- **WHEN** 使用默认 NES 发布配置生成 effective POM
- **THEN** Spring Framework 依赖 MUST 解析为 NES GAV，且不得出现作为正式依赖的 `org.springframework:*`

#### Scenario: 检查传递依赖

- **WHEN** 解析 `bjca-footstone-bpring-context`
- **THEN** 其 aop、beans、core、expression 等传递依赖 MUST 继续保持 NES GAV，不得回流官方坐标

### Requirement: 官方 Spring 兼容测试与发布元数据隔离

官方 Spring Framework 5.3.39 MUST 仅用于隔离兼容性测试，其坐标 MUST 通过命令行参数、临时消费者或独立测试夹具隔离，不能写入默认 RELEASE POM 或与 NES Spring 同时加载。

#### Scenario: 运行官方兼容测试

- **WHEN** 构建切换到官方 Spring Framework 5.3.39
- **THEN** 该执行 MUST 明确标记为兼容性验证，产物不得部署为 NES RELEASE

#### Scenario: 官方与 NES 依赖并存

- **WHEN** dependency tree 同时包含官方与 NES Spring Framework 或 Spring Retry JAR
- **THEN** 验证 MUST 失败并阻止发布

### Requirement: 发布元数据体现 BJCA fork 身份

后续 GAV 实施 MUST 更新项目 name、description、organization、URL、SCM 和 Maven 元数据，使其指向 BJCA fork，同时保留 Apache 2.0 许可证、上游来源和补丁溯源。

#### Scenario: 检查发布 POM

- **WHEN** 生成候选 RELEASE POM
- **THEN** POM MUST 包含 BJCA fork 的组织与 SCM 信息，并保留正确许可证声明

### Requirement: GAV 映射可供消费者审计

项目 MUST 维护 `doc/GAV_MAPPING.md`，列出项目本身及所有 Spring Framework 直接依赖的原始 GAV、NES GAV、版本、scope 和迁移方式。

#### Scenario: 坐标发生变化

- **WHEN** groupId、artifactId、版本或依赖映射被修改
- **THEN** 同一 change MUST 更新 GAV 映射、Quick Start、User Manual 和 Release Notes

### Requirement: GAV 重品牌不得充当漏洞修复证据

安全状态 MUST 依据代码、测试和制品内容判定，不能仅因 groupId/artifactId 改变而将 CVE 标记为“已修复”或“免疫”。

#### Scenario: 重品牌制品仍含漏洞代码

- **WHEN** NES 坐标 JAR 尚未包含 CVE 修复
- **THEN** 漏洞状态 MUST 保持“修复中”或其他真实状态，发布门禁 MUST 拒绝将其作为安全 RELEASE
