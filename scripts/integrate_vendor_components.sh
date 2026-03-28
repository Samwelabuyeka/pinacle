#!/usr/bin/env bash
set -euo pipefail

VENDOR_DIR="${MAYA_VENDOR_DIR:-$HOME/maya_vendor}"
TARGET_DIR="${PWD}/integrations/vendor"
mkdir -p "$TARGET_DIR"

copy_if_exists() {
  local src_file="$1"
  local dst_dir="$2"
  if [ -f "$src_file" ]; then
    cp "$src_file" "$dst_dir/"
  fi
}

copy_min() {
  local name="$1"
  local src="$VENDOR_DIR/$name"
  local dst="$TARGET_DIR/$name"
  mkdir -p "$dst"
  copy_if_exists "$src/README.md" "$dst"
  copy_if_exists "$src/LICENSE" "$dst"
  copy_if_exists "$src/LICENSE.md" "$dst"
  copy_if_exists "$src/pyproject.toml" "$dst"
}

copy_min ovos-core
copy_min hivemind-core
copy_min whisper.cpp
copy_min piper
copy_min openWakeWord
copy_min home-assistant-core

echo "Integrated vendor metadata into $TARGET_DIR"
