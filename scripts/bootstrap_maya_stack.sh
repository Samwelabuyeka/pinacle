#!/usr/bin/env bash
set -euo pipefail

MAYA_VENDOR_DIR="${MAYA_VENDOR_DIR:-$HOME/maya_vendor}"
mkdir -p "$MAYA_VENDOR_DIR"

clone_repo() {
  local name="$1"
  local url="$2"
  local dst="$MAYA_VENDOR_DIR/$name"
  if [ -d "$dst/.git" ]; then
    git -C "$dst" pull --ff-only
  else
    git clone --depth 1 "$url" "$dst"
  fi
}

clone_repo ovos-core https://github.com/OpenVoiceOS/ovos-core.git
clone_repo hivemind-core https://github.com/JarbasHiveMind/HiveMind-core.git
clone_repo whisper.cpp https://github.com/ggerganov/whisper.cpp.git
clone_repo piper https://github.com/rhasspy/piper.git
clone_repo openWakeWord https://github.com/dscripka/openWakeWord.git
clone_repo home-assistant-core https://github.com/home-assistant/core.git

echo "Downloaded stack repos into: $MAYA_VENDOR_DIR"
echo "You can now reuse/strip modules from these repos for your target device build."
