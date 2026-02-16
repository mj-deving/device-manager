# Project 2: Device Manager — Architecture Guide

> **Audience**: Developers learning full-stack Java and AI-assisted development workflows.
> **Focus**: Architectural decisions, the reasoning behind them, and hard-won lessons.
> This is not a code tutorial — it is a map of *why things are the way they are*.

---

## What This Project Demonstrates

Project 2 is a deliberately multi-layered application:

```
PROBLEM DOMAIN                  LEARNING DOMAIN
──────────────                  ───────────────
Manage network devices          REST API design (Spring Boot)
  → CRUD + audit log            ORM + database design (JPA/PostgreSQL)
  → Filter/search/stats         Security (Spring Security Basic Auth)
  → Desktop client              Client-server architecture (JavaFX)
  → Web client (Project 3)      Multi-module builds (Maven)
                                CI/CD pipelines (GitHub Actions + GitLab)
```

The domain is simple on purpose. Complexity lives in the *architecture*, not the business rules — which is exactly where you learn the most.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  CLIENT TIER                                                    │
│                                                                 │
│  JavaFX Desktop App          Web Browser (Project 3)           │
│  ┌───────────────────┐       ┌─────────────────────────┐       │
│  │ Login Dialog      │       │ Bootstrap 5 + Fetch API  │       │
│  │ Device TableView  │       │ Same features as JavaFX  │       │
│  │ CSV / About / etc │       │ Demonstrates: same API,  │       │
│  └────────┬──────────┘       │ two completely different │       │
│           │ HTTP Basic Auth  │ clients                  │       │
│           │                  └────────────┬────────────┘       │
└───────────┼───────────────────────────────┼─────────────────────┘
            │                               │
            ▼                               ▼
┌─────────────────────────────────────────────────────────────────┐
│  REVERSE PROXY TIER (nginx · 213.199.32.18:80)                 │
│                                                                 │
│   /api/v1/  ──────────────────────► localhost:8080             │
│   /         ──────────────────────► /var/www/portfolio/        │
│   /health   ──────────────────────► 200 OK (no upstream)       │
│   /swagger-ui/  ──────────────────► localhost:8080             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│  APPLICATION TIER (Spring Boot 3.2 · port 8080)                │
│                                                                 │
│   HTTP Request                                                  │
│       │                                                         │
│       ▼                                                         │
│   SecurityFilterChain ──(401 if no/wrong creds)                │
│       │                                                         │
│       ▼                                                         │
│   Controller Layer    ──(validation, routing)                  │
│       │                                                         │
│       ▼                                                         │
│   Service Layer       ──(business logic)                       │
│       │                                                         │
│       ▼                                                         │
│   Repository Layer    ──(Spring Data JPA)                      │
│       │                                                         │
└───────┼─────────────────────────────────────────────────────────┘
        │
┌───────▼─────────────────────────────────────────────────────────┐
│  DATA TIER (PostgreSQL 16 · port 5432)                         │
│   devices, device_logs                                          │
└─────────────────────────────────────────────────────────────────┘
```

**Why this layering?** Each tier has exactly one job. nginx handles TLS termination and routing decisions — the Spring app doesn't need to know it's behind a proxy. The app handles business logic. PostgreSQL handles durability. Change one layer without touching the others.

---

## Module Design: Why Multi-Module Maven

```
device-manager/
├── pom.xml            ← PARENT: dependency versions, plugin config
├── device-manager-server/
│   └── pom.xml        ← CHILD: just says "I inherit from parent"
└── device-manager-client/
    └── pom.xml        ← CHILD: same
```

**The problem multi-module solves:** Server and client share nothing at runtime (different JVMs, different machines) but *do* share build tooling, dependency versions, and project identity. Without a parent POM you'd have to synchronize Spring Boot versions, compiler flags, and surefire versions in two places — and they'd drift.

**The tradeoff:** You pay upfront with more POM complexity. Worth it at 2+ modules; overkill at 1.

### The Custom Parent POM Trap

This project uses `spring-boot-dependencies` as a BOM import rather than inheriting `spring-boot-starter-parent`. The distinction matters:

```
OPTION A: Inherit from spring-boot-starter-parent
    Pros: Everything just works — plugins, versions, defaults
    Cons: You lose control of your parent hierarchy

OPTION B: Import spring-boot-dependencies as BOM  ← what we do
    Pros: Custom parent POM — full control
    Cons: Plugin management from Spring's parent is NOT applied
```

**Consequence of Option B:** Three things that feel "automatic" in Spring Boot projects must be explicit:
1. `maven-compiler-plugin` with `-parameters` flag — Spring MVC needs method parameter names at runtime
2. `spring-boot-maven-plugin` with `repackage` goal — without this you get a 28KB thin JAR, not a fat JAR
3. `maven-surefire-plugin:3.2.5` — Maven 3.8.x bundles surefire 2.x which produces "Tests run: 0" for JUnit 5

The lesson: **frameworks that hide complexity are helpful until you deviate from their happy path — then you need to understand what they were hiding.**

---

## Security Architecture

### Why Basic Auth?

The API is stateless and machine-to-machine. The realistic alternatives:

```
JWT Tokens
  + Better for large-scale, multi-service environments
  - Requires token issuance endpoint, refresh logic, storage
  - Significant complexity for a portfolio demo

Session Cookies
  + Browser-native
  - Requires server-side session state → not truly stateless
  - Doesn't work cleanly for desktop clients

Basic Auth  ← chosen
  + Dead simple — one header, every request
  + Stateless by definition
  + Supported natively by curl, browsers, HttpClient
  - Credentials sent on every request (fine over HTTPS, acceptable demo)
  - Not suitable for production with many users
```

For a portfolio API with one demo user and HTTP (no TLS yet), Basic Auth is the honest choice. Overengineering with JWT would obscure the actual learning.

### The Security Filter Chain

```
Incoming Request
      │
      ▼
DisableEncodeUrlFilter     ← prevents session IDs in URLs
      │
      ▼
SecurityContextHolderFilter ← establishes security context
      │
      ▼
BasicAuthenticationFilter   ← extracts + validates Authorization header
      │
      ├─ Valid creds → set Authentication in context, continue
      └─ No/bad creds → 401 Unauthorized (for protected endpoints)
      │
      ▼
AuthorizationFilter         ← checks: is this endpoint permitted?
      │
      ├─ /health, /swagger-ui/**, /v3/api-docs/** → pass
      └─ everything else → requires authenticated()
```

**Why `STATELESS` session policy?** Without it, Spring Security creates an `HttpSession` and sets a `JSESSIONID` cookie. REST clients don't use cookies. The session is created and immediately abandoned, wasting memory.

### Passwords: Why BCrypt Even for Demo Credentials?

BCrypt is intentionally slow (CPU-bound), which makes brute-force attacks expensive. For a hardcoded demo password it's technically unnecessary — but it:
1. Shows correct practice in the portfolio
2. Avoids security scanner warnings about `{noop}` plain-text passwords
3. Costs nothing extra

---

## The Client Architecture

### Problem: Who Creates the ApiClient?

The JavaFX app needs to authenticate before it can do anything. The naive approach — constructor-inject credentials — doesn't work because FXML's `FXMLLoader` calls the default constructor:

```
FXML Loader creates controllers via reflection (no-arg constructor)
    → Can't inject dependencies through the constructor
    → Must use setter injection or a shared holder
```

**Solution: AppContext singleton**

```java
// Set once at login time
AppContext.setApiClient(authenticatedClient);

// Read everywhere else
apiService = new DeviceApiService(AppContext.getApiClient());
```

This is not dependency injection — it's a global variable with a clean name. The key question is always: *is the tradeoff worth it?* For a single-window desktop app with exactly one shared dependency, yes. For a large application, a proper DI container would be warranted.

### Why JavaFX Tasks for Network Calls?

JavaFX has a single UI thread (the "JavaFX Application Thread"). Every UI update must happen on this thread. Network calls block — they wait for bytes to arrive.

```
WRONG:
    UI Thread → network call → waits 2 seconds → UI freezes → result

RIGHT:
    UI Thread → starts Task on background thread
    Background thread → network call → gets result
    Background thread → Platform.runLater() → UI Thread → update UI
```

This is the same pattern as JavaScript's `Promise` / `async-await`, Android's `AsyncTask`, or React's `useEffect` with `fetch`. The rule is universal: **never block the UI thread**.

---

## CI/CD Architecture

```
Developer Machine
    │
    ├─ git push origin master ────► GitHub (primary)
    │                                    │
    └─ git push gitlab master ────► GitLab (secondary)

                                    GitHub
                                    │
                                    ▼
                               GitHub Actions CI
                               ┌─────────────────┐
                               │ 1. Checkout      │
                               │ 2. Setup JDK 17  │
                               │ 3. Start Postgres │
                               │ 4. Apply schema  │
                               │ 5. mvn test      │  ← 12/12 green
                               │ 6. mvn package   │
                               │ 7. Upload JAR    │
                               └─────────────────┘

                               Manual Deploy (SSH)
                               ┌──────────────────────────────┐
                               │ git pull + mvn build on VPS  │
                               │ deploy-app.sh restarts svc   │
                               │ auto-tags deploy/server-...  │
                               └──────────────────────────────┘
```

**Why GitHub Actions over GitLab CI?** GitLab CI is blocked on free accounts without identity verification. This isn't a technical judgment — it's a practical one. The `.gitlab-ci.yml` exists and is correct; GitHub Actions is the workhorse. See ADR 003.

**Why build on the VPS in the deploy stage?** The Maven build cache lives on the VPS from prior builds. Transferring a 45 MB fat JAR over SSH on every deploy is slower and more fragile than letting the VPS rebuild with its warm cache. The deploy stage in `.gitlab-ci.yml` SSHes in and runs `git pull + mvn package + systemd restart`.

**Why timestamp deploy tags?** `deploy/server-20260214-1506` is a permanent bookmark in git history. `git diff deploy/server-20260214-1506 HEAD` instantly shows every change deployed since that moment — invaluable for debugging production issues.

---

## Lessons Learned

### 1. "Tests run: 0" Hides Everything

Maven Surefire 2.x (bundled with Maven 3.8.x) cannot discover JUnit 5 tests. It runs zero tests and reports `BUILD SUCCESS`. This masked a CSRF bug in `DeviceControllerTest` for two entire sessions.

**The insidious part:** zero tests is indistinguishable from "the test file doesn't exist" unless you actively check the count. Always assert that tests were actually *run*, not just that they *passed*.

**Rule:** Pin `maven-surefire-plugin:3.2.5` explicitly in any project that uses JUnit 5 with a custom parent POM.

### 2. @WebMvcTest Is a Slice Test — It Lies By Omission

`@WebMvcTest` says: "I set up the web layer for you." What it doesn't say: "I won't load your `SecurityConfig`." Without `@Import(SecurityConfig.class)`, Spring Boot applies its *default* security, which has CSRF enabled. The result: POST and DELETE tests return 403 even though `@WithMockUser` is set. The failure looks like an auth problem; the real cause is a missing import.

**Mental model:** Slice tests are like unit tests for a layer — they mock everything outside their scope. Security configuration is "outside" the web layer from `@WebMvcTest`'s perspective.

### 3. JavaFX Cell Reuse Is Invisible Until It Breaks

`TableView` reuses `TableCell` instances for performance. A cell that rendered a green "ACTIVE" status gets recycled for a grey "DECOMMISSIONED" row. Without `removeAll()` before `add()` in `updateItem()`, the old CSS class remains — two styles fight, the last one wins inconsistently.

**The rule:** In any `updateItem()` override, always clean state before setting new state. Always call `super.updateItem()` first.

### 4. `visible=false` ≠ Hidden

In JavaFX layout, `setVisible(false)` hides a node visually but the layout manager still allocates space for it — your offline banner disappears but leaves a red gap. `setManaged(false)` removes the node from layout calculation entirely. **Always set both** for nodes that should truly disappear.

### 5. Git Bash on Windows Mangles SSH Paths

```bash
# This silently converts /home/dev to C:/Program Files/Git/home/dev
ssh vps "/home/dev/deploy-app.sh arg1 arg2"   # ← exit 127

# This works — "bash" is not a path, no conversion
ssh vps "bash /home/dev/deploy-app.sh arg1 arg2"
```

Git Bash (MSYS2) converts leading `/` arguments into Windows paths before handing them to SSH. The remote receives a Windows path; the Linux server can't find it. Prefix with `bash` or set `MSYS_NO_PATHCONV=1`.

---

## AI-Assisted Development Notes

This project was built with Claude Code handling ~80% of code generation. A few workflow observations:

**What AI does well:**
- Boilerplate: Spring controllers, JPA entities, FXML layouts — generated and correct first time
- Pattern application: "add Basic Auth like ADR 009 says" — consistent, idiomatic
- Debugging with context: explaining the `@WebMvcTest` CSRF trap requires knowing Spring Security internals that would take hours to find via Stack Overflow

**What still needs human judgment:**
- Architecture decisions: *should* this be Basic Auth or JWT? AI can list tradeoffs; you choose
- Knowing when "Tests run: 0" is suspicious — the CI showed green, so it needed a human to question it
- Deciding what belongs in `CLAUDE.md` global vs project-local memory — AI writes what you tell it to remember

**The key pattern — Decision → Record → Reference:**

```
1. Decide architecture (human + AI analysis)
         │
         ▼
2. Write ADR in .ai/decisions/ (permanent record of WHY)
         │
         ▼
3. Update CLAUDE.md with patterns/gotchas (AI memory)
         │
         ▼
4. Next session: AI reads context, builds on it consistently
```

Without ADRs and persistent memory, every session starts from zero. With them, AI-assisted sessions compound — each one builds on verified knowledge from the last.

---

## Quick Reference: The Decisions That Matter

| Decision | Why | Gotcha |
|----------|-----|--------|
| Custom parent POM | Control over hierarchy | Must explicitly configure surefire, repackage, -parameters |
| Spring Security Basic Auth | Stateless, simple, demo-appropriate | Tests need `@Import(SecurityConfig.class)` |
| AppContext singleton | FXML can't constructor-inject | Global state — acceptable at this scale only |
| Background threads for network | JavaFX UI thread must not block | All UI updates via `Platform.runLater()` |
| Build on VPS in CI deploy | Warm Maven cache, no JAR transfer | VPS must be reachable during CI run |
| nginx reverse proxy | Decouple routing from app | Must proxy `/swagger-ui/` AND `/v3/api-docs` separately |
| Timestamp deploy tags | Precise diff since last deploy | Must push tag to remote — local-only tags are useless |
| BCrypt for demo password | Portfolio shows correct practice | Overhead negligible; correctness matters |

---

*Next: Project 3 (device-manager-web) applies the same API with a completely different client technology — vanilla JS + Bootstrap 5 — demonstrating that a well-designed REST API is client-agnostic.*
