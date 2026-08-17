## 1. Worktree freeze and OpenSpec readiness

- [x] 1.1 Confirm this change is apply-ready and assigned exclusively to `spring-retry-1.3` in `spring-retry-1.3`.
- [x] 1.2 Acquire the component owner lease and record owner/session/timestamps in the release manifest.
- [x] 1.3 Record branch, `origin` fetch/push URLs, HEAD SHA, active OpenSpec changes, toolchain, current version, and target `1.3.4-nes.patch.1` without exposing credentials.
- [x] 1.4 Inventory tracked and untracked changes; separately resolve any unrelated tracked work and exclude local tool directories from all release staging.
- [x] 1.5 Confirm all declared upstream components are Nexus-verified RELEASEs and record their evidence: `spring-framework-5.3`.
- [x] 1.6 Confirm the approved explicit exclusions and how each will be verified: none.
- [x] 1.7 Complete and record the engineering legal/open-source risk review; continue without ceremonial user confirmation when no substantive blocker exists, and pause only on a concrete unresolved risk.

## 2. RELEASE preparation and component documentation

- [x] 2.1 Set the component version to `1.3.4-nes.patch.1`.
- [x] 2.2 Replace every internal `cn.bjca.footstone` SNAPSHOT dependency, parent, imported BOM, and plugin with the approved RELEASE version.
- [x] 2.3 Discover the complete local publication set and reconcile it with the catalog allowlist: `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`. Use these representative GAVs for consumer smoke coverage: `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`.
- [x] 2.4 Configure the release publication to exclude only the approved items and prove included POMs do not depend on excluded modules.
- [x] 2.5 Update `README.md`, `doc/GAV_MAPPING.md`, `doc/RELEASE_NOTES.md`, `doc/RELEASE_GUIDE.md`, `doc/TESTING.md` with version, GAVs, required RELEASE dependencies, validation commands, Nexus location, exclusions, and notable constraints.
- [x] 2.6 Review the full working diff and confirm it contains no unrelated source changes or credentials.

## 3. Local verification

- [x] 3.1 Obtain the coordinator-controlled global build slot; confirm no other component is running a `make`, Gradle, or Maven command.
- [x] 3.2 Audit existing development build/test evidence, its commit ancestry, and the intervening diff; reuse qualifying coverage and identify only invalidated or missing gates.
- [x] 3.3 Record qualifying development validation evidence; do not automatically run `make verify` or `make test` solely for release, and escalate invalidated coverage to the coordinator for a minimal targeted decision.
- [x] 3.4 Generate local publications/effective POMs: `make install-local`.
- [x] 3.5 Scan every generated POM/BOM for internal `cn.bjca.footstone` `-SNAPSHOT` references; block on any match.
- [x] 3.6 Run a representative consumer test using only the locally prepared RELEASE publications and approved upstream RELEASEs.
- [x] 3.7 Store command, timestamp, exit status, artifact list, POM scan, and consumer evidence in the approved evidence location.
- [x] 3.8 Re-run worktree and diff checks, then advance the manifest to `locally-verified` only when all gates pass.

## 4. Dedicated release commit

- [x] 4.1 Stage only files covered by this OpenSpec, including required component release documentation.
- [x] 4.2 Inspect the staged diff and verify the target version, internal RELEASE dependencies, exclusions, and absence of unrelated files or secrets.
- [ ] 4.3 Create the dedicated release commit for `spring-retry-1.3` `1.3.4-nes.patch.1`.
- [ ] 4.4 Record the release commit SHA and prove the worktree/build inputs match that commit.
- [ ] 4.5 Return the repository and evidence to the coordinating main session; do not deploy, tag, or push from an unapproved child session.

## 5. Nexus absence and coordinator authorization

- [ ] 5.1 From the release commit, regenerate or verify the complete publication set.
- [ ] 5.2 Query Nexus RELEASE for every discovered target GAV and record immutable absence evidence immediately before deploy.
- [ ] 5.3 Block if any target-version POM, binary, checksum, metadata, or partial module asset already exists.
- [ ] 5.4 Have the coordinating main session review worktree, OpenSpec, local verification, release commit, upstream, exclusion, credential-isolation, and Nexus-absence evidence.
- [ ] 5.5 Verify every publication repository ID has a matching non-empty user settings server ID and the intended repository URL/profile is active, without printing credential values.
- [ ] 5.6 Preview the exact deploy command `make deploy` and record explicit coordinator authorization.

## 6. Single deploy and remote artifact verification

- [ ] 6.1 Execute `make deploy` once from the recorded release commit using explicit execute mode and confirmation.
- [ ] 6.2 Record deploy start/end times and sanitized output without credential values.
- [ ] 6.3 Download representative POM and binary/BOM assets from Nexus RELEASE and verify version, expected modules, metadata, and exclusions.
- [ ] 6.4 Scan all downloaded POM/BOM metadata for internal SNAPSHOT references.
- [ ] 6.5 Record immutable asset URLs and checksums.
- [ ] 6.6 Run the RELEASE-only consumer smoke test without an internal SNAPSHOT repository.
- [ ] 6.7 Advance to `nexus-verified` only when all remote and consumer gates pass; otherwise record `partial-failure` and do not redeploy or tag this version.

## 7. Annotated tag and controlled Git push

- [ ] 7.1 Confirm `v1.3.4-nes.patch.1` does not already exist locally or remotely and the target release commit SHA is unchanged.
- [ ] 7.2 Create annotated tag `v1.3.4-nes.patch.1` on the exact release commit only after `nexus-verified`; obtain and supply the CLI token within the coordinating session without asking the user to echo it.
- [ ] 7.3 Push the approved release commit and annotated tag to `origin` under coordinator control without a separate ceremonial user confirmation.
- [ ] 7.4 Fetch and independently verify the remote branch SHA, annotated tag object, and peeled target commit.
- [ ] 7.5 If push fails, preserve Nexus state and retry only the Git operation after reconciliation.

## 8. Documentation, reconciliation, and archive

- [ ] 8.1 Verify component documentation accurately describes the published RELEASE and does not claim excluded artifacts were published.
- [ ] 8.2 Update the central manifest with final state, release commit, tag object/target, Nexus URLs, checksums, evidence paths, exclusions, and verification timestamps.
- [ ] 8.3 Reconcile the manifest against component Git, active OpenSpec, remote tag, and Nexus; resolve every mismatch.
- [ ] 8.4 Update the human-readable run report and central project documentation only after artifact and tag verification.
- [ ] 8.5 Release the owner lease after all component writes and evidence updates finish.
- [ ] 8.6 Validate this component change and archive it only after state reaches `documented` with complete Git/Nexus/manifest evidence.
- [ ] 8.7 Record the OpenSpec archive path and final `archived` state in the release manifest.
