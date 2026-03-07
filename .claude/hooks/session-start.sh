#!/bin/bash
set -euo pipefail

# Only run in remote (Claude Code on the web) environments
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

# Make gradlew executable
chmod +x gradlew

# Download Gradle wrapper distribution and project dependencies, then compile.
# Use || true so session startup is not blocked if network restrictions
# prevent downloading some dependencies (e.g. blocked Maven repos).
./gradlew classes --no-daemon || echo "Warning: Gradle build failed (likely due to network restrictions). Dependencies may need to be resolved manually."
