#!/usr/bin/env python3
import json
import os
import subprocess
from http.server import BaseHTTPRequestHandler, HTTPServer

HOST = os.environ.get("BITNET_CLOUD_HOST", "127.0.0.1")
PORT = int(os.environ.get("BITNET_CLOUD_PORT", "8080"))
API_KEY = os.environ.get("BITNET_CLOUD_API_KEY", "")
BITNET_DIR = os.environ.get("BITNET_DIR", os.path.expanduser("~/bitnet.cpp"))
MODEL_PATH = os.environ.get(
    "BITNET_MODEL",
    os.path.join(BITNET_DIR, "models/BitNet-b1.58-2B-4T/ggml-model-i2_s.gguf"),
)


class Handler(BaseHTTPRequestHandler):
    def _send(self, code: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        if self.path != "/generate":
            return self._send(404, {"error": "not_found"})

        if API_KEY and self.headers.get("Authorization") != f"Bearer {API_KEY}":
            return self._send(401, {"error": "unauthorized"})

        length = int(self.headers.get("Content-Length", "0"))
        data = json.loads(self.rfile.read(length) or b"{}")
        prompt = data.get("prompt")
        if not prompt:
            return self._send(400, {"error": "prompt_required"})

        cmd = [
            "python",
            os.path.join(BITNET_DIR, "run_inference.py"),
            "-m",
            MODEL_PATH,
            "-p",
            prompt,
            "-n",
            str(data.get("n_predict", 64)),
        ]

        try:
            proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
            return self._send(200, {"output": proc.stdout.strip()})
        except subprocess.CalledProcessError as exc:
            return self._send(500, {"error": exc.stderr[-1000:]})


def main():
    if not API_KEY:
        raise SystemExit("BITNET_CLOUD_API_KEY must be set")
    HTTPServer((HOST, PORT), Handler).serve_forever()


if __name__ == "__main__":
    main()
