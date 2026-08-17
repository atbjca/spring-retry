# Engineering legal/open-source compliance review

Reviewed at `2026-08-17T07:21:37Z` for internal Nexus distribution of `1.3.4-nes.patch.1`. This is an engineering review, not legal advice.

- Upstream Spring Retry source is Apache License 2.0; `LICENSE-2.0.txt` is retained and the POM declares Apache 2.0.
- Existing source copyright and Apache license headers are retained.
- The main, sources, javadoc, and POM publication set provides the binary, source, API documentation, license metadata, and upstream traceability expected for this internal library distribution.
- Documentation identifies this repository as a modified NES maintenance fork and does not claim an official Spring release, endorsement, or ownership of upstream trademarks.
- Java packages remain upstream-compatible while the Maven GAV is explicitly NES-branded.
- Distribution is limited to the approved internal Nexus RELEASE repository. Credentials stay in user-level Maven settings and are not copied into Git or evidence.
- There are no excluded modules and no external managed-service or redistribution authorization is implied by this release.

No concrete unresolved licensing, notice, attribution, source-delivery, trademark, or distribution-boundary blocker was found. Release preparation may continue without a ceremonial user confirmation.
