#!/usr/bin/env bash
set -euo pipefail

PROMPT="${1:-Hello from private cloud!}"
CLOUD_DIR="${CLOUD_DIR:-$HOME/private-bitnet-cloud}"

if [ ! -f "$CLOUD_DIR/.env" ]; then
  echo "Missing $CLOUD_DIR/.env. Run ./scripts/make_private_cloud.sh first." >&2
  exit 1
fi

set -a
source "$CLOUD_DIR/.env"
set +a

"$CLOUD_DIR/start_private_cloud.sh" >/tmp/bitnet_private_cloud.log 2>&1 &
PID=$!
trap 'kill $PID >/dev/null 2>&1 || true' EXIT
sleep 1

curl -s "http://127.0.0.1:${BITNET_CLOUD_PORT}/generate" \
  -H "Authorization: Bearer ${BITNET_CLOUD_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"prompt\":\"${PROMPT}\",\"n_predict\":32}"

echo
