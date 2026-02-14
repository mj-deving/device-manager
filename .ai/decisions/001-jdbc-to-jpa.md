# ADR 001 — Raw JDBC (Project 1) → JPA/Hibernate (Project 2)

**Status**: Accepted
**Date**: 2026-02-14
**Project**: device-manager (Project 2)

---

## Context

Project 1 (`device-inventory-cli`) used raw JDBC with HikariCP for all database
access. Every query was a hand-written `PreparedStatement` with manual
`ResultSet` mapping inside try-with-resources blocks.

Project 2 is a Spring Boot REST API — a context where JPA/Hibernate is the
de-facto standard. The question was whether to continue with raw JDBC or
adopt the Spring Data JPA stack.

---

## Decision

Use **JPA/Hibernate via Spring Data JPA** for Project 2.

---

## Reasons

1. **Repository pattern for free**: `JpaRepository<Device, UUID>` gives
   `findAll`, `findById`, `save`, `delete`, and paginated queries
   (`Page<Device>`) without writing a single SQL statement for standard CRUD.

2. **Schema evolution**: Hibernate's `ddl-auto: update` handles column
   additions during development. In production (`ddl-auto: validate`) it
   catches schema mismatches at startup before any request is served.

3. **Spring ecosystem fit**: Spring Boot auto-configures a `DataSource`,
   `EntityManagerFactory`, and transaction management from `application.yml`
   alone. The JDBC equivalent (HikariCP singleton, manual transaction handling)
   requires ~100 lines of boilerplate configuration.

4. **Portfolio progression**: Deliberately using different data-access
   strategies across projects (JDBC → JPA → potentially JOOQ or R2DBC in
   later projects) demonstrates breadth.

---

## Trade-offs

| | JDBC (Project 1) | JPA (Project 2) |
|---|---|---|
| Control over SQL | Full | Partial (JPQL / native query escape hatch) |
| Boilerplate | High | Low |
| N+1 query risk | None (you write every query) | Real — must use `@EntityGraph` or `JOIN FETCH` |
| Learning curve | Lower | Higher (proxy objects, lazy loading, session lifecycle) |
| Fit for REST API | Works | Idiomatic |

---

## Consequences

- `Device.java` is now a `@Entity` class (not a plain record). Field names must
  match column names or use `@Column(name=...)`.
- `DeviceLog.java` uses `@ManyToOne(fetch = FetchType.LAZY)` to avoid loading
  the full `Device` every time a log entry is fetched.
- Integration tests require `@DataJpaTest` (spins up an embedded H2 or a
  real PostgreSQL via Testcontainers) rather than `Assume.assumeNotNull`.
