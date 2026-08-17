# Spring Retry NES GAV 映射

> 状态：POM 已冻结为 `1.3.4-nes.patch.1` RELEASE 候选；NES Spring Framework `5.3.39-nes.patch.1` 上游已在 Nexus RELEASE 验证，当前组件尚未部署。

目标仓库：`http://192.168.131.36:8088/repository/releases/`。

## 1. 项目坐标

| 用途 | GroupId | ArtifactId | Version | 当前可用性 |
| --- | --- | --- | --- | --- |
| RELEASE 候选 | `cn.bjca.footstone.bpring.retry` | `bjca-footstone-bpring-retry` | `1.3.4-nes.patch.1` | 本地 install、POM 扫描和 consumer 已通过；Nexus RELEASE 验证完成前不得作为已发布坐标使用 |
| 原官方坐标 | `org.springframework.retry` | `spring-retry` | 1.3.x | 仅用于识别上游来源和消费者排除，不再是本 fork 的项目 GAV |

项目为单模块，不创建独立 Retry BOM。

## 2. Java 身份保持规则

GAV 重品牌只改变 Maven 制品身份，不改变：

- `org.springframework.retry.*`
- `org.springframework.classify.*`
- 公共类名、方法签名和注解名
- 消费者 Java import
- Apache 2.0 许可证和上游来源

主 JAR 与 Maven/Gradle 消费者已验证这些 package/import 保持不变。

## 3. Spring Framework 坐标参数化

POM 使用三项统一属性构造 BOM、context、core、test 和 tx 坐标：

| 属性 | 默认 NES 值 | 官方兼容值 |
| --- | --- | --- |
| `spring.framework.group-id` | `cn.bjca.footstone.bpring` | `org.springframework` |
| `spring.framework.artifact-prefix` | `bjca-footstone-bpring-` | `spring-` |
| `spring.framework.version` | `5.3.39-nes.patch.1` | `5.3.39` |

默认直接依赖映射：

| 用途 | 默认 NES GAV | Scope/语义 |
| --- | --- | --- |
| Framework BOM | `cn.bjca.footstone.bpring:bjca-footstone-bpring-framework-bom:5.3.39-nes.patch.1` | `import` |
| Context | `cn.bjca.footstone.bpring:bjca-footstone-bpring-context:5.3.39-nes.patch.1` | `compile`、`optional=true` |
| Core | `cn.bjca.footstone.bpring:bjca-footstone-bpring-core:5.3.39-nes.patch.1` | `compile`、`optional=true` |
| Test | `cn.bjca.footstone.bpring:bjca-footstone-bpring-test:5.3.39-nes.patch.1` | `test` |
| Transactions | `cn.bjca.footstone.bpring:bjca-footstone-bpring-tx:5.3.39-nes.patch.1` | `test` |

官方隔离命令必须一次性覆盖全部三项属性：

```bash
mvn \
  -Dspring.framework.group-id=org.springframework \
  -Dspring.framework.artifact-prefix=spring- \
  -Dspring.framework.version=5.3.39 \
  clean verify
```

漏掉任一属性都可能形成混合坐标，不能作为兼容性证据。

## 4. 已验证依赖闭环

默认 NES 运行时依赖为：

```text
bjca-footstone-bpring-retry
├── bjca-footstone-bpring-context
│   ├── bjca-footstone-bpring-aop
│   ├── bjca-footstone-bpring-beans
│   ├── bjca-footstone-bpring-core
│   └── bjca-footstone-bpring-expression
└── bjca-footstone-bpring-core
    └── bjca-footstone-bpring-jcl
```

默认 effective POM、完整 dependency tree 和 runtime dependency list 均不含正式 `org.springframework:spring-*`；官方 5.3.39 invocation 的依赖树不含 `cn.bjca.footstone.bpring:bjca-footstone-bpring-*`。JaCoCo 仅存在于 Maven 插件执行环境，不是项目 dependency。

## 5. 消费者迁移原则

1. 在消费者仓库创建独立 OpenSpec。
2. 找出所有引入 `org.springframework.retry:spring-retry` 的直接和传递路径。
3. 在实际引入点排除官方 Retry，并排除会造成双份 classpath 的官方 Framework 路径。
4. 添加 NES Retry GAV 和同一套 NES Framework 依赖。
5. 保持 `org.springframework.retry.*` Java import 不变。
6. 运行 Maven/Gradle dependency tree 与业务 smoke test。
7. Spring Boot、Spring Kafka 和业务仓库分别实施自己的 adoption change，本仓库不跨仓写入。

具体示例见 [快速入门](QUICK_START.md) 和 [用户手册](USER_MANUAL.md)。

## 6. 验证门禁

默认 NES：

```bash
mvn clean verify
mvn help:effective-pom -Doutput=target/effective-pom.xml
mvn dependency:tree -Dverbose -DoutputFile=target/dependency-tree.txt
```

必须检查：

- 项目 GAV 与 SNAPSHOT/RELEASE 阶段一致；
- RELEASE POM 不含内部 `-SNAPSHOT`；
- 默认依赖树不含官方 Spring Retry 或官方 Framework；
- 官方兼容树不含 NES Framework；
- classpath 不存在官方/NES 重复类；
- JAR package 仍为 `org/springframework/retry/` 和 `org/springframework/classify/`；
- POM 保留正确许可证、上游来源和 BJCA fork 元数据；
- GAV 重品牌不被用作漏洞修复证据。
