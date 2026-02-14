#!/usr/bin/env bash
# deploy.sh — Deploy device-manager-server to the VPS.
#
# Run from your local machine (Git Bash / WSL / Linux):
#   ./deploy.sh
#
# Prerequisites:
#   - SSH alias "vps" configured in ~/.ssh/config pointing to 213.199.32.18
#   - Repository already cloned at /home/dev/device-manager on the VPS
#   - /home/dev/deploy-app.sh exists on the VPS
#
# Strategy: all heavy lifting (git pull, maven build, systemd restart) runs
# on the VPS over SSH — this avoids the Windows Maven PATH issue and keeps
# the deploy script dependency-free on the local machine.

set -euo pipefail

VPS="vps"
APP_DIR="/home/dev/device-manager"
JAR_PATH="$APP_DIR/device-manager-server/target/device-manager-server-1.0.0.jar"
SERVICE_NAME="device-manager"

# ── Pre-flight checks ─────────────────────────────────────────────────────────
# Rule: the VPS always runs code that is in version control.
# If you haven't committed and pushed, abort now — deploying untracked code
# creates a mismatch between what's running and what's in git history.

echo "==> Pre-flight: checking git state..."

# 1. No uncommitted changes
if [[ -n $(git status --porcelain) ]]; then
    echo ""
    echo "ERROR: You have uncommitted changes." >&2
    echo "       Commit and push first, then re-run ./deploy.sh" >&2
    echo ""
    git status --short
    exit 1
fi

# 2. Local HEAD must match remote HEAD (i.e. everything is pushed)
# git ls-remote queries GitHub directly without modifying local state.
LOCAL_SHA=$(git rev-parse HEAD)
REMOTE_SHA=$(git ls-remote origin refs/heads/master | cut -f1)

if [[ "$LOCAL_SHA" != "$REMOTE_SHA" ]]; then
    echo ""
    echo "ERROR: Local branch is ahead of origin/master (unpushed commits)." >&2
    echo "       Run: git push origin master" >&2
    echo "       Then re-run ./deploy.sh" >&2
    echo ""
    echo "  Local:  $LOCAL_SHA"
    echo "  Remote: $REMOTE_SHA"
    exit 1
fi

echo "    git state OK — HEAD $(git rev-parse --short HEAD) is pushed."
echo ""

# ── Deploy ────────────────────────────────────────────────────────────────────

echo "==> [1/5] Pulling latest code on VPS..."
ssh "$VPS" "cd $APP_DIR && git pull origin master"

echo "==> [2/5] Building server JAR on VPS (tests skipped)..."
# -pl device-manager-server  — build only the server module, not the JavaFX client
# -DskipTests               — skip tests here; they run in GitHub Actions CI
# -B                        — batch mode: no progress bars, cleaner SSH output
ssh "$VPS" "cd $APP_DIR && mvn clean package -pl device-manager-server -DskipTests -B"

echo "==> [3/5] Deploying via deploy-app.sh..."
# deploy-app.sh stops the old service, copies the JAR, starts the new service,
# and enables it on boot.
ssh "$VPS" "/home/dev/deploy-app.sh $SERVICE_NAME $JAR_PATH"

echo "==> [4/5] Service status:"
ssh "$VPS" "sudo systemctl status java-app@$SERVICE_NAME --no-pager -l"

echo "==> [5/5] Smoke test (waiting 5s for Spring Boot to start)..."
# Try actuator health first (if spring-boot-actuator is on classpath),
# fall back to the stats endpoint which is always available.
ssh "$VPS" "sleep 5 && (curl -sf http://localhost:8080/actuator/health || curl -sf http://localhost:8080/api/v1/stats) && echo ' => OK'"

# ── Post-deploy: tag the deployed commit ──────────────────────────────────────
# Creates a timestamped tag pinned to exactly the commit now running on the VPS.
# Later:  git diff deploy/server-<timestamp> HEAD
# shows every change made since that deploy.

TAG="deploy/server-$(date +%Y%m%d-%H%M)"
git tag -a "$TAG" -m "Deploy device-manager-server to VPS"
git push origin "$TAG"

echo ""
echo "==> Deploy complete."
echo "    API:     http://213.199.32.18/api/v1/devices"
echo "    Swagger: http://213.199.32.18/swagger-ui/index.html"
echo "    Stats:   http://213.199.32.18/api/v1/stats"
echo ""
echo "    Tagged:  $TAG"
echo "    Diff since this deploy: git diff $TAG HEAD"
