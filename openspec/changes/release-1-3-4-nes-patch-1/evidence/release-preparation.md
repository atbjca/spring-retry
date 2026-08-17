# RELEASE preparation

- Target: `1.3.4-nes.patch.1`
- Publication: `cn.bjca.footstone.bpring.retry:bjca-footstone-bpring-retry:1.3.4-nes.patch.1`
- Packaging: one main JAR, sources JAR, javadoc JAR, and POM
- Internal upstream: NES Spring Framework `5.3.39-nes.patch.1`, Nexus-verified
- Explicit exclusions: none
- Nexus target: `http://192.168.131.36:8088/repository/releases/`, selected by Maven `distributionManagement` server ID `releases`
- Resource policy: Maven/Make single-threaded, no Maven `-T`; no concurrent Make, Maven, or Gradle commands

The POM version is frozen to the RELEASE target. All internal Framework declarations already use `5.3.39-nes.patch.1`; no internal parent or plugin SNAPSHOT is declared. The project is single-module, so the complete publication allowlist is exactly the one GAV above.

Documentation now describes a RELEASE candidate without claiming deployment. The remaining local gates are `make install-local`, complete generated-POM scanning, and representative local consumer verification. Nexus absence, the single deploy, remote downloads/checksums, RELEASE-only consumer, annotated tag, push, final documentation, and archive remain later gates.
