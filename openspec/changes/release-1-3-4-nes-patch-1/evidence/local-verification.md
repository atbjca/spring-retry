# Local RELEASE verification

- Completed at: `2026-08-17T07:43:33Z`
- Source HEAD before release commit: `b6b6508b9f0130154df5b4345376caf8936405e1`
- Version: `1.3.4-nes.patch.1`
- Resource policy: Make/Maven single-threaded; Maven used no `-T`; no concurrent Gradle command

## Local publication

Command:

```text
make install-local
```

Effective command:

```text
JAVA_HOME=/Users/anan/.sdkman/candidates/java/8.0.472-amzn \
  /Users/anan/dev/apache-maven-3.8.2/bin/mvn clean install
```

Result: `BUILD SUCCESS` in 47.2 seconds. Maven ran the existing full suite: 328 tests, 0 failures, 0 errors, 0 skipped. JaCoCo report/check and Javadoc passed. The local repository contains exactly the main JAR, POM, sources JAR, and javadoc JAR under the target RELEASE directory.

## Metadata and artifact checks

The release scanner checked the source POM and installed POM:

```json
{"filesScanned":2,"clean":true,"findings":[]}
```

The generated metadata uses NES Framework `5.3.39-nes.patch.1` and contains no internal `cn.bjca.footstone` SNAPSHOT reference. The artifact SHA-256 values are:

| Asset | SHA-256 |
|---|---|
| main JAR | `915fb580a1668816151328bd9bac28cbdad1d443ab123517a2a10d7a579f2e7f` |
| sources JAR | `d3aadad40e6e972e496efe4fed57c35675c648cd08f4164ce1aa211acaff6c1f` |
| javadoc JAR | `80007ebb27c2758641c807f70bbae199ccba093090898c10783f10d30f972a99` |
| installed POM | `c32a3858422583fbf17fc0b21da35350f7e6db6c0af1179a15e867dbb66d1235` |

The main JAR contains the expected Retry packages, license metadata, Maven metadata, and `AbstractMapRetryContextCache`; representative classes are Java 8 major version 52.

## Representative consumer

Temporary consumer: `/private/tmp/spring-retry-release-consumer-20260817/`

The consumer ran offline with JDK 8 and the local Maven repository. It resolved `bjca-footstone-bpring-retry:1.3.4-nes.patch.1`, NES Framework `5.3.39-nes.patch.1`, and no official Spring first-party coordinates. The smoke test exercised both ordinary and circuit-breaker cache setters and passed 1/1 test. Dependency evidence is in `dependency-tree.txt` in that temporary directory.

An initial consumer attempt failed only because its fixture asserted a non-required `Implementation-Version` manifest attribute. The fixture was corrected to verify the loaded RELEASE path; the corrected run is the accepted result. No product or Nexus state was changed by that setup failure.

## Gate decision

Local verification passes. The manifest may advance to `locally-verified`; release commit, Nexus absence, deploy, remote verification, tag, and push remain pending.
