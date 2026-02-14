# ADR 003 — GitHub Actions as Primary CI (Not GitLab CI)

**Status**: Accepted
**Date**: 2026-02-14
**Project**: All portfolio projects

---

## Context

All four portfolio projects are mirrored to two remotes:
- `origin` → GitHub (`https://github.com/mj-deving/`)
- `gitlab` → GitLab (`git@gitlab.com:mj-deving-group/`)

Both platforms offer CI/CD. GitLab CI has a runner
(v18.8.0) already installed and registered on the VPS. GitLab also has project
boards, 60 labels (15 per project), and `.gitlab-ci.yml` files committed to
every repo. The question was which CI platform to make authoritative.

---

## Decision

**GitHub Actions** is the primary and authoritative CI/CD pipeline.
GitLab CI pipelines exist but are currently non-functional.

---

## Reasons

1. **GitLab free-tier blocks all pipelines**: GitLab.com requires identity
   verification before running any pipeline job — including jobs on
   self-hosted runners registered to the account. The runner on the VPS is
   registered and polling, but GitLab never dispatches jobs to it. This is a
   platform restriction, not a configuration error.

2. **GitHub Actions works without verification**: Push to GitHub → Actions
   triggers immediately. No account verification required on the free tier.

3. **PostgreSQL service containers**: GitHub Actions supports
   `services: postgres:16` with `--health-cmd pg_isready` natively, making
   integration tests against a real database trivial to configure.

4. **Established template**: `device-inventory-cli` has a working
   `.github/workflows/ci.yml`. All subsequent projects copy and adapt it,
   reducing setup time per project to ~10 minutes.

---

## Current State of GitLab CI

`.gitlab-ci.yml` files exist and are syntactically correct in all repos.
They are kept for two reasons:
1. **Future-proofing**: If GitLab identity verification is completed,
   pipelines will start working immediately without any changes.
2. **Portfolio visibility**: Recruiters visiting the GitLab group see
   CI configuration, demonstrating familiarity with the platform.

---

## Consequences

- All CI status badges and pipeline links point to GitHub Actions.
- The dual-push workflow must remain: `git push origin master && git push gitlab master`.
- If GitLab CI is unblocked in the future, the `.gitlab-ci.yml` files should
  be validated and potentially designated as the primary pipeline (GitLab's
  self-hosted runner on the VPS is architecturally preferable for deploy jobs).

---

## Revisit Trigger

This decision should be revisited when GitLab identity verification is
completed. At that point, compare:
- GitHub Actions (hosted runner, simple, free for public repos)
- GitLab CI with VPS runner (self-hosted, more control, deploy-native)
