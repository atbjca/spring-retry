# Spring Retry 1.3 NES 维护分支

本仓库是 Spring Retry 1.3.x 的 NES 维护 fork，为 Spring Framework 5.3.x / Java 8 产品线提供可审计的兼容性、安全修复、GAV 重品牌和不可变发布流程。

> **`1.3.4-nes.patch.1` 已作为不可变 RELEASE 发布并完成 Nexus、RELEASE-only consumer 和 Git annotated tag 验证。** 发布 commit 为 `1f6dc7a9a02a1b19662c4f1a099fd7dd4069c291`，tag 为 `v1.3.4-nes.patch.1`。

## 当前状态

| 项目 | 当前事实 |
| --- | --- |
| 维护分支 | `1.3.x-bjca-patch` |
| RELEASE GAV | `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1` |
| Java | Java 8；代表性公共类 class file major version 52 |
| Spring Framework | 仅承诺 5.3.x；官方兼容基线 5.3.39 |
| 默认产品链 Framework | NES `5.3.39-nes.patch.1` |
| Java package | `org.springframework.retry.*`、`org.springframework.classify.*`，保持不变 |
| 构建系统 | Maven；Gradle 7.6.3 仅用于消费者 smoke test |
| 最近全量结果 | 官方 5.3.39 与默认 NES 均为 328 tests、0 failure、0 error、0 skipped |
| 发布状态 | Nexus RELEASE 四件套、checksum、RELEASE-only consumer、远端 branch/tag 均已验证 |

Spring Framework 4.x、Java 6/7 和 Spring Framework 6.x API 不在支持范围。

## 当前安全状态

CVE-2026-41710 主状态为“已修复”，修复版本为 `1.3.4-nes.patch.1`。普通缓存现为有界访问顺序 LRU，断路器使用独立严格容量缓存；目标 RELEASE 已完成远端资产和隔离 consumer 验证。

- [漏洞状态总览](doc/VULNERABILITY_REPORT.md)
- [CVE-2026-41710 独立文档](doc/CVE/CVE-2026-41710.md)
- [安全报告策略](SECURITY.md)

GAV 重品牌、listener 修复和全绿构建都不能替代 CVE 源码修复及其攻击路径验证。

## 开发构建入口

推荐使用根目录 Makefile；它会为子进程显式选择 JDK 8 和 `~/dev/apache-maven-3.8.2/bin/mvn`：

```bash
make help
make verify
```

隔离验证官方 Spring Framework 5.3.39：

```bash
make verify-official
```

可通过 `JAVA8_HOME`、`DEV_ROOT`、`MAVEN` 和 `MAVEN_FLAGS` 覆盖本地路径。`make install-local` 安装 POM 中的当前版本到本地 Maven repository；`make deploy` 根据版本自动选择 snapshots/releases，RELEASE 必须显式设置 `ALLOW_RELEASE_DEPLOY=true`。Makefile 不提供 Git tag/push，Gradle 仍只用于临时消费者，不是本仓库构建系统。

本次 RELEASE 使用以下受控命令完成了一次实际上传：

```bash
make deploy ALLOW_RELEASE_DEPLOY=true
```

官方 invocation 只用于兼容性证据，不得 install/deploy 为 NES 制品。默认构建可生成主 JAR、sources JAR、javadoc JAR 和 JaCoCo HTML/XML；RELEASE deploy 能力不替代独立 OpenSpec、不可变候选检查和再次授权。

## 文档

| 文档 | 用途 |
| --- | --- |
| [快速入门](doc/QUICK_START.md) | Maven/Gradle 消费、部署门禁、最小用例和依赖去重 |
| [用户手册](doc/USER_MANUAL.md) | 声明式/命令式、无状态/有状态、listener、断路器和缓存行为 |
| [维护要求](doc/REQUIREMENTS.md) | OpenSpec、审批、TDD、覆盖率、影响分析和安全设计 |
| [GAV 映射](doc/GAV_MAPPING.md) | 已发布 RELEASE 与两套 Framework 坐标 |
| [兼容性说明](doc/COMPATIBILITY.md) | Java/Spring/Maven 支持矩阵和非支持范围 |
| [测试指南](doc/TESTING.md) | RED/GREEN、双矩阵、JaCoCo、制品和消费者证据 |
| [漏洞状态总览](doc/VULNERABILITY_REPORT.md) | 六种 CVE 状态、统计和独立文档索引 |
| [发布指南](doc/RELEASE_GUIDE.md) | 本地门禁、Nexus、远端复验、tag 和不可变失败处理 |
| [发布说明](doc/RELEASE_NOTES.md) | `1.3.4-nes.patch.1` 的发布结果、制品摘要和 Git 证据 |
| [安全策略](SECURITY.md) | 支持范围、私密报告渠道和响应流程 |

## OpenSpec 流程

源码、测试、构建、依赖、GAV、文档和发布变更都必须经过：

```text
Explore → Proposal → Design/Impact Analysis → Specs → Tasks
        → 明确批准 → Apply/TDD → 验证与文档同步 → Archive
```

Java 生产代码必须先写失败测试，再实现最小修复。新增核心逻辑或复杂算法的覆盖率至少为 60%。

## 上游与许可证

本项目来源于 [Spring Retry](https://github.com/spring-attic/spring-retry)，继续保留上游 Java package、作者信息和 Apache License 2.0。NES fork 的发布元数据、补丁 commit、CVE 和验证证据必须保持可追溯。
