#!/usr/bin/env python3
from __future__ import annotations

import os
from pathlib import Path

ALLOWED_ROOTS = [Path.home() / "Documents", Path.home() / "Downloads", Path.home() / "Desktop"]


def can_access_path(path: str) -> bool:
    p = Path(path).expanduser().resolve()
    for root in ALLOWED_ROOTS:
        try:
            if p.is_relative_to(root.resolve()):
                return True
        except Exception:
            continue
    return False


def search_files(base_path: str, query: str, limit: int = 20) -> list[str]:
    if not can_access_path(base_path):
        return []
    base = Path(base_path).expanduser().resolve()
    out = []
    q = query.lower()
    for f in base.rglob("*"):
        if len(out) >= limit:
            break
        name = f.name.lower()
        if q in name:
            out.append(str(f))
    return out
