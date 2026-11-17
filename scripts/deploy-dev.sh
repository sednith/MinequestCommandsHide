#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
VELOCITY_PLUGINS="${VELOCITY_PLUGINS:-/home/minequest/MinequestLocal/velocity/plugins}"

cd "$PROJECT_DIR"

echo "[INFO] Сборка MinequestCommandsHide"
./gradlew --no-daemon clean build -PvelocityPluginsDir="$VELOCITY_PLUGINS"

echo "[INFO] Jar в корне проекта:"
ls -1 "$PROJECT_DIR"/MinequestCommandsHide-*.jar

echo "[INFO] Jar на dev-сервере ($VELOCITY_PLUGINS):"
ls -1 "$VELOCITY_PLUGINS"/MinequestCommandsHide-*.jar

echo "[INFO] Готово. Перезапустите Velocity или выполните /mch reload."
