# ADR 002 — Single-Stage Dockerfile (JRE Only, No Build Stage)

**Status**: Accepted
**Date**: 2026-02-14
**Project**: device-manager-server

---

## Context

A common Dockerfile pattern for Java apps is a **multi-stage build**:

```
Stage 1 (builder): FROM eclipse-temurin:17-jdk → mvn clean package
Stage 2 (runtime): FROM eclipse-temurin:17-jre → COPY --from=builder app.jar
```

This keeps the build toolchain out of the final image. The question was
whether to use multi-stage or single-stage here.

---

## Decision

Use a **single-stage Dockerfile** with `eclipse-temurin:17-jre` only.
The JAR is built by Maven *before* `docker build` runs.

---

## Reasons

1. **VPS already has Maven and JDK**: The build environment is the VPS itself.
   Docker is an optional deployment vehicle, not the build system.

2. **deploy.sh builds the JAR over SSH**: The deploy pipeline is
   `git pull → mvn package → systemd restart`. Docker is an optional extra
   that can wrap the pre-built JAR without owning the build step.

3. **Smaller image**: JRE-only (~250 MB) vs JDK (~500 MB). At runtime, a Java
   process needs the JRE (class loader, garbage collector, JIT). The compiler
   (`javac`) is never used after `mvn package` completes.

4. **Simpler Dockerfile**: 7 lines. Multi-stage would be ~20 lines and embed
   Maven dependency caching logic that already lives in the GitHub Actions CI.

---

## Trade-offs

| | Single-stage | Multi-stage |
|---|---|---|
| Image size | ~250 MB | ~250 MB (same final stage) |
| Build reproducibility | Depends on Maven on VPS | Self-contained |
| Dockerfile complexity | Low | Medium |
| CI integration | JAR must be pre-built | `docker build` alone is enough |
| Portability | Lower (needs Maven elsewhere) | Higher |

Multi-stage would be better if Docker were the *only* build/deploy mechanism
(e.g., pushing to a container registry and deploying to Kubernetes). For this
project's VPS + systemd setup, it adds complexity without benefit.

---

## Consequences

- `docker build` fails if run before `mvn clean package`. This is intentional
  and documented in the Dockerfile comments.
- To run locally with Docker:
  ```bash
  mvn clean package -pl device-manager-server -DskipTests
  cd device-manager-server
  docker build -t device-manager-server:1.0.0 .
  docker run -p 8080:8080 \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/devicedb \
    -e SPRING_DATASOURCE_USERNAME=portfolio \
    -e SPRING_DATASOURCE_PASSWORD=portfolio_dev_password \
    device-manager-server:1.0.0
  ```
- If a container registry (Docker Hub, GitHub Container Registry) is added
  in the future, re-evaluate multi-stage to make `docker build` fully
  self-contained.
