## ADDED Requirements

### Requirement: RELEASE 必须依赖已完成的前置 change

Spring Retry RELEASE change MUST 在维护基线、Spring 5.3/GAV 构建基线和适用 CVE remediation 全部通过验证并归档后开始。不得将未完成任务或 SNAPSHOT 证据带入正式发布。

#### Scenario: CVE 仍处于实现或待验证阶段

- **WHEN** CVE-2026-41710 状态仍为“修复中”且不是单纯待远端发布阶段
- **THEN** RELEASE change MUST 被阻止

#### Scenario: 兼容性测试仍失败

- **WHEN** Spring 5.3 全量测试存在失败、错误或未批准跳过
- **THEN** 不得生成或部署 RELEASE

### Requirement: 发布版本和制品集固定

首个正式 NES Spring Retry 版本 MUST 使用 `1.3.4-nes.patch.1`，并生成主 JAR、POM、sources JAR、javadoc JAR及仓库要求的校验文件。所有制品 MUST 来自同一 Git commit 和同一次候选构建。

#### Scenario: 候选制品版本不一致

- **WHEN** 任一 JAR、POM 或 classifier 使用不同版本或 commit
- **THEN** 本地发布门禁 MUST 失败并重新生成完整候选集

### Requirement: 本地发布验证完整且可重复

发布前 MUST 使用批准的 JDK 8 与 Maven 执行 clean build、全量测试、覆盖率、package/install、effective POM、dependency tree、JAR manifest/META-INF 和消费者 smoke test，并记录命令、版本、commit 和结果。

#### Scenario: 本地 Maven 可用

- **WHEN** `~/dev` 中存在兼容 Maven 且 Wrapper 需要额外下载
- **THEN** 发布验证 MUST 使用已确认的本地 Maven 版本并记录绝对路径或版本信息

#### Scenario: 任一本地门禁失败

- **WHEN** 编译、测试、覆盖率、POM 扫描、依赖树或消费者验证失败
- **THEN** RELEASE 状态 MUST 保持未就绪，不得部署

### Requirement: RELEASE 元数据不得包含 SNAPSHOT 或官方坐标回流

候选 RELEASE POM 及其 NES Spring Framework 依赖链 MUST 不含内部 `-SNAPSHOT`，默认 dependency tree MUST 不含官方 Spring Retry 或 Spring Framework 坐标，也不得包含官方/NES 重复类。

#### Scenario: effective POM 含内部 SNAPSHOT

- **WHEN** 扫描发现任何 `cn.bjca.footstone` 依赖版本以 `-SNAPSHOT` 结尾
- **THEN** 部署 MUST 被拒绝

#### Scenario: dependency tree 含双份 Spring Retry

- **WHEN** 消费者同时解析官方 `spring-retry` 和 NES Retry
- **THEN** smoke test MUST 失败并要求修正 BOM、exclude 或直接依赖

### Requirement: Nexus 部署需要目标不存在和明确授权

部署前 MUST 对本地候选集中所有目标 GAV 执行 Nexus RELEASE 不存在检查，并在确认所有其他门禁通过后取得用户单独明确授权。凭证只能从用户 Maven settings 或批准的环境读取。

#### Scenario: 目标版本不存在

- **WHEN** Nexus 未发现该 GAV 的 POM、JAR、校验或部分资产且用户批准部署
- **THEN** 协调者才可以执行一次部署

#### Scenario: 发现部分或完整资产

- **WHEN** Nexus 已存在目标版本的任何资产
- **THEN** 部署 MUST 停止，不得覆盖或删除，并记录调查结果

### Requirement: 远端制品必须重新下载验证

部署完成后 MUST 从 Nexus 重新下载 POM、主 JAR、sources、javadoc 和校验文件，验证大小、摘要、GAV、依赖、修复代码特征及干净消费者解析。仅 Maven 命令成功不足以判定发布完成。

#### Scenario: 远端资产完整一致

- **WHEN** 所有资产可下载、摘要一致、POM 无 SNAPSHOT/官方回流且消费者 smoke test 通过
- **THEN** RELEASE 才能标记为 Nexus 已验证

#### Scenario: 远端资产不完整

- **WHEN** 部署后缺少任一必需资产或内容不一致
- **THEN** 发布 MUST 标记为 partial-failure，禁止对同版本重新部署

### Requirement: Git tag 必须绑定已验证制品的精确 commit

Nexus 验证完成后，项目 MUST 在生成并部署该制品的精确 commit 上创建 annotated tag，并验证远端 branch/tag 指向。Nexus 未验证前不得创建发布完成标签。

#### Scenario: Nexus 成功但 Git push 暂时失败

- **WHEN** 制品已验证而仅 commit/tag push 失败
- **THEN** 维护者 MUST 只重试 Git 操作，不得重新部署 Nexus 制品

### Requirement: 发布失败使用不可变修订策略

已进入 Nexus RELEASE 的资产 MUST NOT 被删除、覆盖或以相同版本盲目重发。任何无法修复的部分发布或制品缺陷 MUST 使用新的 `-nes.patch.N` 版本并创建新的 OpenSpec change。

#### Scenario: 发布后发现制品缺陷

- **WHEN** `1.3.4-nes.patch.1` 已存在但验证发现缺陷
- **THEN** 修复 MUST 规划为新的 patch 版本，原制品保留并在文档中标记状态

### Requirement: 下游采用使用独立 OpenSpec 和消费者验证

Spring Boot 2.7、Spring Kafka 2.9 及其他维护仓库 MUST 分别建立独立 OpenSpec change 切换到 NES Retry GAV，更新其 BOM/依赖、漏洞文档和 Quick Start，并验证无官方/NES 双份 classpath。

#### Scenario: Spring Kafka 替换两处 Retry 依赖

- **WHEN** Spring Kafka 2.9 采用 NES Retry
- **THEN** `spring-kafka` 与 `spring-kafka-test` 的依赖替换、exclude 规则和相关 CVE 状态 MUST 在其自身 change 中验证

#### Scenario: Spring Boot BOM 采用新坐标

- **WHEN** Spring Boot 2.7 管理 NES Retry
- **THEN** BOM MUST 发布新 group/artifact/version 映射，并通过 Maven 与 Gradle 消费者测试
