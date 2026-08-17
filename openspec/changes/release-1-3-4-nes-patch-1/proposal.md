## Why

`spring-retry-1.3` currently targets `1.3.4-nes.patch.1` but does not yet have a complete, immutable, and independently auditable RELEASE lifecycle. The release must survive session loss, preserve unrelated work, and publish metadata that references only approved internal RELEASE dependencies.

## What Changes

- Freeze and inventory the `spring-retry-1.3` worktree before release edits.
- Create RELEASE metadata for `1.3.4-nes.patch.1` and replace all internal SNAPSHOT references with approved RELEASE versions.
- Run the component-specific build, tests, local publication/effective-POM scan, and consumer verification.
- Form a dedicated release commit containing component release documentation and no unrelated changes.
- Verify every discovered target GAV is absent from Nexus RELEASE before the coordinator-authorized deploy.
- Download and verify published POM/binary assets and checksums from Nexus, then run RELEASE-only consumption.
- Create annotated tag `v1.3.4-nes.patch.1` on the exact release commit only after Nexus verification.
- Record Git, Nexus, documentation, and manifest evidence before archiving this change.

## Capabilities

### New Capabilities

- `component-release-spring-retry-1.3`: Release `spring-retry-1.3` `1.3.4-nes.patch.1` with reproducible local gates, immutable Nexus publication, exact Git tagging, documentation, and archive evidence.

### Modified Capabilities

None.

## Impact

- **Repository:** `spring-retry-1.3`
- **Release version:** `1.3.4-nes.patch.1`
- **Release scope:** `primary`
- **Representative GAVs:** `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`
- **Complete publication GAVs:** `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`
- **Internal RELEASE dependencies:** `spring-framework-5.3`
- **Explicit exclusions:** none
- **Release documentation:** `README.md`, `doc/GAV_MAPPING.md`, `doc/RELEASE_NOTES.md`, `doc/RELEASE_GUIDE.md`, `doc/TESTING.md`
- **External systems:** Nexus RELEASE and the repository's configured `origin`
- **Credentials:** Remain exclusively in user-level Gradle/Maven configuration and are never copied into this change or Git
