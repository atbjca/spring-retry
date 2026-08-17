# component-release Specification

## Purpose
TBD - created by archiving change establish-spring-retry-maintenance-baseline. Update Purpose after archive.
## Requirements
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

### Requirement: Exclusive and safe component preparation

The release process MUST assign one active owner to `spring-retry-1.3` and MUST inspect branch, origin, tracked and untracked changes, and active OpenSpec changes before modifying `spring-retry-1.3`.

#### Scenario: Worktree is safe
- **WHEN** the owner lease is active and all tracked changes are covered by the approved release change
- **THEN** preparation may update the component release version and dependencies

#### Scenario: Unrelated work exists
- **WHEN** unrelated tracked changes or another active owner are detected
- **THEN** preparation stops without cleaning, discarding, staging, or committing that work

### Requirement: Complete internal RELEASE metadata

Every generated RELEASE POM or BOM for `1.3.4-nes.patch.1` MUST use approved RELEASE versions for internal `cn.bjca.footstone` dependencies, parents, imported BOMs, and plugins.

#### Scenario: Generated metadata is clean
- **WHEN** the complete local publication set contains no internal version ending in `-SNAPSHOT`
- **THEN** the metadata gate passes

#### Scenario: Internal SNAPSHOT remains
- **WHEN** any generated publication contains an internal SNAPSHOT reference
- **THEN** local verification fails and deploy is forbidden

### Requirement: Proportional local verification

The component MUST have commit-correlated evidence for its approved build and relevant tests, and MUST complete release-version local publication or effective-POM generation, POM scan, and representative consumer validation against the release commit. Existing development build/test evidence MAY be reused when its commit is an ancestor and the intervening diff does not invalidate its coverage.

All `make`, Gradle, and Maven commands MUST run while this component exclusively owns the coordinator-controlled workspace build slot. A successful build MAY also satisfy the test gate only when it executed the complete required test suite without exclusions.

#### Scenario: All local gates pass
- **WHEN** `make verify`, `make test`, `make install-local`, the metadata scan, and consumer validation succeed
- **THEN** commands and evidence are recorded and the component may become `locally-verified`

#### Scenario: A local gate fails
- **WHEN** a required command fails or is skipped without an approved exception
- **THEN** the component cannot be committed for release or deployed

#### Scenario: Development verification is reusable
- **WHEN** successful build/test evidence identifies an ancestor commit and no intervening production source, test source, or build-logic change invalidates it
- **THEN** the evidence and diff assessment satisfy that coverage without rerunning the command

#### Scenario: Another component owns the build slot
- **WHEN** this component is ready to run a build, test, local publication, consumer, or deploy command
- **THEN** it waits until the coordinator assigns the global build slot

### Requirement: Auditable release commit and documentation

The component SHALL have one dedicated release commit containing `1.3.4-nes.patch.1`, internal RELEASE dependency updates, `README.md`, `doc/GAV_MAPPING.md`, `doc/RELEASE_NOTES.md`, `doc/RELEASE_GUIDE.md`, `doc/TESTING.md`, approved exclusions, and no unrelated source changes.

#### Scenario: Release diff is approved
- **WHEN** the staged diff matches this OpenSpec and all local gates pass
- **THEN** the release commit SHA is recorded in the central manifest

#### Scenario: Documentation or diff is incomplete
- **WHEN** component documentation still presents the target as SNAPSHOT or the diff contains unapproved files
- **THEN** release commit creation is blocked

### Requirement: Automatic engineering compliance review

The release process MUST review license permissions and obligations, LICENSE/NOTICE/SPDX/source delivery, attribution and endorsement language, distribution boundary, and explicit exclusions. It MUST record the review result and MUST NOT require a ceremonial user confirmation when no substantive blocker exists.

#### Scenario: No substantive compliance blocker exists
- **WHEN** the approved scope satisfies the recorded license, notice, source, attribution, boundary, and exclusion obligations
- **THEN** the review evidence is recorded and release preparation may continue without requesting a fixed legal/compliance acknowledgement

#### Scenario: A substantive risk remains unresolved
- **WHEN** licensing, trademark, source delivery, external distribution boundary, or another material compliance condition is unclear or unsatisfied
- **THEN** the coordinator explains the concrete risk and blocks publication until it is resolved or explicitly authorized by the appropriate decision maker

### Requirement: Exact Maven credential routing

Before Maven deployment, every `distributionManagement` repository ID MUST have a matching non-empty server ID in the selected user settings, and the intended repository URL/profile MUST be active. Credential values MUST remain secret.

#### Scenario: RELEASE server mapping is valid
- **WHEN** the RELEASE repository ID exactly matches a configured settings server and the intended release URL is active
- **THEN** credential-routing preflight passes without printing username or password values

#### Scenario: Repository and server IDs differ
- **WHEN** the POM uses `releases` but settings only defines another ID such as `snapshots`
- **THEN** deployment is blocked before upload because Maven will not bind those credentials to the RELEASE repository

### Requirement: Target absence before immutable deployment

Immediately before deployment, the coordinator MUST verify that every GAV discovered in the local publication set is absent from Nexus RELEASE.

#### Scenario: Complete target set is absent
- **WHEN** no POM, binary, checksum, metadata, or partial module asset exists for `1.3.4-nes.patch.1`
- **THEN** the coordinator may authorize the single deploy after reviewing all other gates

#### Scenario: Any target asset exists
- **WHEN** Nexus contains any complete or partial target-version asset
- **THEN** deploy is blocked and the existing assets are recorded for investigation

### Requirement: Coordinator-controlled deployment and remote verification

Only the coordinating main session SHALL execute `make deploy`, and only after explicit execution confirmation. After deployment, it MUST download and verify representative POM and binary assets, metadata, checksums, and RELEASE-only consumer resolution.

#### Scenario: Remote release is valid
- **WHEN** all expected assets are downloadable, metadata is RELEASE-only, checksums are recorded, and the consumer smoke test passes
- **THEN** the component advances to `nexus-verified`

#### Scenario: Publication is incomplete or uncertain
- **WHEN** expected assets are missing, inconsistent, or only partially present
- **THEN** the component becomes `partial-failure`, is not redeployed at the same version, and cannot be tagged

### Requirement: Exact annotated release tag

After Nexus verification, the coordinator MUST create annotated tag `v1.3.4-nes.patch.1` on the exact release commit used to build and deploy `1.3.4-nes.patch.1`.

The coordinator MUST retain CLI TAG/PUSH confirmation tokens as internal execution safeguards and MUST NOT require the user to echo those tokens after the release and remote-verification gates have passed.

#### Scenario: Tag and push are valid
- **WHEN** the annotated tag points to the recorded release commit and both commit and tag are verified on `origin`
- **THEN** tag object IDs and remote evidence are recorded

#### Scenario: Git finalization gates pass
- **WHEN** Nexus is verified, the release commit and target tag are unambiguous, and no Git mismatch exists
- **THEN** the coordinating session previews, token-confirms, and executes tag and push without requesting a ceremonial user acknowledgement

#### Scenario: Nexus is not verified
- **WHEN** remote artifact verification is incomplete or failed
- **THEN** no release tag is created or pushed

### Requirement: Explicit exclusions

The release process MUST honor and verify these exclusions: none.

#### Scenario: Exclusions are honored
- **WHEN** local publications, deploy tasks, and remote assets omit every excluded item and no included POM depends on it
- **THEN** exclusion verification passes

#### Scenario: Excluded content is present
- **WHEN** an excluded module is published or referenced by an included RELEASE POM
- **THEN** finalization fails and the immutable failure procedure applies

### Requirement: Evidence-backed documentation and archive

The component OpenSpec MUST remain active until Nexus assets, remote Git references, component documentation, and the central run manifest agree.

#### Scenario: Completion evidence agrees
- **WHEN** the component is `nexus-verified`, tagged, remotely verified, documented, and reconciled in the manifest
- **THEN** the OpenSpec change may be archived and its archive path recorded

#### Scenario: Evidence is missing or mismatched
- **WHEN** any required Git, Nexus, documentation, or manifest evidence is absent or inconsistent
- **THEN** archive is blocked

### Requirement: Immutable partial-failure handling

Existing RELEASE assets MUST NOT be overwritten, deleted, or blindly redeployed.

#### Scenario: Partial assets exist
- **WHEN** a failed or interrupted deployment leaves any target-version asset in Nexus
- **THEN** the component records `partial-failure` and requires a newly approved NES patch version

#### Scenario: Nexus succeeded but Git push failed
- **WHEN** Nexus verification is complete and only commit or tag push failed
- **THEN** the coordinator retries only the Git operation without redeploying
