# ADR 004 — Multi-Module Maven Structure for Projects 2 and 4

**Status**: Accepted
**Date**: 2026-02-14
**Project**: device-manager (Project 2), arcade-hub (Project 4)

---

## Context

Projects 2 and 4 each contain two or more separately deployable JARs that
share a domain model and version:

- **device-manager**: server (Spring Boot, :8080) + client (JavaFX desktop app)
- **arcade-hub**: server (Spring Boot + WebSocket, :8081) + simulator (standalone Java)

The question was whether to manage these as:
1. A single Maven project with multiple source trees
2. Separate, independent Maven projects per JAR
3. A **multi-module Maven project** with a parent POM

---

## Decision

Use **multi-module Maven** with a parent POM for Projects 2 and 4.

```
device-manager/
├── pom.xml                     ← parent (packaging=pom)
├── device-manager-server/
│   └── pom.xml                 ← child module
└── device-manager-client/
    └── pom.xml                 ← child module
```

---

## Reasons

1. **Single version source of truth**: The version (`1.0.0`) is declared once
   in the parent POM. Both modules inherit it. A version bump is one-line
   change, not two.

2. **Dependency version management via BOM**: The parent imports the Spring Boot
   BOM (`<dependencyManagement>`). Child modules declare dependencies without
   `<version>` tags — Spring Boot's BOM resolves compatible versions
   automatically. This prevents the "dependency hell" of managing compatible
   versions manually across modules.

3. **Build all with one command**: `mvn clean package` from the root builds
   both JARs in dependency order. The server JAR is available before the
   client compiles (relevant if client ever depends on server DTOs).

4. **Selective build**: `mvn clean package -pl device-manager-server` builds
   only the server — used in `deploy.sh` to avoid compiling the JavaFX client
   on a headless VPS (JavaFX requires a display or headless mode configuration).

5. **CI clarity**: GitHub Actions builds the parent, producing both artifacts
   in one workflow run. Two separate repos would need two separate workflows.

---

## Trade-offs

| | Multi-module | Separate repos |
|---|---|---|
| Version consistency | Automatic | Manual |
| Dependency management | BOM inheritance | Duplicate `<version>` tags |
| Build in one command | Yes | No |
| Repo simplicity | More complex root POM | Simpler individual POMs |
| Independent release cadence | Harder | Easy |
| CI setup | One workflow | One workflow per repo |

Separate repos would make sense if server and client had completely different
release cycles or different teams. For a portfolio project where they are
always released together, multi-module is strictly better.

---

## Consequences

- The parent POM uses `<packaging>pom</packaging>` — it produces no JAR itself,
  only coordinates the build.
- Each child POM declares `<parent>` pointing to the parent artifact. Maven
  resolves the parent by looking in the local repository or by relative path
  (`../pom.xml`).
- Adding a third module (e.g., a shared `device-manager-common` library for
  DTOs shared between server and client) requires only: create the directory,
  add a child POM, declare it in the parent's `<modules>` list.
- The JavaFX client (`device-manager-client`) uses `mvn javafx:run` for local
  development — this requires the `javafx-maven-plugin` in the client POM, not
  the parent.
