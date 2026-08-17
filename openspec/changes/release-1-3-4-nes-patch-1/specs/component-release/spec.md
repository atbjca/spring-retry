## ADDED Requirements

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
