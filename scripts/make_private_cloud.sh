#!/usr/bin/env bash
set -euo pipefail

CLOUD_DIR="${CLOUD_DIR:-$HOME/private-bitnet-cloud}"
mkdir -p "$CLOUD_DIR"

if [ ! -f "$CLOUD_DIR/.env" ]; then
  API_KEY="$(python - <<'PY'
import secrets
print(secrets.token_urlsafe(32))
PY
)"
  cat > "$CLOUD_DIR/.env" <<EOV
BITNET_CLOUD_HOST=127.0.0.1
BITNET_CLOUD_PORT=8080
BITNET_CLOUD_API_KEY=$API_KEY
BITNET_DIR=${BITNET_DIR:-$HOME/bitnet.cpp}
EOV
fi

cat > "$CLOUD_DIR/start_private_cloud.sh" <<'EOS'
#!/usr/bin/env bash
set -euo pipefail
set -a
source "$(dirname "$0")/.env"
set +a
python /workspace/pinacle/private_cloud/bitnet_private_api.py
EOS
chmod +x "$CLOUD_DIR/start_private_cloud.sh"

echo "Private cloud created at: $CLOUD_DIR"
echo "Start it with: $CLOUD_DIR/start_private_cloud.sh"
echo "It binds to 127.0.0.1 by default (private/local only)."
