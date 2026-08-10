## ADDED Requirements

### Requirement: Spring Framework 坐标选择必须参数化且可审计

POM SHALL 使用统一的 Framework groupId、artifactId 前缀和 version 属性构造 BOM、context、core、test、tx 坐标。默认属性 MUST 解析为 NES Framework `5.3.39-nes.patch.1`；官方兼容验证 MUST 通过一次 Maven invocation 完整覆盖三项属性为官方 Spring Framework 5.3.39。任何构建场景的 effective POM 和 dependency tree MUST 形成单一坐标集合。

#### Scenario: 默认生成 NES effective POM

- **WHEN** 使用 POM 默认属性生成 effective POM 和 dependency tree
- **THEN** 项目 GAV MUST 为 `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1-SNAPSHOT`，Framework BOM 与直接/传递依赖 MUST 全部使用 NES GAV，且不得出现正式 `org.springframework:spring-*` 依赖

#### Scenario: 命令行切换到官方 Framework

- **WHEN** Maven invocation 将 Framework groupId 覆盖为 `org.springframework`、artifactId 前缀覆盖为 `spring-`、version 覆盖为 `5.3.39`
- **THEN** Framework BOM 与直接/传递依赖 MUST 全部解析为官方 5.3.39，dependency tree MUST 不含 `cn.bjca.footstone.bpring:bjca-footstone-bpring-*`

#### Scenario: 官方验证生成本地制品

- **WHEN** 官方 Framework 兼容 invocation 执行 package 或 verify
- **THEN** 该产物 MUST 仅视为临时兼容性证据，MUST NOT install/deploy 为 NES RELEASE，最终验收 MUST 重新执行默认 NES clean build

#### Scenario: 参数遗漏造成坐标混用

- **WHEN** effective POM 或 dependency tree 因属性覆盖不完整而出现官方/NES 混合坐标
- **THEN** 构建验收 MUST 失败，维护者 MUST 修正统一属性或依赖声明，MUST NOT 通过 exclude 隐藏未经分析的混用

### Requirement: SNAPSHOT 发布元数据必须体现 BJCA fork 身份

开发 POM SHALL 使用目标 NES SNAPSHOT GAV，并 MUST 将 name、description、organization、URL、SCM、developer/issue metadata 指向公共 BJCA fork。POM MUST 保留 Apache 2.0 许可证，项目文档 MUST 保留官方 Spring Retry 来源和 NES backport 溯源，不得包含私服凭证或内部认证值。

#### Scenario: 检查 SNAPSHOT effective POM

- **WHEN** 生成默认 SNAPSHOT effective POM
- **THEN** 项目坐标、BJCA organization 和 `https://github.com/atbjca/spring-retry` SCM MUST 正确，许可证 MUST 仍为 Apache 2.0

#### Scenario: 检查 Java package

- **WHEN** 检查 SNAPSHOT JAR 内容和消费者编译
- **THEN** Java package MUST 继续为 `org.springframework.retry.*` 与 `org.springframework.classify.*`，消费者 MUST NOT 因 GAV 变化修改 Java import
