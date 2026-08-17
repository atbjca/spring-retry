# Remote RELEASE verification

- Version: `1.3.4-nes.patch.1`
- Release commit: `1f6dc7a9a02a1b19662c4f1a099fd7dd4069c291`
- Nexus repository: `http://192.168.131.36:8088/repository/releases/`
- Final pre-deploy absence check: `2026-08-17T08:12:30Z`, POM and JAR both `404`
- Successful single upload: completed `2026-08-17T08:15:52Z`
- Nexus core verification: `2026-08-17T08:17:27Z`
- RELEASE-only consumer completed: `2026-08-17T08:29:15Z`
- Git remote verification: `2026-08-17T08:37:44Z`

## Deployment

The first coordinator execute invocation was rejected by `check-deploy` before Maven started because the central catalog omitted the required `ALLOW_RELEASE_DEPLOY=true` flag. Nexus was reconciled and remained completely absent. The catalog entry was corrected, the exact command was re-previewed, and the one actual upload used:

```text
make deploy ALLOW_RELEASE_DEPLOY=true
```

Maven ran single-threaded on JDK 8 with no `-T`. Result: `BUILD SUCCESS` in 48.315 seconds; 328 tests passed with zero failures/errors/skips, JaCoCo check passed, and main JAR, POM, sources JAR, javadoc JAR, and Maven metadata were uploaded. No redeploy or overwrite occurred.

## Remote assets

| Asset | SHA-256 |
|---|---|
| POM | `c32a3858422583fbf17fc0b21da35350f7e6db6c0af1179a15e867dbb66d1235` |
| main JAR | `be3fe5aac3fc9dbe42668ef9ae76cd59f0f0e9b992a573f8b13966c76a1c9972` |
| sources JAR | `47160da016ba5acae219cd59a31013647465d15c408e7303f009b4953acedece` |
| javadoc JAR | `3e30f165af865d33521ea0cf3462c944b874ea8f6a3e150a1e53ba7d139aef77` |

All four downloaded assets match the deploy-after-build local candidates. The remote POM scanner returned `clean=true` with no internal SNAPSHOT findings. Metadata uses NES Framework `5.3.39-nes.patch.1`; the main JAR contains the CVE repair classes and Java 8 major version 52.

## RELEASE-only consumer

The consumer used a fresh isolated Maven local at `/private/tmp/spring-retry-release-consumer-20260817/m2-release-only` and a settings file in which every repository and plugin repository has `snapshots=false`. It resolved Retry `1.3.4-nes.patch.1` and only NES Framework `5.3.39-nes.patch.1` first-party dependencies. Result: 1 test, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESS` in 19.476 seconds.

## Git finalization

- Annotated tag: `v1.3.4-nes.patch.1`
- Tag object: `d96b2fa6a19b0c0642d45141eb2ffdcb01663d0f`
- Peeled target: `1f6dc7a9a02a1b19662c4f1a099fd7dd4069c291`
- Remote branch `refs/heads/1.3.x-bjca-patch`: `1f6dc7a9a02a1b19662c4f1a099fd7dd4069c291`

Push and independent verification used GitHub SSH-over-443 because direct HTTPS was unavailable. The repository origin was restored to its original HTTPS URL afterward.
