## Context

`spring-retry-1.3` is a Git repository at `spring-retry-1.3`. This `primary` scope change releases `1.3.4-nes.patch.1` from one dedicated source commit and records enough evidence for a fresh session to reconcile the component against Git, OpenSpec, and Nexus.

Representative publications:

`cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`

Complete publication set:

`cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`

Declared internal dependencies:

`spring-framework-5.3`

Explicit exclusions:

none

## Goals / Non-Goals

**Goals:**

- Preserve unrelated work and establish single-session ownership before editing.
- Publish `1.3.4-nes.patch.1` with no internal `cn.bjca.footstone` SNAPSHOT metadata.
- Verify the catalog-defined local build, tests, publication metadata, and consumer behavior.
- Prove target absence before the single authorized Nexus deployment.
- Verify immutable remote assets before creating annotated tag `v1.3.4-nes.patch.1`.
- Keep component documentation and OpenSpec evidence sufficient for later reconciliation and archive.

**Non-Goals:**

- Do not introduce unrelated features, dependency upgrades, or refactors.
- Do not publish excluded modules or coordinates.
- Do not overwrite, delete, or redeploy an existing RELEASE version.
- Do not create the next development SNAPSHOT as part of this change.
- Do not store or print repository credentials.

## Decisions

### 1. Freeze the worktree before release edits

Record the current branch, origin, tracked changes, untracked paths, active OpenSpec changes, toolchain, current version, and target GAVs. Stop when unrelated tracked changes exist. Exclude local tool directories and other unapproved files from the release commit.

Only the manifest owner for `spring-retry-1.3` may write to this repository while the component is being prepared.

### 2. Publish only a complete internal RELEASE dependency graph

Set the project version to `1.3.4-nes.patch.1` and update every internal dependency, parent, imported BOM, and plugin version to its approved RELEASE. Generate the complete local publication set and scan all generated POM/BOM metadata, not only the representative GAVs.

Any internal version ending in `-SNAPSHOT` is a hard failure.

### 3. Bind validation and publication to one release commit

Run:

- Build: `make verify`
- Tests: `make test`
- Local publication/effective POM: `make install-local`

Audit existing development build/test evidence first. It may be reused when it identifies a successful command and commit in the release commit's ancestry and the intervening diff contains no production source, test source, or build-logic change that invalidates coverage. Record the ancestry and diff assessment, then run only missing or invalidated coverage. Obtain the coordinator-controlled workspace build slot before each required `make`, Gradle, or Maven command; no other component may execute such a command concurrently. A successful build that already includes the complete required test suite may be recorded as both build and test evidence.

Run representative RELEASE-only consumer validation after local publication. Update `README.md`, `doc/GAV_MAPPING.md`, `doc/RELEASE_NOTES.md`, `doc/RELEASE_GUIDE.md`, `doc/TESTING.md` before creating the dedicated release commit. Build and deploy from that recorded commit without subsequent source changes.

### 4. Separate reversible preparation from irreversible finalization

A child session may prepare this repository, run local gates, update component documentation, and create the local release commit while holding the owner lease.

The coordinating main session reviews evidence and controls:

- Nexus RELEASE deploy using `make deploy`;
- release commit push;
- annotated tag creation and push.

Every command remains a preview until the coordinating session selects execute mode. Deploy authorization remains an explicit release decision. After Nexus and Git gates pass, the coordinator obtains TAG/PUSH confirmation tokens from CLI previews and executes them directly without asking the user to echo ceremonial tokens.

Engineering legal/open-source review is evidence-driven, not a ceremonial user acknowledgement. Check license permissions and obligations, LICENSE/NOTICE/SPDX/source delivery, attribution and endorsement language, distribution boundary, and exclusions. Continue when no substantive blocker exists; surface the concrete risk and pause when an unresolved condition could materially change compliance or authorization.

Before Maven deploy, verify that the RELEASE `distributionManagement` repository ID has a matching non-empty server ID in the selected user settings and that the intended URL profile is active. A different server ID with valid credentials does not authenticate the RELEASE repository.

### 5. Treat Nexus RELEASE as immutable

Immediately before deploy, check every GAV in the discovered publication set for absence. If any complete or partial asset exists, block deploy and investigate.

After the one authorized deploy, download representative POM and binary assets from Nexus RELEASE, verify version and metadata, calculate checksums, and run the RELEASE-only consumer smoke test. Do not create or push `v1.3.4-nes.patch.1` until the component reaches `nexus-verified`.

An uncertain or partial publication is reconciled first. Existing assets are never deleted or overwritten; a corrected release uses a new NES patch version.

### 6. Make the tag and archive auditable

Create annotated tag `v1.3.4-nes.patch.1` on the exact release commit used for deployment. Verify the remote branch and tag after push.

Keep the component OpenSpec active until Nexus, Git tag, component documentation, and central manifest evidence agree. Archive only after all completion gates pass.

## State Progression

```text
planned
  -> openspec-ready
  -> prepared
  -> locally-verified
  -> release-committed
  -> deployed
  -> nexus-verified
  -> tagged
  -> documented
  -> archived
```

`blocked` and `partial-failure` are terminal for automatic progression until the coordinator records an approved reconciliation. `superseded` identifies an immutable version replaced by a later NES patch.

## Risks / Trade-offs

- **Dirty worktree contaminates the release commit** — fail closed on unrelated tracked changes and review the staged diff.
- **Generated POM retains an internal SNAPSHOT** — scan every local and downloaded remote POM/BOM.
- **Multi-module deploy partially succeeds** — record existing assets, do not retry the version, and create a new patch plan.
- **Session ends after deploy** — reconcile Nexus and Git instead of repeating deploy.
- **Tag points to a different commit** — verify the tag target locally and remotely before archive.
- **Credentials leak into evidence** — use only user-level configuration and redact output before recording it.

## Recovery

- Before Nexus publication, fix release preparation through this change and repeat local verification.
- After uncertain deploy, freeze all release actions and inspect Nexus assets.
- After verified Nexus publication but failed Git push, retry only the push.
- After partial or incorrect publication, preserve evidence and replace this target with a newly approved patch version.
