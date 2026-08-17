# Development validation reuse

Release preparation starts from HEAD `b6b6508b9f0130154df5b4345376caf8936405e1`. The archived CVE remediation, `doc/TESTING.md`, and `doc/CVE/CVE-2026-41710.md` are recorded at this same commit. The release-only diff changes the project version and release documentation/evidence; it does not change production source, test source, dependency versions, or executable build logic.

Reused evidence:

- Official Spring Framework 5.3.39: 328 tests, zero failures/errors/skips, JaCoCo and Javadoc passed.
- NES Spring Framework `5.3.39-nes.patch.1`: 328 tests, zero failures/errors/skips, JaCoCo and Javadoc passed.
- Default NES Maven install: 328 tests and packaging passed.
- Maven and Gradle local consumers passed with NES-only Framework dependencies.
- Nexus SNAPSHOT four-asset download, checksum, API, dependency-tree, and JDK 8 remote-consumer verification passed.
- CVE-2026-41710 attack-path, cache isolation, listener lifecycle, concurrency, and compatibility coverage passed; affected production classes exceed the required 60% line coverage.

Decision: do not rerun broad `make verify`, `make test`, or `make build`. Run only `make install-local` with the RELEASE version, scan generated metadata, and run a representative RELEASE local consumer. Maven remains single-threaded with no `-T`; no Make, Maven, or Gradle command may run concurrently with another project.
