# 快速入门

> **`1.3.4-nes.patch.1` 已完成 CVE-2026-41710 修复、Nexus RELEASE 四件套、checksum、隔离 consumer 和 Git tag 验证。** 生产消费者应使用下述 RELEASE 坐标。

## 1. 前置条件

- Java 8；
- Maven 3.8.2；
- Spring Framework 5.3.x，默认产品链为 NES `5.3.39-nes.patch.1`；
- Gradle 消费者示例使用 7.6.3；
- 能访问组织批准的 Nexus `maven-public`/SNAPSHOT/RELEASE 仓库；
- classpath 中只能保留一套 Spring Retry 和一套 Spring Framework。

本仓库使用 Maven 构建。下面的 Gradle 内容只面向消费者，不表示本仓库需要 Gradle。

## 2. Nexus 配置示例

仓库 URL 和凭证必须来自本机 Maven settings 或组织配置，不得提交真实值。示例：

```xml
<settings>
  <servers>
    <server>
      <id>snapshots</id>
      <username>${env.NEXUS_USERNAME}</username>
      <password>${env.NEXUS_PASSWORD}</password>
    </server>
    <server>
      <id>releases</id>
      <username>${env.NEXUS_USERNAME}</username>
      <password>${env.NEXUS_PASSWORD}</password>
    </server>
  </servers>
</settings>
```

项目仓库示例使用不可解析的占位域名：

```xml
<repository>
  <id>bjca-nexus</id>
  <url>https://nexus.example.invalid/repository/maven-public/</url>
</repository>
```

## 3. Maven 消费坐标

当前 RELEASE：

```xml
<dependency>
  <groupId>cn.bjca.footstone.bpring.retry</groupId>
  <artifactId>bjca-footstone-bpring-retry</artifactId>
  <version>1.3.4-nes.patch.1</version>
</dependency>
```

该 RELEASE 已从 Nexus 隔离重新解析并验证。需要只使用本地 Maven repository 时，可在本仓库执行：

```bash
make install-local
```

消费者可以通过组织批准的 Nexus 聚合仓库或 Maven local 解析。生产升级仍须在消费者仓库中排除官方坐标并运行自身 smoke test。

如果应用尚未通过其他组件提供 Spring AOP，还需要同一套 NES Framework AOP：

```xml
<dependency>
  <groupId>cn.bjca.footstone.bpring</groupId>
  <artifactId>bjca-footstone-bpring-aop</artifactId>
  <version>5.3.39-nes.patch.1</version>
</dependency>
```

## 4. Gradle 7.6.3 消费坐标

RELEASE 可从组织 Nexus 解析；只有本地开发需要时才保留 `mavenLocal()`：

```groovy
repositories {
    mavenLocal()
    maven {
        url = uri("https://nexus.example.invalid/repository/maven-public/")
        credentials {
            username = providers.environmentVariable("NEXUS_USERNAME").orNull
            password = providers.environmentVariable("NEXUS_PASSWORD").orNull
        }
    }
}

dependencies {
    implementation "cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1"
    implementation "cn.bjca.footstone.bpring:bjca-footstone-bpring-aop:5.3.39-nes.patch.1"
}
```

本机验证命令：

```bash
JAVA_HOME=/Users/anan/.sdkman/candidates/java/8.0.472-amzn \
  /Users/anan/dev/gradle-7.6.3/bin/gradle --no-daemon test
```

生产消费者应移除不再需要的 `mavenLocal()`，确保从组织 Nexus 解析已验证 RELEASE。

## 5. 排除官方 Spring Retry 与 Framework

如果某个依赖传递引入官方制品，在实际引入点增加排除。以下为 Maven 语法模板：

```xml
<dependency>
  <groupId>实际上游依赖的 groupId</groupId>
  <artifactId>实际上游依赖的 artifactId</artifactId>
  <version>实际版本</version>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.retry</groupId>
      <artifactId>spring-retry</artifactId>
    </exclusion>
    <exclusion>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

上例的 Framework exclusion 只是语法模板。必须根据实际 dependency tree 在真正引入点排除 `spring-context`、`spring-core` 或其他官方模块，不能机械复制后遗漏其他路径。

Gradle 语法：

```groovy
implementation("实际上游依赖的 groupId:实际上游依赖的 artifactId:实际版本") {
    exclude group: "org.springframework.retry", module: "spring-retry"
    exclude group: "org.springframework", module: "spring-context"
}
```

不要同时保留：

```text
org.springframework.retry:spring-retry
cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry
```

也不要同时保留同名 package 的：

```text
org.springframework:spring-*
cn.bjca.footstone.bpring:bjca-footstone-bpring-*
```

## 6. 最小声明式示例

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Configuration
@EnableRetry
class RetryConfiguration {
}

@Service
class RemoteService {

    @Retryable(
            value = RemoteCallException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    public String invoke() {
        // 调用可能发生瞬时失败的远程服务
        throw new RemoteCallException();
    }

    @Recover
    public String recover(RemoteCallException exception) {
        // 所有重试耗尽后的降级结果
        return "fallback";
    }
}

class RemoteCallException extends RuntimeException {
}
```

注意：

- 被代理的方法通常应为可代理的非 `private` 方法；
- 同一个对象内部的自调用不会经过 Spring AOP 代理；
- 默认是无状态重试，默认最多尝试 3 次；
- `@Recover` 参数应与目标异常和返回值匹配。

## 7. 最小命令式示例

```java
RetryTemplate template = RetryTemplate.builder()
        .maxAttempts(3)
        .fixedBackoff(1000)
        .retryOn(RemoteCallException.class)
        .build();

String result = template.execute(context -> remoteService.invoke());
```

## 8. 有状态缓存安全配置

普通有状态重试默认使用容量 4096 的访问顺序 LRU。容量满时会驱逐最久未访问条目、记录不含业务 key 的固定 WARN，并继续接受新 key；已有 key 更新不会因容量已满而失败：

```java
RetryTemplate template = new RetryTemplate();
template.setRetryContextCache(new MapRetryContextCache(1024));
```

如果业务不能接受自动驱逐，可显式使用严格模式；新 key 在满容量时会抛 `RetryCacheCapacityExceededException`：

```java
template.setRetryContextCache(new MapRetryContextCache(1024, false));
```

软引用缓存提供相同的 LRU/严格容量选择：

```java
template.setRetryContextCache(new SoftReferenceMapRetryContextCache(1024));
```

普通重试和断路器应使用独立缓存。命令式配置：

```java
template.setRetryContextCache(new MapRetryContextCache(1024));
template.setCircuitBreakerRetryContextCache(new MapRetryContextCache(1024, false));
```

注解式配置使用两个约定名称：

```java
@Bean(name = "retryContextCache")
public RetryContextCache retryContextCache() {
    return new MapRetryContextCache(1024);
}

@Bean(name = "circuitBreakerRetryContextCache")
public RetryContextCache circuitBreakerRetryContextCache() {
    return new MapRetryContextCache(1024, false);
}
```

只有一个未使用约定名称的 `RetryContextCache` Bean 时，它仅为兼容性回退给普通重试；断路器继续使用独立严格默认缓存。两类缓存即使使用相同 raw key，也会按 retry policy/context 隔离读取、更新和清理。

迁移前应评估 LRU 提前丢失旧重试状态的业务影响，并监控 `AbstractMapRetryContextCache` 的固定 WARN 与 `RetryCacheCapacityExceededException`。未发布前可回退源码 change；正式发布后必须通过新 NES patch 版本回滚，不能覆盖既有 RELEASE。

## 9. 依赖树验证

Maven：

```bash
mvn dependency:tree -Dverbose -DoutputFile=target/dependency-tree.txt
rg "spring-retry|bjca-footstone-bpring-retry" target/dependency-tree.txt
rg "org.springframework:|cn.bjca.footstone.bpring:" target/dependency-tree.txt
```

Gradle 消费者：

```bash
/Users/anan/dev/gradle-7.6.3/bin/gradle dependencies --configuration runtimeClasspath
```

验收结果必须满足：

- 只有 NES Retry 或隔离的官方 Retry，不同时存在；
- 产品链只使用 NES Framework `5.3.39-nes.patch.1`；
- 官方 5.3.39 只出现在独立兼容性验证；
- 不存在内部 RELEASE 对 SNAPSHOT 的引用。

## 10. 当前仓库构建与官方隔离验证

推荐使用 Makefile 运行默认 NES 产品链：

```bash
make verify
```

官方 Spring Framework 5.3.39 隔离验证：

```bash
make verify-official
```

其他入口：

```bash
make test
make install-local
make clean deploy
make validate OPEN_SPEC_CHANGE=remediate-cve-2026-41710-stateful-cache-exhaustion
```

当前版本以 `-SNAPSHOT` 结尾时，`make deploy` 校验 `nexusSnapshotUrl` 并使用 `snapshots`。RELEASE 版本默认拒绝部署；只有独立 release change 已完成全部前置检查并再次获得授权时，才可执行：

```bash
make deploy ALLOW_RELEASE_DEPLOY=true
```

此时 Makefile 校验 `nexusReleaseUrl`，Maven 根据非 SNAPSHOT 版本选择 `releases`。默认路径可用 `JAVA8_HOME`、`DEV_ROOT`、`MAVEN`、`MAVEN_FLAGS` 覆盖。Makefile 不提供 Git tag/push；官方 invocation 只用于兼容性证据，禁止 install/deploy。测试、编译和 JaCoCo 产物使用默认 `target/` 路径，Gradle 仍仅用于临时消费者。

## 11. 继续阅读

- [用户手册](USER_MANUAL.md)
- [GAV 映射](GAV_MAPPING.md)
- [兼容性说明](COMPATIBILITY.md)
- [测试指南](TESTING.md)
- [漏洞状态总览](VULNERABILITY_REPORT.md)
- [发布指南](RELEASE_GUIDE.md)
