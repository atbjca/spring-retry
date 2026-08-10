# 维护治理与文档基线实施证据

## 1. 实施前门禁

- 检查日期：2026-08-06（Asia/Shanghai）
- 当前分支：`1.3.x-bjca-patch`
- 当前 HEAD：`4f104e175626674e929915bec218102662fdb280`
- tracked 修改：无
- staged 修改：无
- 既有未跟踪路径：`.codex/`、`.cursor/`、`openspec/`；均按原状保留，不删除、不覆盖
- 本 change 允许修改：`openspec/config.yaml`、`README.md`、`SECURITY.md`、`doc/` 以及本 change 自身工件和证据
- 本 change 禁止修改：`pom.xml`、`src/`、测试源码、Nexus、Git 历史和下游仓库
- 可恢复点：所有直接影响均为本地 Markdown 或声明式 OpenSpec 配置；未执行远端写入或不可逆操作

## 2. 当前构建事实

- 当前项目 GAV：`org.springframework.retry:spring-retry:1.3.5-SNAPSHOT`
- 当前 POM Java 级别：`1.6`
- 当前 POM Spring Framework：`4.3.29.RELEASE`
- 当前 README 声明：Java 1.7、Maven 3.0.5 及以上
- 目标状态将在永久文档中单独标记，不能覆盖或伪装上述当前事实

## 3. 本地工具发现

- PATH 中 Maven：`/Users/anan/dev/apache-maven-3.8.2/bin/mvn`，版本 3.8.2
- `~/dev` 中另有 Maven 3.2.2、3.5.4、3.6.3、3.9.6
- SDKMAN Java 候选包含 `8.0.472-amzn`、`8.0.482-kona`、11、17、21；当前 shell 使用 Java 17.0.17
- `~/dev` 中存在 Gradle 7.6.3、8.6、8.14.5；本仓库无 Gradle 构建，因此本 change 不选择或运行 Gradle

## 4. 验证记录

### 4.1 CVE-2026-41710 信息源复核

查询日期：2026-08-06。

| 信息源 | 复核结果 |
| --- | --- |
| Spring 官方公告 | 标题为 Cache Exhaustion in Stateful Retries leads to Denial of Service；MEDIUM；CVSS 3.1 向量 `AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:N/A:H`；2.0.13 为 OSS 修复，1.3.5 为 enterprise 修复 |
| GitHub Reviewed Advisory `GHSA-2827-2mxx-j8pv` | CVSS 5.9、CWE-770；描述受影响版本 2.0.0–2.0.12 和 1.3.0–1.3.4；1.3.x 没有公开 Maven first patched version；2026-07-29 更新 |
| NVD | CVE 状态为 Awaiting Analysis；CNA 数据给出 CVSS 5.9、CWE-770 和网络高复杂度可用性影响；2026-07-23 更新 |
| OSV | 与 GHSA 对齐，2.0.13 为 OSS fixed event，1.3.4 为 1.x last affected；2026-07-29 更新 |
| 上游 issue `#505` | 确认容量限制会阻止已有 cache entry 更新 |
| 上游 commit `6f351edae3d3575fffbde3c0f62fef963dacd152` | 引入普通状态缓存 LRU、已有 key 更新、普通/断路器缓存隔离和命名 Bean 选择；该 2.x 补丁不能不经适配直接复制到本 1.3.x 分支 |

信息差异：Spring 官方列出 enterprise `1.3.5` 修复，但 GitHub/OSV 没有可公开消费的 1.3.x first patched Maven 版本；NVD 尚处于 Awaiting Analysis。当前 POM 的 `1.3.5-SNAPSHOT` 不是该 enterprise 制品，必须以当前源码为准。

### 4.2 当前源码证据

- `MapRetryContextCache.java:37`：默认容量 4096。
- `MapRetryContextCache.java:77-83`：`map.size() >= capacity` 时在 `map.put` 前直接抛异常，既拒绝新 key，也拒绝容量满时更新已有 key。
- `RetryTemplate.java:95`：默认只有一个 `RetryContextCache`。
- `RetryTemplate.java:442-452`：失败状态写入同一缓存。
- `AnnotationAwareRetryOperationsInterceptor.java:93`、`:234-254`：普通有状态重试与断路器都使用同一个 `retryContextCache`。
- `RetryConfiguration.java:103-108`、`:133-140`：容器只查找唯一 `RetryContextCache` Bean，没有独立断路器缓存入口。

结论：当前源码具有公告描述的攻击路径，主状态必须保持“修复中”。

文档静态校验和 OpenSpec 最终校验将在本文件后续补充。

### 4.3 永久文档静态校验

- 必需文件检查：`README.md`、`SECURITY.md` 和 10 份 `doc/` 文档全部存在。
- Markdown 检查：12 份永久文档的相对链接和 fenced code block 配对全部通过。
- 语言检查：12 份永久文档均以中文为主要叙述语言。
- 敏感信息检查：13 个配置/文档文件未发现私钥、GitHub token、AWS access key、URL 内嵌凭证或字面量用户名/密码；Nexus 示例只使用 `${env.NEXUS_USERNAME}` 和 `${env.NEXUS_PASSWORD}`。
- GAV 检查：所有 NES Retry 版本仅为 `1.3.4-nes.patch.1` 或 `1.3.4-nes.patch.1-SNAPSHOT`，artifactId 均为 `bjca-footstone-bpring-retry`；NES Framework 版本均为 `5.3.39-nes.patch.1`。
- CVE 检查：1 份独立 CVE 文档、总览和 Release Notes 的主状态均为“修复中”；总览统计为“修复中 1、合计 1”，其余五种状态均为 0。
- `git diff --check`：通过。

### 4.4 OpenSpec 校验

以下严格校验均通过且无 issue：

```bash
openspec validate establish-spring-retry-maintenance-baseline \
  --type change --strict --json --no-interactive
openspec validate --all --strict --json --no-interactive
```

### 4.5 最终变更范围

- tracked 修改只有 `README.md`。
- 新增文件位于 `SECURITY.md`、`doc/`、`openspec/config.yaml` 和本 change 目录。
- `git status --short -- pom.xml src` 无输出，确认未修改 POM、Java 源码或测试源码。
- 未执行 Nexus、Git 历史或下游仓库写入。
- 未运行 Maven 测试或 JaCoCo：本 change 没有可执行逻辑、POM 或测试源码变更，覆盖率按批准设计标记为不适用；替代证据为上述静态校验。
- `.codex/` 与 `.cursor/` 的既有未跟踪内容保持原样，未纳入本 change 交付。
